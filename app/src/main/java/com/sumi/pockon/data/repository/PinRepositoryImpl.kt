package com.sumi.pockon.data.repository

import com.sumi.pockon.data.local.preference.PreferenceLocalDataSource
import com.sumi.pockon.domain.repository.PinRepository
import javax.inject.Inject

class PinRepositoryImpl @Inject constructor(
    private val localDataSource: PreferenceLocalDataSource
) : PinRepository {
    override fun getPin() = localDataSource.getPinNum()
    override fun isEnabled() = localDataSource.isAuthPin()
    override fun save(pin: String) {
        localDataSource.savePinNum(pin)
        localDataSource.saveIsAuthPin(true)
    }
    override fun disable() {
        localDataSource.removePinNum()
        localDataSource.removeAuthPin()
    }
}
