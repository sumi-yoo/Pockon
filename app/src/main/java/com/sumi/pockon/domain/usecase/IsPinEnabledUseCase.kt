package com.sumi.pockon.domain.usecase

import com.sumi.pockon.domain.repository.PinRepository
import javax.inject.Inject

class IsPinEnabledUseCase @Inject constructor(private val pinRepository: PinRepository) {
    operator fun invoke(): Boolean = pinRepository.isEnabled()
}
