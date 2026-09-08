package com.sumi.pockon.domain.usecase

import com.sumi.pockon.domain.repository.UserSessionRepository
import javax.inject.Inject

class SaveUserSessionUseCase @Inject constructor(private val repository: UserSessionRepository) {
    operator fun invoke(uid: String, email: String = "", name: String? = null, profileImage: String? = null, isGuest: Boolean = false) =
        repository.save(uid, email, name, profileImage, isGuest)
}
