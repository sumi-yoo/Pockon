package com.sumi.pockon.ui.notification

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumi.pockon.data.repository.PreferenceRepository
import com.sumi.pockon.data.model.Gift
import com.sumi.pockon.data.repository.AlarmRepository
import com.sumi.pockon.domain.usecase.ObserveAllGiftsUseCase
import com.sumi.pockon.util.convertTo12HourFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationSettingViewModel @Inject constructor(
    private val observeAllGiftsUseCase: ObserveAllGiftsUseCase,
    private val preferenceRepository: PreferenceRepository,
    private val alarmRepository: AlarmRepository
) : ViewModel() {

    private var giftList = listOf<Gift>()
    private var isLoading = true
    private var selectedHour = 0
    private var selectedMinute = 0

    private val dayList = listOf(0, 1, 3, 7, 14)
    private var isNotiEndDt = preferenceRepository.isNotiEndDt()
    private var notiEndDtDay = preferenceRepository.getNotiEndDtDay()

    private val _seletedTime = mutableStateOf("")
    val seletedTime: State<String> = _seletedTime

    private val _seletedDay = mutableIntStateOf(preferenceRepository.getNotiEndDtDay())
    val seletedDay: State<Int> = _seletedDay

    private val _isShowTimePickerWheelDialog = mutableStateOf(false)
    val isShowTimePickerWheelDialog: State<Boolean> = _isShowTimePickerWheelDialog

    init {
        initTime()

        viewModelScope.launch(Dispatchers.IO) {
            observeAllGiftsUseCase().collectLatest { allGift ->
                giftList = allGift
                isLoading = false
            }
        }
    }

    private fun initTime() {
        val time = preferenceRepository.getNotiEndDtTime()
        _seletedTime.value = convertTo12HourFormat(time.first, time.second)
        selectedHour = time.first
        selectedMinute = time.second
    }

    fun getDayList() = dayList

    fun setSeletedDay(day: Int) {
        _seletedDay.intValue = day
    }
    fun toggleIsShowTimePickerWheelDialog() {
        _isShowTimePickerWheelDialog.value = !_isShowTimePickerWheelDialog.value
    }

    fun getNotiEndDtTime(): Pair<Int, Int> {
        return Pair(selectedHour, selectedMinute)
    }

    fun selectedTime(hour24: Int, minute: Int) {
        selectedHour = hour24
        selectedMinute = minute
    }

    fun confirmTime() {
        _seletedTime.value = convertTo12HourFormat(selectedHour, selectedMinute)
        preferenceRepository.saveNotiEndDtTime(selectedHour, selectedMinute)
    }

    fun changeNotiEndDt() {
        if (!isNotiEndDt || isLoading) return

        preferenceRepository.saveNotiEndDtDay(_seletedDay.intValue)
        giftList.forEach { gift ->
            // 알림 등록
            alarmRepository.cancelAlarm(gift.id, notiEndDtDay)
            if (gift.usedDt.isEmpty()) alarmRepository.setAlarm(gift, preferenceRepository.getNotiEndDtDay(), preferenceRepository.getNotiEndDtTime())
        }
    }
}
