package com.sumi.pockon.data.repository

import com.sumi.pockon.data.local.preference.PreferenceLocalDataSource
import com.sumi.pockon.domain.repository.UserSessionRepository
import javax.inject.Inject

class UserSessionRepositoryImpl @Inject constructor(
    private val localDataSource: PreferenceLocalDataSource
) : UserSessionRepository {
    override fun uid() = localDataSource.getUid()
    override fun email() = localDataSource.getEmail()
    override fun isGuest() = localDataSource.isGuestMode()
    override fun save(uid: String, email: String, name: String?, profileImage: String?, isGuest: Boolean) {
        localDataSource.saveUid(uid)
        localDataSource.saveEmail(email)
        localDataSource.saveName(name)
        localDataSource.saveProfileImage(profileImage)
        localDataSource.saveIsGuestMode(isGuest)
    }
    override fun clear() = localDataSource.removeAll()
}
