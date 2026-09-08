package com.sumi.pockon.data.repository

import com.sumi.pockon.data.local.preference.PreferenceLocalDataSource
import com.sumi.pockon.domain.model.NotificationSettings
import com.sumi.pockon.domain.repository.NotificationSettingsRepository
import javax.inject.Inject

class NotificationSettingsRepositoryImpl @Inject constructor(
    private val localDataSource: PreferenceLocalDataSource
) : NotificationSettingsRepository {
    override fun get() = NotificationSettings(
        isEnabled = localDataSource.isNotiEndDt(),
        daysBeforeExpiry = localDataSource.getNotiEndDtDay(),
        hour = localDataSource.getNotiEndDtHour(),
        minute = localDataSource.getNotiEndDtMinute()
    )

    override fun save(settings: NotificationSettings) {
        localDataSource.saveIsNotiEndDt(settings.isEnabled)
        localDataSource.saveNotiEndDtDay(settings.daysBeforeExpiry)
        localDataSource.saveNotiEndDtHour(settings.hour)
        localDataSource.saveNotiEndDtMinute(settings.minute)
    }
}
