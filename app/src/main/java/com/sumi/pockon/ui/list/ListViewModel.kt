package com.sumi.pockon.ui.list

import com.sumi.pockon.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumi.pockon.domain.usecase.GetNotificationSettingsUseCase
import com.sumi.pockon.domain.usecase.GetUserSessionUseCase
import com.sumi.pockon.data.model.Gift
import com.sumi.pockon.domain.usecase.CancelGiftAlarmUseCase
import com.sumi.pockon.domain.usecase.DeleteGiftUseCase
import com.sumi.pockon.domain.usecase.DeleteGiftsUseCase
import com.sumi.pockon.domain.usecase.SyncGiftListUseCase
import com.sumi.pockon.domain.usecase.UpdateGiftUseCase
import com.sumi.pockon.domain.usecase.ObserveAvailableGiftsUseCase
import com.sumi.pockon.util.NetworkMonitor
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
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val observeAvailableGiftsUseCase: ObserveAvailableGiftsUseCase,
    private val syncGiftListUseCase: SyncGiftListUseCase,
    private val updateGiftUseCase: UpdateGiftUseCase,
    private val deleteGiftUseCase: DeleteGiftUseCase,
    private val deleteGiftsUseCase: DeleteGiftsUseCase,
    private val getNotificationSettingsUseCase: GetNotificationSettingsUseCase,
    private val getUserSessionUseCase: GetUserSessionUseCase,
    private val cancelGiftAlarmUseCase: CancelGiftAlarmUseCase,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _events = MutableSharedFlow<ListEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<ListEvent> = _events.asSharedFlow()

    private val session = getUserSessionUseCase()
    private var uid = session.uid
    private var isGuestMode = session.isGuest
    private var isRefresh = false
    private var removeGift: Gift? = null

    private val _uiState = MutableStateFlow(ListUiState())
    val uiState: StateFlow<ListUiState> = _uiState.asStateFlow()

    private var filterList = listOf<String>()

    init {
        observeGiftList() // 관찰자 등록
    }

    fun setTopTitle(title: Int) {
        _uiState.value = _uiState.value.copy(topTitle = title)
    }

    fun setIsAllSelect(flag: Boolean) {
        _uiState.value = _uiState.value.copy(isAllSelect = flag)
    }

    // 로컬 기프티콘 목록 변화 감지해서 가져오기
    private fun observeGiftList() {
        viewModelScope.launch(Dispatchers.IO) {
            observeAvailableGiftsUseCase().collectLatest { allGift ->
                if (allGift.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        giftList = allGift,
                        copyGiftList = allGift
                    )
                    sortChips()
                    filterList()
                    orderBy()
                } else {
                    _uiState.value = _uiState.value.copy(
                        giftList = emptyList(),
                        copyGiftList = emptyList(),
                        chipElement = emptyMap(),
                        checkedGiftIds = emptyList(),
                        isAllSelect = false
                    )
                    filterList = listOf()
                    if (isRefresh) {
                        toggleIsScrollTop()
                        isRefresh = false
                    }
                }
            }
        }
    }

    // 서버에서 기프티콘 리스트 가져오기
    suspend fun refreshGiftList() {
        filterList = listOf()
        if (isGuestMode) {
            sortChips()
            filterList()
            orderBy()
            toggleIsScrollTop()
            return
        }

        if (!networkMonitor.isConnected()) {
            _uiState.value = _uiState.value.copy(isShowNoInternetDialog = true)
            return
        }

        isRefresh = true
        val result = syncGiftListUseCase(uid)
        if (result.isFailure) {
            isRefresh = false
        }
    }

    private fun sortChips() {
        val element = mutableMapOf("" to true)
        _uiState.value.giftList.forEach {
            if (!element.containsKey(it.brand)) element[it.brand] = false
        }
        val beforeElements = mutableMapOf<String, Boolean>()
        val chips = element.toList().sortedWith(compareBy { it.first }).toMap()
        filterList = filterList.filter { chips.containsKey(it) }
        chips.keys.forEach { key ->
            beforeElements[key] = filterList.contains(key)
        }
        if (filterList.isEmpty()) beforeElements[""] = true
        _uiState.value = _uiState.value.copy(chipElement = beforeElements)
    }

    fun setRemoveGift(gift: Gift) {
        removeGift = gift
    }

    fun changeChipState(targetList: List<String>) {
        val beforeElements = mutableMapOf<String, Boolean>()
        val beforeFilters = mutableListOf<String>()

        _uiState.value.chipElement.keys.forEach { key ->
            val state = _uiState.value.chipElement[key]
            if (targetList.contains(key)) beforeElements[key] = !state!! else beforeElements[key] =
                state!!

            if (targetList.contains("") && key.isNotEmpty()) { // 전체 클릭
                beforeElements[key] = false
            }
            if (!targetList.contains("") && key.isEmpty()) { // 전체 이외 클릭
                beforeElements[key] = false
            }

            if (beforeElements[key] == true && key.isNotEmpty()) beforeFilters.add(key)
        }

        if (beforeFilters.isEmpty() && beforeElements[""] == false) {
            beforeElements[""] = true
        }

        _uiState.value = _uiState.value.copy(chipElement = beforeElements)
        filterList = beforeFilters
        filterList()
        orderBy()
        toggleIsScrollTop()
        clearCheckedGiftList()
    }

    private fun filterList() {
        val filtered = mutableListOf<Gift>()
        _uiState.value.giftList.forEach {
            if (filterList.contains(it.brand) || filterList.isEmpty()) filtered.add(it)
        }
        _uiState.value = _uiState.value.copy(copyGiftList = filtered)
    }

    fun orderBy() {
        val state = _uiState.value
        val sortedGiftList = when (state.topTitle) {
            R.string.top_app_bar_recent -> { // 최신순
                val dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA)
                state.copyGiftList.sortedWith(
                    compareByDescending<Gift> {
                        dateFormat.parse(it.addDt)?.time
                    }.thenBy { it.brand }.thenBy { it.name }
                )
            }
            R.string.top_app_bar_end_date -> { // 만료일순
                val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
                state.copyGiftList.sortedWith(
                    compareBy<Gift> {
                        dateFormat.parse(it.endDt)?.time
                    }.thenBy { it.brand }.thenBy { it.name }
                )
            }
            else -> { // 가나다순
                state.copyGiftList.sortedWith(
                    compareBy(
                        { it.brand },
                        { it.name },
                        { it.endDt.ifEmpty { "99991231" } }
                    )
                )
            }
        }
        _uiState.value = _uiState.value.copy(copyGiftList = sortedGiftList)
        if (isRefresh) {
            toggleIsScrollTop()
            isRefresh = false
        }
    }

    // 기프티콘 수정
    fun usedGift(gift: Gift) {
        if (!isGuestMode && !networkMonitor.isConnected()) {
            _uiState.value = _uiState.value.copy(isShowNoInternetDialog = true)
            _events.tryEmit(ListEvent.GiftUseFailed)
            return
        }

        val nowDt = SimpleDateFormat(
            "yyyy.MM.dd",
            Locale.getDefault()
        ).format(Date(System.currentTimeMillis()))
        val updateGift = gift.copy(usedDt = nowDt)
        viewModelScope.launch {
            val result = updateGiftUseCase(
                isGuestMode = isGuestMode,
                gift = updateGift,
                shouldUploadPhoto = false
            )
            if (result.isSuccess) {
                cancelGiftAlarmUseCase(gift.id, getNotificationSettingsUseCase().daysBeforeExpiry)
            } else {
                _events.emit(ListEvent.GiftUseFailed)
            }
        }
    }

    // 기프티콘 삭제
    fun removeGift() {
        if (!isGuestMode && !networkMonitor.isConnected()) {
            _uiState.value = _uiState.value.copy(isShowNoInternetDialog = true)
            _events.tryEmit(ListEvent.GiftDeleteFailed)
            return
        }

        if (removeGift == null || removeGift?.id?.isEmpty() == true) {
            _events.tryEmit(ListEvent.GiftDeleteFailed)
            return
        }
        val uid = removeGift!!.uid
        val id = removeGift!!.id
        val gift = removeGift!!.copy()
        removeGift = null
        viewModelScope.launch {
            val result = deleteGiftUseCase(isGuestMode, uid, id)
            if (result.isSuccess) {
                cancelGiftAlarmUseCase(gift.id, getNotificationSettingsUseCase().daysBeforeExpiry)
                _events.emit(ListEvent.GiftDeleted(isBulk = false))
            } else {
                _events.emit(ListEvent.GiftDeleteFailed)
            }
        }
    }

    // 선택된 기프티콘 리스트에 추가(for 삭제)
    fun checkedGift(id: String) {
        val state = _uiState.value
        val filteredIds = state.checkedGiftIds.filter { it != id }
        if (filteredIds.size == state.checkedGiftIds.size) { // 선택
            val checkedList = state.checkedGiftIds.toMutableList()
            checkedList.add(id)
            _uiState.value = state.copy(
                checkedGiftIds = checkedList,
                isAllSelect = checkedList.size == state.copyGiftList.size
            )
        } else { // 해제
            _uiState.value = state.copy(checkedGiftIds = filteredIds, isAllSelect = false)
        }
    }

    // 선택된 기프티콘 리스트 초기화
    fun clearCheckedGiftList() {
        _uiState.value = _uiState.value.copy(
            checkedGiftIds = emptyList(),
            isAllSelect = false
        )
    }

    // 전체선택/전체해제
    fun onClickAllSelect() {
        val state = _uiState.value
        val isAllSelect = !state.isAllSelect
        _uiState.value = state.copy(
            isAllSelect = isAllSelect,
            checkedGiftIds = if (isAllSelect) state.copyGiftList.map { it.id } else emptyList()
        )
    }

    // 선택 삭제/전체 삭제
    fun deleteSelection() {
        if (!isGuestMode && !networkMonitor.isConnected()) {
            _uiState.value = _uiState.value.copy(isShowNoInternetDialog = true)
            _events.tryEmit(ListEvent.GiftDeleteFailed)
            return
        }

        val ids = _uiState.value.checkedGiftIds
        if (ids.isEmpty()) {
            _events.tryEmit(ListEvent.GiftDeleteFailed)
            return
        }

        viewModelScope.launch {
            val result = deleteGiftsUseCase(isGuestMode, uid, ids)
            if (result.isSuccess) {
                ids.forEach { id ->
                    cancelGiftAlarmUseCase(id, getNotificationSettingsUseCase().daysBeforeExpiry)
                }
                _events.emit(ListEvent.GiftDeleted(isBulk = true))
            } else {
                _events.emit(ListEvent.GiftDeleteFailed)
            }
        }
    }

    fun changeNoInternetDialogState() {
        _uiState.value = _uiState.value.copy(isShowNoInternetDialog = false)
    }

    fun toggleIsScrollTop() {
        _uiState.value = _uiState.value.copy(isScrollTop = !_uiState.value.isScrollTop)
    }

    fun getFilterList() = this.filterList
}

data class ListUiState(
    val giftList: List<Gift> = emptyList(),
    val copyGiftList: List<Gift> = emptyList(),
    val chipElement: Map<String, Boolean> = emptyMap(),
    val topTitle: Int = R.string.top_app_bar_recent,
    val checkedGiftIds: List<String> = emptyList(),
    val isAllSelect: Boolean = false,
    val isShowNoInternetDialog: Boolean = false,
    val isScrollTop: Boolean = false
)

sealed interface ListEvent {
    data object GiftUseFailed : ListEvent
    data class GiftDeleted(val isBulk: Boolean) : ListEvent
    data object GiftDeleteFailed : ListEvent
}
