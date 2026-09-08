package com.sumi.pockon.data.repository

import com.sumi.pockon.data.local.preference.PreferenceLocalDataSource
import com.sumi.pockon.domain.repository.AppPreferencesRepository
import javax.inject.Inject

class AppPreferencesRepositoryImpl @Inject constructor(
    private val localDataSource: PreferenceLocalDataSource
) : AppPreferencesRepository {
    override fun isInitialGiftSyncRequired() = localDataSource.isFirstLogin()

    override fun markInitialGiftSyncCompleted() {
        localDataSource.saveIsFirstLogin(false)
    }

    override fun isNotificationPermissionRationaleShown() = localDataSource.isPermRationale()

    override fun markNotificationPermissionRationaleShown() {
        localDataSource.saveIsPermRationale(true)
    }
}
