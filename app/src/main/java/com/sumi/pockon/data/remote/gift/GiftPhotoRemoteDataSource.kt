package com.sumi.pockon.data.remote.gift

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.sumi.pockon.util.toByteArray
import com.google.firebase.storage.StorageReference
import com.sumi.pockon.util.CryptoManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class GiftPhotoRemoteDataSource @Inject constructor(
    private val storageRef: StorageReference
) {

    suspend fun uploadPhoto(data: Bitmap, uid: String, id: String) {
        storageRef.child("$uid/$id.enc")
            .putBytes(data.toByteArray(uid))
            .await()
    }

    suspend fun downloadPhotos(uid: String, ids: List<String>): Map<String, Bitmap?> = coroutineScope {
        val key = CryptoManager.generateKeyFromUID(uid)

        ids.map { id ->
            async {
                val photo = try {
                    val encrypted = storageRef.child("$uid/$id.enc")
                        .getBytes(Long.MAX_VALUE)
                        .await()
                    val decrypted = CryptoManager.decrypt(encrypted, key)
                    BitmapFactory.decodeByteArray(decrypted, 0, decrypted.size)
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    null
                }

                id to photo
            }
        }.awaitAll().toMap()
    }

    suspend fun deletePhoto(uid: String, id: String) {
        storageRef.child("$uid/$id.enc").delete().await()
    }

    suspend fun deletePhotos(uid: String, ids: List<String>) = coroutineScope {
        ids.map { id -> async { deletePhoto(uid, id) } }.awaitAll()
    }
}
