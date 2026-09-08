package com.sumi.pockon.domain.usecase

import com.sumi.pockon.domain.repository.GiftRepository
import javax.inject.Inject

class GetGiftCountByEndDateUseCase @Inject constructor(
    private val giftRepository: GiftRepository
) {
    suspend operator fun invoke(endDt: String): Result<Int> =
        giftRepository.getGiftCountByEndDate(endDt)
}
