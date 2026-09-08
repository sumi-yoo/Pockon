package com.sumi.pockon.domain.usecase

import com.sumi.pockon.data.model.Gift
import com.sumi.pockon.domain.repository.GiftRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveUsedGiftsUseCase @Inject constructor(
    private val giftRepository: GiftRepository
) {
    operator fun invoke(): Flow<List<Gift>> = giftRepository.observeUsedGifts()
}
