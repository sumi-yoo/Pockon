package com.sumi.pockon.ui.used

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumi.pockon.data.repository.PreferenceRepository
import com.sumi.pockon.data.repository.GiftRepository
import com.sumi.pockon.data.model.Gift
import com.sumi.pockon.domain.usecase.DeleteGiftsUseCase
import com.sumi.pockon.util.NetworkMonitor
import com.sumi.pockon.util.loadImageFromPath
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class UsedViewModel @Inject constructor(
    private val giftRepository: GiftRepository,
    private val deleteGiftsUseCase: DeleteGiftsUseCase,
    private val preferenceRepository: PreferenceRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _events = MutableSharedFlow<UsedEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<UsedEvent> = _events

    private var uid = preferenceRepository.getUid()
    private var isGuestMode = preferenceRepository.isGuestMode()

    private val _giftList = mutableStateOf<List<Gift>>(listOf())
    val giftList: State<List<Gift>> = _giftList

    private val _checkedGiftList = mutableStateOf<List<String>>(listOf())
    val checkedGiftList: State<List<String>> = _checkedGiftList

    private val _isAllSelect = mutableStateOf<Boolean>(false)
    val isAllSelect: State<Boolean> = _isAllSelect

    private val _isShowIndicator = mutableStateOf(false)
    val isShowIndicator: State<Boolean> = _isShowIndicator

    init {
        observeGiftList()
    }

    // 로컬 기프티콘 목록 변화 감지해서 가져오기
    private fun observeGiftList() {
        viewModelScope.launch(Dispatchers.IO) {
            giftRepository.getAllGift(2).collectLatest { allGift ->
                if (allGift.isNotEmpty()) {
                    val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())

                    _giftList.value = allGift.map { gift ->
                        Gift(
                            id = gift.id,
                            uid = gift.uid,
                            photo = loadImageFromPath(gift.photoPath),
                            name = gift.name,
                            brand = gift.brand,
                            endDt = gift.endDt,
                            addDt = gift.addDt,
                            memo = gift.memo,
                            usedDt = gift.usedDt,
                            cash = gift.cash,
                            isFavorite = gift.isFavorite
                        )
                    }.sortedWith(
                        compareByDescending<Gift> { dateFormat.parse(it.usedDt)?.time ?: Long.MIN_VALUE }
                            .thenBy { it.brand }
                            .thenBy { it.name }
                            .thenBy { it.endDt ?: "99991231" }
                    )
                } else {
                    // 기프티콘 없음
                    _giftList.value = listOf()
                }
            }
        }
    }

    fun setIsAllSelect(flag: Boolean) {
        _isAllSelect.value = flag
    }

    // 선택된 기프티콘 리스트 초기화
    fun clearCheckedGiftList() {
        _checkedGiftList.value = listOf()
    }

    // 선택 삭제/전체 삭제
    fun deleteSelection() {
        if (!isGuestMode && !networkMonitor.isConnected()) {
            _events.tryEmit(UsedEvent.DeleteFailed)
            return
        }

        val ids = _checkedGiftList.value
        if (ids.isEmpty()) {
            _events.tryEmit(UsedEvent.DeleteFailed)
            return
        }

        _isShowIndicator.value = true
        viewModelScope.launch {
            try {
                val result = deleteGiftsUseCase(isGuestMode, uid, ids)
                _events.emit(
                    if (result.isSuccess) UsedEvent.Deleted else UsedEvent.DeleteFailed
                )
            } finally {
                _isShowIndicator.value = false
            }
        }
    }

    // 선택된 기프티콘 리스트에 추가(for 삭제)
    fun checkedGift(id: String) {
        val filterList = _checkedGiftList.value.filter { it != id }
        if (filterList.size == _checkedGiftList.value.size) { // 선택
            val checkedList = _checkedGiftList.value.toMutableList()
            checkedList.add(id)
            _checkedGiftList.value = checkedList

            if (_checkedGiftList.value.size == _giftList.value.size) _isAllSelect.value = true
        } else { // 해제
            _checkedGiftList.value = filterList
            if (_checkedGiftList.value.size != _giftList.value.size) _isAllSelect.value = false
        }
    }

    // 전체선택/전체해제
    fun onClickAllSelect() {
        _isAllSelect.value = !_isAllSelect.value
        if (_isAllSelect.value) {
            _checkedGiftList.value = _giftList.value.map { it.id }
        } else {
            _checkedGiftList.value = listOf()
        }
    }
}

sealed interface UsedEvent {
    data object Deleted : UsedEvent
    data object DeleteFailed : UsedEvent
}
