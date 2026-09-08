package com.sumi.pockon.domain.usecase

import com.sumi.pockon.domain.repository.AppPreferencesRepository
import javax.inject.Inject

class IsInitialGiftSyncRequiredUseCase @Inject constructor(
    private val repository: AppPreferencesRepository
) {
    operator fun invoke() = repository.isInitialGiftSyncRequired()
}
