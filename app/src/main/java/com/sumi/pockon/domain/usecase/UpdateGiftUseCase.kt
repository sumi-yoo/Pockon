package com.sumi.pockon.domain.usecase

import com.sumi.pockon.domain.model.Gift
import com.sumi.pockon.domain.repository.AlarmRepository
import com.sumi.pockon.domain.repository.GiftRepository
import com.sumi.pockon.domain.repository.NotificationSettingsRepository
import javax.inject.Inject

class UpdateGiftUseCase @Inject constructor(
    private val giftRepository: GiftRepository,
    private val notificationSettingsRepository: NotificationSettingsRepository,
    private val alarmRepository: AlarmRepository
) {
    suspend operator fun invoke(
        isGuestMode: Boolean,
        gift: Gift,
        photoBytes: ByteArray? = null
    ): Result<Unit> = giftRepository.updateGift(
        isGuestMode = isGuestMode,
        gift = gift,
        photoBytes = photoBytes
    ).onSuccess {
        val settings = notificationSettingsRepository.get()
        alarmRepository.cancel(gift.id, settings.daysBeforeExpiry)
        if (settings.isEnabled && gift.usedDt.isEmpty()) {
            alarmRepository.schedule(gift, settings.daysBeforeExpiry, settings.hour to settings.minute)
        }
    }
}
