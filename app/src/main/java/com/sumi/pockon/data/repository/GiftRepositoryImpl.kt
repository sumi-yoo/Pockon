package com.sumi.pockon.data.repository

import com.sumi.pockon.data.local.gift.GiftPhotoCache
import com.sumi.pockon.data.local.gift.GiftLocalDataSource
import com.sumi.pockon.data.mapper.toDomain
import com.sumi.pockon.data.mapper.toEntity
import com.sumi.pockon.data.remote.gift.GiftDataRemoteSource
import com.sumi.pockon.data.remote.gift.GiftPhotoRemoteDataSource
import com.sumi.pockon.data.remote.gift.toDomain
import com.sumi.pockon.data.remote.gift.toDto
import com.sumi.pockon.domain.model.Gift
import com.sumi.pockon.domain.repository.GiftRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class GiftRepositoryImpl @Inject constructor(
    private val giftDataRemoteSource: GiftDataRemoteSource,
    private val giftPhotoRemoteDataSource: GiftPhotoRemoteDataSource,
    private val giftLocalDataSource: GiftLocalDataSource,
    private val giftPhotoCache: GiftPhotoCache
) : GiftRepository {

    override fun observeAllGifts(): Flow<List<Gift>> =
        giftLocalDataSource.getAllGift().map { gifts -> gifts.map { it.toDomain() } }

    override fun observeUsedGifts(): Flow<List<Gift>> =
        giftLocalDataSource.getAllUsedGift().map { gifts -> gifts.map { it.toDomain() } }

    override fun observeAvailableGifts(): Flow<List<Gift>> =
        giftLocalDataSource.getAllNotUsedGift().map { gifts -> gifts.map { it.toDomain() } }

    override fun observeGift(id: String): Flow<Gift> = giftLocalDataSource.getGift(id)
        .map { gift -> gift.toDomain() }

    override suspend fun getGiftCountByEndDate(endDt: String): Result<Int> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(giftLocalDataSource.getGiftCountByEndDate(endDt))
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Result.failure(exception)
            }
        }

    override suspend fun clearAllGifts(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            giftLocalDataSource.deleteAllGift()
            Result.success(Unit)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override suspend fun syncGifts(uid: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val gifts = giftDataRemoteSource.loadGifts(uid).map { it.toDomain() }
            val photos = giftPhotoRemoteDataSource.downloadPhotos(
                uid = uid,
                ids = gifts.map(Gift::id)
            )
            val cachedPaths = giftLocalDataSource.getPhotoPaths(gifts.map(Gift::id))
                .associate { it.id to it.photoPath }
            val giftEntities = gifts.map { gift ->
                val photoPath = photos[gift.id]?.let { giftPhotoCache.save(gift.id, it) }
                    ?: cachedPaths[gift.id].orEmpty()
                gift.toEntity(gift.id, photoPath)
            }

            giftLocalDataSource.deleteAllAndInsertGifts(giftEntities)
            Result.success(Unit)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override suspend fun addGift(isGuestMode: Boolean, gift: Gift, photoBytes: ByteArray): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val id = if (isGuestMode) {
                    SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault()).format(Date())
                } else {
                    val remoteId = giftDataRemoteSource.createGift(gift.toDto().copy(id = ""))
                    giftPhotoRemoteDataSource.uploadPhoto(photoBytes, gift.uid, remoteId)
                    remoteId
                }

                giftLocalDataSource.insertGift(
                    gift.toEntity(id, giftPhotoCache.save(id, photoBytes))
                )
                Result.success(Unit)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Result.failure(exception)
            }
        }

    override suspend fun updateGift(
        isGuestMode: Boolean,
        gift: Gift,
        photoBytes: ByteArray?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!isGuestMode) {
                giftDataRemoteSource.updateGift(gift.toDto())

                if (photoBytes != null) {
                    giftPhotoRemoteDataSource.uploadPhoto(photoBytes, gift.uid, gift.id)
                }
            }

            val photoPath = photoBytes?.let { giftPhotoCache.save(gift.id, it) }
                ?: giftLocalDataSource.getPhotoPath(gift.id).orEmpty()
            giftLocalDataSource.insertGift(gift.toEntity(gift.id, photoPath))
            Result.success(Unit)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override suspend fun updateGiftFavorite(
        isGuestMode: Boolean,
        id: String,
        isFavorite: Boolean
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!isGuestMode) {
                giftDataRemoteSource.updateGiftFavorite(id, isFavorite)
            }

            giftLocalDataSource.updateGiftIsFavorite(id, isFavorite)
            Result.success(Unit)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override suspend fun deleteGift(
        isGuestMode: Boolean,
        uid: String,
        id: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!isGuestMode) {
                giftPhotoRemoteDataSource.deletePhoto(uid, id)
                giftDataRemoteSource.deleteGift(id)
            }

            giftLocalDataSource.deleteGift(id)
            Result.success(Unit)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override suspend fun deleteGifts(
        isGuestMode: Boolean,
        uid: String,
        ids: List<String>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!isGuestMode) {
                giftPhotoRemoteDataSource.deletePhotos(uid, ids)
                giftDataRemoteSource.deleteGifts(ids)
            }

            giftLocalDataSource.deleteGifts(ids)
            Result.success(Unit)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

}
