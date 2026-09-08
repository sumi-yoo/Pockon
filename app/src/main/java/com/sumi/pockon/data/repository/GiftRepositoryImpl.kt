package com.sumi.pockon.data.repository

import android.content.Context
import com.sumi.pockon.data.local.gift.GiftEntity
import com.sumi.pockon.data.local.gift.GiftLocalDataSource
import com.sumi.pockon.data.model.Gift
import com.sumi.pockon.data.remote.gift.GiftDataRemoteSource
import com.sumi.pockon.data.remote.gift.GiftPhotoRemoteDataSource
import com.sumi.pockon.domain.repository.GiftRepository
import com.sumi.pockon.util.saveBitmapToFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class GiftRepositoryImpl @Inject constructor(
    private val giftDataRemoteSource: GiftDataRemoteSource,
    private val giftPhotoRemoteDataSource: GiftPhotoRemoteDataSource,
    private val giftLocalDataSource: GiftLocalDataSource,
    @ApplicationContext private val context: Context
) : GiftRepository {

    override suspend fun syncGifts(uid: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val gifts = giftDataRemoteSource.loadGifts(uid)
            val photos = giftPhotoRemoteDataSource.downloadPhotos(
                uid = uid,
                ids = gifts.map(Gift::id)
            )
            val giftEntities = gifts.map { gift ->
                gift.copy(photo = photos[gift.id]).toEntity(gift.id, context)
            }

            giftLocalDataSource.deleteAllAndInsertGifts(giftEntities)
            Result.success(Unit)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override suspend fun addGift(isGuestMode: Boolean, gift: Gift): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val id = if (isGuestMode) {
                    SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault()).format(Date())
                } else {
                    val remoteId = giftDataRemoteSource.createGift(gift.copy(photo = null))
                    val photo = requireNotNull(gift.photo) { "Gift photo is required." }
                    giftPhotoRemoteDataSource.uploadPhoto(photo, gift.uid, remoteId)
                    remoteId
                }

                giftLocalDataSource.insertGift(gift.toEntity(id, context))
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
        shouldUploadPhoto: Boolean
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!isGuestMode) {
                giftDataRemoteSource.updateGift(gift.copy(photo = null))

                if (shouldUploadPhoto) {
                    val photo = requireNotNull(gift.photo) { "Gift photo is required." }
                    giftPhotoRemoteDataSource.uploadPhoto(photo, gift.uid, gift.id)
                }
            }

            giftLocalDataSource.insertGift(gift.toEntity(gift.id, context))
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

    private fun Gift.toEntity(id: String, context: Context) = GiftEntity(
        id = id,
        uid = uid,
        photoPath = saveBitmapToFile(photo, context),
        name = name,
        brand = brand,
        endDt = endDt,
        addDt = addDt,
        memo = memo,
        usedDt = usedDt,
        cash = cash,
        isFavorite = isFavorite
    )
}
