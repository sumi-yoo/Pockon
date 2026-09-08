package com.sumi.pockon.domain.repository

import com.sumi.pockon.data.model.Gift

interface GiftRepository {
    suspend fun addGift(isGuestMode: Boolean, gift: Gift): Result<Unit>

    suspend fun updateGift(
        isGuestMode: Boolean,
        gift: Gift,
        shouldUploadPhoto: Boolean
    ): Result<Unit>

    suspend fun updateGiftFavorite(
        isGuestMode: Boolean,
        id: String,
        isFavorite: Boolean
    ): Result<Unit>
}
