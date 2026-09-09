package com.sumi.pockon.ui.notification

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumi.pockon.domain.model.Gift
import com.sumi.pockon.domain.usecase.CancelGiftAlarmUseCase
import com.sumi.pockon.domain.usecase.ScheduleGiftAlarmUseCase
import com.sumi.pockon.domain.usecase.ObserveAllGiftsUseCase
import com.sumi.pockon.domain.usecase.GetNotificationSettingsUseCase
import com.sumi.pockon.domain.usecase.SaveNotificationSettingsUseCase
import com.sumi.pockon.util.convertTo12HourFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationSettingViewModel @Inject constructor(
    private val observeAllGiftsUseCase: ObserveAllGiftsUseCase,
    private val cancelGiftAlarmUseCase: CancelGiftAlarmUseCase,
    private val scheduleGiftAlarmUseCase: ScheduleGiftAlarmUseCase,
    private val getNotificationSettingsUseCase: GetNotificationSettingsUseCase,
    private val saveNotificationSettingsUseCase: SaveNotificationSettingsUseCase
) : ViewModel() {

    private var giftList = listOf<Gift>()
    private var isLoading = true
    private var selectedHour = 0
    private var selectedMinute = 0

    private val dayList = listOf(0, 1, 3, 7, 14)
    private var notificationSettings = getNotificationSettingsUseCase()

    private val _seletedTime = mutableStateOf("")
    val seletedTime: State<String> = _seletedTime

    private val _seletedDay = mutableIntStateOf(notificationSettings.daysBeforeExpiry)
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
        saveNotificationSettingsUseCase(notificationSettings)
    }

    fun changeNotiEndDt() {
        if (!notificationSettings.isEnabled || isLoading) return

        val previousDaysBeforeExpiry = notificationSettings.daysBeforeExpiry
        notificationSettings = notificationSettings.copy(daysBeforeExpiry = _seletedDay.intValue)
        saveNotificationSettingsUseCase(notificationSettings)
        giftList.forEach { gift ->
            // 알림 등록
            cancelGiftAlarmUseCase(gift.id, previousDaysBeforeExpiry)
            if (gift.usedDt.isEmpty()) {
                scheduleGiftAlarmUseCase(
                    gift,
                    notificationSettings.daysBeforeExpiry,
                    notificationSettings.hour to notificationSettings.minute
                )
            }
        }
    }
}
