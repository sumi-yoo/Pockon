package com.sumi.pockon.domain.usecase

import com.sumi.pockon.domain.repository.AlarmRepository
import com.sumi.pockon.domain.repository.GiftRepository
import com.sumi.pockon.domain.repository.NotificationSettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class RefreshGiftAlarmsUseCase @Inject constructor(
    private val giftRepository: GiftRepository,
    private val notificationSettingsRepository: NotificationSettingsRepository,
    private val alarmRepository: AlarmRepository
) {
    suspend operator fun invoke() {
        val settings = notificationSettingsRepository.get()
        giftRepository.observeAllGifts().first().forEach { gift ->
            alarmRepository.cancel(gift.id, settings.daysBeforeExpiry)
            if (settings.isEnabled && gift.usedDt.isEmpty()) {
                alarmRepository.schedule(gift, settings.daysBeforeExpiry, settings.hour to settings.minute)
            }
        }
    }
}
