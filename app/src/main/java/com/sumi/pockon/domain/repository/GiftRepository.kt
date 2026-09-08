package com.sumi.pockon.domain.repository

import com.sumi.pockon.data.model.Gift
import kotlinx.coroutines.flow.Flow

interface GiftRepository {
    fun observeAllGifts(): Flow<List<Gift>>

    fun observeUsedGifts(): Flow<List<Gift>>

    fun observeAvailableGifts(): Flow<List<Gift>>

    fun observeGift(id: String): Flow<Gift>

    suspend fun getGiftCountByEndDate(endDt: String): Result<Int>

    suspend fun clearAllGifts(): Result<Unit>

    suspend fun syncGifts(uid: String): Result<Unit>

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

    suspend fun deleteGift(isGuestMode: Boolean, uid: String, id: String): Result<Unit>

    suspend fun deleteGifts(isGuestMode: Boolean, uid: String, ids: List<String>): Result<Unit>
}
