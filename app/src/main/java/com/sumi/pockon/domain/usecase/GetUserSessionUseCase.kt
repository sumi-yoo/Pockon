package com.sumi.pockon.domain.usecase

import com.sumi.pockon.domain.repository.UserSessionRepository
import javax.inject.Inject

class GetUserSessionUseCase @Inject constructor(private val repository: UserSessionRepository) {
    operator fun invoke() = Session(
        uid = repository.uid(),
        email = repository.email(),
        name = repository.name(),
        profileImage = repository.profileImage(),
        isGuest = repository.isGuest()
    )
}

data class Session(
    val uid: String,
    val email: String,
    val name: String?,
    val profileImage: String?,
    val isGuest: Boolean
)
