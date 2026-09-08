package com.sumi.pockon.domain.usecase

import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.sumi.pockon.domain.repository.AuthRepository
import javax.inject.Inject

class GetGoogleCredentialUseCase @Inject constructor(private val authRepository: AuthRepository) {
    suspend operator fun invoke(): Result<GoogleIdTokenCredential> = authRepository.getGoogleCredential()
}
