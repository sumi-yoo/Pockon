package com.sumi.pockon.domain.usecase

import android.content.Intent
import com.sumi.pockon.domain.repository.AuthRepository
import javax.inject.Inject

class GetSignInIntentUseCase @Inject constructor(private val authRepository: AuthRepository) {
    suspend operator fun invoke(accountName: String?): Result<Intent> = authRepository.getSignInIntent(accountName)
}
