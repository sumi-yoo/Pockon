package com.sumi.pockon.ui.used

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumi.pockon.domain.usecase.GetUserSessionUseCase
import com.sumi.pockon.domain.model.Gift
import com.sumi.pockon.domain.usecase.DeleteGiftsUseCase
import com.sumi.pockon.domain.repository.GiftRepository
import com.sumi.pockon.domain.repository.GiftPhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class UsedViewModel @Inject constructor(
    private val giftRepository: GiftRepository,
    private val giftPhotoRepository: GiftPhotoRepository,
    private val deleteGiftsUseCase: DeleteGiftsUseCase,
    private val getUserSessionUseCase: GetUserSessionUseCase
) : ViewModel() {

    private val _events = MutableSharedFlow<UsedEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<UsedEvent> = _events.asSharedFlow()

    private val session = getUserSessionUseCase()
    private var uid = session.uid
    private var isGuestMode = session.isGuest

    private val _uiState = MutableStateFlow(UsedUiState())
    val uiState: StateFlow<UsedUiState> = _uiState.asStateFlow()

    init {
        observeGiftList()
    }

    // 로컬 기프티콘 목록 변화 감지해서 가져오기
    private fun observeGiftList() {
        viewModelScope.launch(Dispatchers.IO) {
            giftRepository.observeUsedGifts().collectLatest { allGift ->
                val photoPaths = giftPhotoRepository.getPhotoPaths(allGift.map(Gift::id))
                if (allGift.isNotEmpty()) {
                    val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())

                    _uiState.value = _uiState.value.copy(giftList = allGift.sortedWith(
                        compareByDescending<Gift> { dateFormat.parse(it.usedDt)?.time ?: Long.MIN_VALUE }
                            .thenBy { it.brand }
                            .thenBy { it.name }
                            .thenBy { it.endDt.ifEmpty { "99991231" } }
                    ), photoPaths = photoPaths)
                } else {
                    _uiState.value = UsedUiState()
                }
            }
        }
    }

    fun setIsAllSelect(flag: Boolean) {
        _uiState.value = _uiState.value.copy(isAllSelect = flag)
    }

    // 선택된 기프티콘 리스트 초기화
    fun clearCheckedGiftList() {
        _uiState.value = _uiState.value.copy(checkedGiftIds = emptyList())
    }

    // 선택 삭제/전체 삭제
    fun deleteSelection() {
        val ids = _uiState.value.checkedGiftIds
        if (ids.isEmpty()) {
            _events.tryEmit(UsedEvent.DeleteFailed)
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val result = runCatching {
                    deleteGiftsUseCase(isGuestMode, uid, ids)
                }.getOrNull()
                _events.emit(
                    if (result?.isSuccess == true) UsedEvent.Deleted else UsedEvent.DeleteFailed
                )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    // 선택된 기프티콘 리스트에 추가(for 삭제)
    fun checkedGift(id: String) {
        val state = _uiState.value
        val filterList = state.checkedGiftIds.filter { it != id }
        if (filterList.size == state.checkedGiftIds.size) { // 선택
            val checkedList = state.checkedGiftIds.toMutableList()
            checkedList.add(id)
            _uiState.value = state.copy(
                checkedGiftIds = checkedList,
                isAllSelect = checkedList.size == state.giftList.size
            )
        } else { // 해제
            _uiState.value = state.copy(
                checkedGiftIds = filterList,
                isAllSelect = false
            )
        }
    }

    // 전체선택/전체해제
    fun onClickAllSelect() {
        val state = _uiState.value
        val isAllSelect = !state.isAllSelect
        _uiState.value = state.copy(
            isAllSelect = isAllSelect,
            checkedGiftIds = if (isAllSelect) state.giftList.map { it.id } else emptyList()
        )
    }
}

data class UsedUiState(
    val giftList: List<Gift> = emptyList(),
    val photoPaths: Map<String, String> = emptyMap(),
    val checkedGiftIds: List<String> = emptyList(),
    val isAllSelect: Boolean = false,
    val isLoading: Boolean = false
)

sealed interface UsedEvent {
    data object Deleted : UsedEvent
    data object DeleteFailed : UsedEvent
}
