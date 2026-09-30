package com.sumi.pockon.ui.notification

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumi.pockon.domain.usecase.UpdateNotificationSettingsUseCase
import com.sumi.pockon.domain.repository.NotificationSettingsRepository
import com.sumi.pockon.util.convertTo12HourFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationSettingViewModel @Inject constructor(
    private val notificationSettingsRepository: NotificationSettingsRepository,
    private val updateNotificationSettingsUseCase: UpdateNotificationSettingsUseCase
) : ViewModel() {

    private var selectedHour = 0
    private var selectedMinute = 0

    private val dayList = listOf(0, 1, 3, 7, 14)
    private var notificationSettings = notificationSettingsRepository.get()

    private val _seletedTime = mutableStateOf("")
    val seletedTime: State<String> = _seletedTime

    private val _seletedDay = mutableIntStateOf(notificationSettings.daysBeforeExpiry)
    val seletedDay: State<Int> = _seletedDay

    private val _isShowTimePickerWheelDialog = mutableStateOf(false)
    val isShowTimePickerWheelDialog: State<Boolean> = _isShowTimePickerWheelDialog

    init {
        initTime()

    }

    private fun initTime() {
        _seletedTime.value = convertTo12HourFormat(notificationSettings.hour, notificationSettings.minute)
        selectedHour = notificationSettings.hour
        selectedMinute = notificationSettings.minute
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
        notificationSettings = notificationSettings.copy(hour = selectedHour, minute = selectedMinute)
        viewModelScope.launch { updateNotificationSettingsUseCase(notificationSettings) }
    }

    fun changeNotiEndDt() {
        if (!notificationSettings.isEnabled) return

        notificationSettings = notificationSettings.copy(daysBeforeExpiry = _seletedDay.intValue)
        viewModelScope.launch { updateNotificationSettingsUseCase(notificationSettings) }
    }
}
