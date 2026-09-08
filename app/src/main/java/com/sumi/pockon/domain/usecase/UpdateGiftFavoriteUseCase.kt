package com.sumi.pockon.domain.usecase

import com.sumi.pockon.domain.repository.GiftRepository
import javax.inject.Inject

class UpdateGiftFavoriteUseCase @Inject constructor(
    private val giftRepository: GiftRepository
) {
    suspend operator fun invoke(
        isGuestMode: Boolean,
        id: String,
        isFavorite: Boolean
    ): Result<Unit> = giftRepository.updateGiftFavorite(
        isGuestMode = isGuestMode,
        id = id,
        isFavorite = isFavorite
    )
}
