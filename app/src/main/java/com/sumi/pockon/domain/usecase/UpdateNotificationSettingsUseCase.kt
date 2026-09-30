package com.sumi.pockon.domain.usecase

import com.sumi.pockon.domain.model.NotificationSettings
import com.sumi.pockon.domain.repository.NotificationSettingsRepository
import javax.inject.Inject

class UpdateNotificationSettingsUseCase @Inject constructor(
    private val notificationSettingsRepository: NotificationSettingsRepository,
    private val refreshGiftAlarmsUseCase: RefreshGiftAlarmsUseCase
) {
    suspend operator fun invoke(settings: NotificationSettings) {
        notificationSettingsRepository.save(settings)
        refreshGiftAlarmsUseCase()
    }

    suspend fun disable() {
        invoke(notificationSettingsRepository.get().copy(isEnabled = false))
    }
}
