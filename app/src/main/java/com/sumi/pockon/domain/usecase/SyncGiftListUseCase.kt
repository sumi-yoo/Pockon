package com.sumi.pockon.domain.usecase

import com.sumi.pockon.domain.repository.GiftRepository
import javax.inject.Inject

class SyncGiftListUseCase @Inject constructor(
    private val giftRepository: GiftRepository
) {
    suspend operator fun invoke(uid: String): Result<Unit> = giftRepository.syncGifts(uid)
}
