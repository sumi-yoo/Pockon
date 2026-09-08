package com.sumi.pockon.domain.usecase

import com.sumi.pockon.domain.repository.UserSessionRepository
import javax.inject.Inject

class GetUserSessionUseCase @Inject constructor(private val repository: UserSessionRepository) {
    operator fun invoke() = Session(repository.uid(), repository.email(), repository.isGuest())
}

data class Session(val uid: String, val email: String, val isGuest: Boolean)
