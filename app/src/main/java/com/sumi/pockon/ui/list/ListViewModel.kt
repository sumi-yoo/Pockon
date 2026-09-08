package com.sumi.pockon.ui.list

import com.sumi.pockon.R
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumi.pockon.data.repository.PreferenceRepository
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
import kotlinx.coroutines.flow.SharedFlow
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
    private val preferenceRepository: PreferenceRepository,
    private val cancelGiftAlarmUseCase: CancelGiftAlarmUseCase,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _events = MutableSharedFlow<ListEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<ListEvent> = _events

    private var uid = preferenceRepository.getUid()
    private var isGuestMode = preferenceRepository.isGuestMode()
    private var isRefresh = false
    private var removeGift: Gift? = null

    private val _giftList = mutableStateOf<List<Gift>>(listOf())
    val giftList: State<List<Gift>> = _giftList

    private val _copyGiftList = mutableStateOf<List<Gift>>(listOf())
    val copyGiftList: State<List<Gift>> = _copyGiftList

    private var filterList = listOf<String>()

    private var _chipElement = mutableStateOf<Map<String, Boolean>?>(null)
    val chipElement: State<Map<String, Boolean>?> = _chipElement

    private val _topTitle = mutableIntStateOf(R.string.top_app_bar_recent)
    val topTitle: State<Int> = _topTitle

    private val _checkedGiftList = mutableStateOf<List<String>>(listOf())
    val checkedGiftList: State<List<String>> = _checkedGiftList

    private val _isAllSelect = mutableStateOf(false)
    val isAllSelect: State<Boolean> = _isAllSelect

    private val _isShowNoInternetDialog = mutableStateOf(false)
    val isShowNoInternetDialog: State<Boolean> = _isShowNoInternetDialog

    private val _isScrollTop = mutableStateOf(false)
    val isScrollTop: State<Boolean> = _isScrollTop

    init {
        observeGiftList() // 관찰자 등록
    }

    fun setTopTitle(title: Int) {
        _topTitle.intValue = title
    }

    fun setIsAllSelect(flag: Boolean) {
        _isAllSelect.value = flag
    }

    // 로컬 기프티콘 목록 변화 감지해서 가져오기
    private fun observeGiftList() {
        viewModelScope.launch(Dispatchers.IO) {
            observeAvailableGiftsUseCase().collectLatest { allGift ->
                if (allGift.isNotEmpty()) {
                    _giftList.value = allGift
                    _copyGiftList.value = _giftList.value
                    sortChips()
                    filterList()
                    orderBy()
                } else {
                    _giftList.value = listOf()
                    _copyGiftList.value = listOf()
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
            _isShowNoInternetDialog.value = true
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
        _giftList.value.forEach {
            if (!element.containsKey(it.brand)) element[it.brand] = false
        }
        val beforeElements = mutableMapOf<String, Boolean>()
        val chips = element.toList().sortedWith(compareBy { it.first }).toMap()
        filterList = filterList.filter { chips.containsKey(it) }
        chips.keys.forEach { key ->
            beforeElements[key] = filterList.contains(key)
        }
        if (filterList.isEmpty()) beforeElements[""] = true
        _chipElement.value = beforeElements
    }

    fun setRemoveGift(gift: Gift) {
        removeGift = gift
    }

    fun changeChipState(targetList: List<String>) {
        val beforeElements = mutableMapOf<String, Boolean>()
        val beforeFilters = mutableListOf<String>()

        _chipElement.value?.keys?.forEach { key ->
            val state = _chipElement.value!![key]
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

        _chipElement.value = beforeElements
        filterList = beforeFilters
        filterList()
        orderBy()
        toggleIsScrollTop()
        clearCheckedGiftList()
    }

    private fun filterList() {
        val filtered = mutableListOf<Gift>()
        _giftList.value.forEach {
            if (filterList.contains(it.brand) || filterList.isEmpty()) filtered.add(it)
        }
        _copyGiftList.value = filtered
    }

    fun orderBy() {
        when (_topTitle.intValue) {
            R.string.top_app_bar_recent -> { // 최신순
                val dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA)
                _copyGiftList.value = _copyGiftList.value.sortedWith(
                    compareByDescending<Gift> {
                        dateFormat.parse(it.addDt)?.time
                    }.thenBy { it.brand }.thenBy { it.name }
                )
            }
            R.string.top_app_bar_end_date -> { // 만료일순
                val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
                _copyGiftList.value = _copyGiftList.value.sortedWith(
                    compareBy<Gift> {
                        dateFormat.parse(it.endDt)?.time
                    }.thenBy { it.brand }.thenBy { it.name }
                )
            }
            else -> { // 가나다순
                _copyGiftList.value = _copyGiftList.value.sortedWith(
                    compareBy(
                        { it.brand },
                        { it.name },
                        { it.endDt.ifEmpty { "99991231" } }
                    )
                )
            }
        }
        if (isRefresh) {
            toggleIsScrollTop()
            isRefresh = false
        }
    }

    // 기프티콘 수정
    fun usedGift(gift: Gift) {
        if (!isGuestMode && !networkMonitor.isConnected()) {
            _isShowNoInternetDialog.value = true
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
                cancelGiftAlarmUseCase(gift.id, preferenceRepository.getNotiEndDtDay())
            } else {
                _events.emit(ListEvent.GiftUseFailed)
            }
        }
    }

    // 기프티콘 삭제
    fun removeGift() {
        if (!isGuestMode && !networkMonitor.isConnected()) {
            _isShowNoInternetDialog.value = true
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
                cancelGiftAlarmUseCase(gift.id, preferenceRepository.getNotiEndDtDay())
                _events.emit(ListEvent.GiftDeleted(isBulk = false))
            } else {
                _events.emit(ListEvent.GiftDeleteFailed)
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

            if (_checkedGiftList.value.size == _copyGiftList.value.size) _isAllSelect.value = true
        } else { // 해제
            _checkedGiftList.value = filterList
            if (_checkedGiftList.value.size != _copyGiftList.value.size) _isAllSelect.value = false
        }
    }

    // 선택된 기프티콘 리스트 초기화
    fun clearCheckedGiftList() {
        _checkedGiftList.value = listOf()
    }

    // 전체선택/전체해제
    fun onClickAllSelect() {
        _isAllSelect.value = !_isAllSelect.value
        if (_isAllSelect.value) {
            _checkedGiftList.value = _copyGiftList.value.map { it.id }
        } else {
            _checkedGiftList.value = listOf()
        }
    }

    // 선택 삭제/전체 삭제
    fun deleteSelection() {
        if (!isGuestMode && !networkMonitor.isConnected()) {
            _isShowNoInternetDialog.value = true
            _events.tryEmit(ListEvent.GiftDeleteFailed)
            return
        }

        val ids = _checkedGiftList.value
        if (ids.isEmpty()) {
            _events.tryEmit(ListEvent.GiftDeleteFailed)
            return
        }

        viewModelScope.launch {
            val result = deleteGiftsUseCase(isGuestMode, uid, ids)
            if (result.isSuccess) {
                ids.forEach { id ->
                    cancelGiftAlarmUseCase(id, preferenceRepository.getNotiEndDtDay())
                }
                _events.emit(ListEvent.GiftDeleted(isBulk = true))
            } else {
                _events.emit(ListEvent.GiftDeleteFailed)
            }
        }
    }

    fun changeNoInternetDialogState() {
        _isShowNoInternetDialog.value = !_isShowNoInternetDialog.value
    }

    fun toggleIsScrollTop() {
        _isScrollTop.value = !_isScrollTop.value
    }

    fun getFilterList() = this.filterList
}

sealed interface ListEvent {
    data object GiftUseFailed : ListEvent
    data class GiftDeleted(val isBulk: Boolean) : ListEvent
    data object GiftDeleteFailed : ListEvent
}
