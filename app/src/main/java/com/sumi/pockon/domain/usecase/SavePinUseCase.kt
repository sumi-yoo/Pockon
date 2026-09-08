package com.sumi.pockon.domain.usecase

import com.sumi.pockon.domain.repository.PinRepository
import javax.inject.Inject

class SavePinUseCase @Inject constructor(private val pinRepository: PinRepository) {
    operator fun invoke(pin: String) = pinRepository.save(pin)
}
