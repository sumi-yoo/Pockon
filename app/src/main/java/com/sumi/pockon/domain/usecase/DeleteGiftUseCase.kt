package com.sumi.pockon.domain.usecase

import com.sumi.pockon.domain.repository.AlarmRepository
import com.sumi.pockon.domain.repository.GiftRepository
import com.sumi.pockon.domain.repository.NotificationSettingsRepository
import javax.inject.Inject

class DeleteGiftUseCase @Inject constructor(
    private val giftRepository: GiftRepository,
    private val notificationSettingsRepository: NotificationSettingsRepository,
    private val alarmRepository: AlarmRepository
) {
    suspend operator fun invoke(isGuestMode: Boolean, uid: String, id: String): Result<Unit> =
        giftRepository.deleteGift(isGuestMode, uid, id).onSuccess {
            alarmRepository.cancel(id, notificationSettingsRepository.get().daysBeforeExpiry)
        }
}
