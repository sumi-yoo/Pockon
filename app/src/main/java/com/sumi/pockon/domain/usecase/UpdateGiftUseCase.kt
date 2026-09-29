package com.sumi.pockon.domain.usecase

import com.sumi.pockon.domain.model.Gift
import com.sumi.pockon.domain.repository.GiftRepository
import javax.inject.Inject

class UpdateGiftUseCase @Inject constructor(
    private val giftRepository: GiftRepository
) {
    suspend operator fun invoke(
        isGuestMode: Boolean,
        gift: Gift,
        photoBytes: ByteArray? = null
    ): Result<Unit> = giftRepository.updateGift(
        isGuestMode = isGuestMode,
        gift = gift,
        photoBytes = photoBytes
    )
}
