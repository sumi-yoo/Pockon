package com.sumi.pockon.domain.usecase

import com.sumi.pockon.domain.repository.GiftRepository
import javax.inject.Inject

class ClearAllGiftsUseCase @Inject constructor(
    private val giftRepository: GiftRepository
) {
    suspend operator fun invoke(): Result<Unit> = giftRepository.clearAllGifts()
}
