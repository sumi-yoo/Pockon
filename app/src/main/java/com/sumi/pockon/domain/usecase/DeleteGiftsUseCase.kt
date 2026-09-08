package com.sumi.pockon.domain.usecase

import com.sumi.pockon.domain.repository.GiftRepository
import javax.inject.Inject

class DeleteGiftsUseCase @Inject constructor(
    private val giftRepository: GiftRepository
) {
    suspend operator fun invoke(isGuestMode: Boolean, uid: String, ids: List<String>): Result<Unit> =
        giftRepository.deleteGifts(isGuestMode, uid, ids)
}
