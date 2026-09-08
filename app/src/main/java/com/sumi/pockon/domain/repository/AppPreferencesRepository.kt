package com.sumi.pockon.domain.repository

interface AppPreferencesRepository {
    fun isInitialGiftSyncRequired(): Boolean
    fun markInitialGiftSyncCompleted()
    fun isNotificationPermissionRationaleShown(): Boolean
    fun markNotificationPermissionRationaleShown()
}
