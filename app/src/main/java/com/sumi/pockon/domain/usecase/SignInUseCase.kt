package com.sumi.pockon.domain.usecase

import com.sumi.pockon.domain.repository.AuthRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(private val authRepository: AuthRepository) {
    suspend operator fun invoke(idToken: String): Result<String> = authRepository.signIn(idToken)
}
