package com.sumi.pockon.domain.usecase

import com.sumi.pockon.domain.repository.AlarmRepository
import com.sumi.pockon.domain.repository.GiftRepository
import com.sumi.pockon.domain.repository.NotificationSettingsRepository
import javax.inject.Inject

class DeleteGiftsUseCase @Inject constructor(
    private val giftRepository: GiftRepository,
    private val notificationSettingsRepository: NotificationSettingsRepository,
    private val alarmRepository: AlarmRepository
) {
    suspend operator fun invoke(isGuestMode: Boolean, uid: String, ids: List<String>): Result<Unit> =
        giftRepository.deleteGifts(isGuestMode, uid, ids).onSuccess {
            val daysBeforeExpiry = notificationSettingsRepository.get().daysBeforeExpiry
            ids.forEach { id -> alarmRepository.cancel(id, daysBeforeExpiry) }
        }
}
