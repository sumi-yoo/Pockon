package com.sumi.pockon.domain.repository

import com.sumi.pockon.data.model.Gift

interface GiftRepository {
    suspend fun addGift(isGuestMode: Boolean, gift: Gift): Result<Unit>
}
