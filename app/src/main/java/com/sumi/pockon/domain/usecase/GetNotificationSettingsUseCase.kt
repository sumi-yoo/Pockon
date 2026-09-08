package com.sumi.pockon.domain.usecase

import com.sumi.pockon.domain.repository.NotificationSettingsRepository
import javax.inject.Inject

class GetNotificationSettingsUseCase @Inject constructor(
    private val repository: NotificationSettingsRepository
) {
    operator fun invoke() = repository.get()
}
