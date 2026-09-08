package com.sumi.pockon.domain.repository

import com.sumi.pockon.domain.model.NotificationSettings

interface NotificationSettingsRepository {
    fun get(): NotificationSettings
    fun save(settings: NotificationSettings)
}
