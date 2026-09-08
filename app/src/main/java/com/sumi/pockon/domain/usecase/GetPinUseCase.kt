package com.sumi.pockon.domain.usecase

import com.sumi.pockon.domain.repository.PinRepository
import javax.inject.Inject

class GetPinUseCase @Inject constructor(private val pinRepository: PinRepository) {
    operator fun invoke(): String = pinRepository.getPin()
}
