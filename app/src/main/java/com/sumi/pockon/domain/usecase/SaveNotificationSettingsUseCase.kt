package com.sumi.pockon.domain.usecase

import com.sumi.pockon.domain.model.NotificationSettings
import com.sumi.pockon.domain.repository.NotificationSettingsRepository
import javax.inject.Inject

class SaveNotificationSettingsUseCase @Inject constructor(
    private val repository: NotificationSettingsRepository
) {
    operator fun invoke(settings: NotificationSettings) = repository.save(settings)
}
