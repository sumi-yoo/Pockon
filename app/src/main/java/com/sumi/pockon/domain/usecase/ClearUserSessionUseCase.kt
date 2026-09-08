package com.sumi.pockon.domain.usecase

import com.sumi.pockon.domain.repository.UserSessionRepository
import javax.inject.Inject

class ClearUserSessionUseCase @Inject constructor(private val repository: UserSessionRepository) {
    operator fun invoke() = repository.clear()
}
