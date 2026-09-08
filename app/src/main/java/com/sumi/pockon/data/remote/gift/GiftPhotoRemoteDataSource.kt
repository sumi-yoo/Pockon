package com.sumi.pockon.data.remote.gift

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.sumi.pockon.util.toByteArray
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.StorageReference
import com.sumi.pockon.util.CryptoManager
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

    fun uploadData(data: Bitmap, uid: String, id: String, onComplete: (Boolean) -> Unit) {
        storageRef.child("${uid}/${id}.enc")
            .putBytes(data.toByteArray(uid))
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun downloadAllData(
        uid: String,
        ids: List<String>,
        onComplete: (Map<String, Bitmap?>) -> Unit
    ) {
        val key = CryptoManager.generateKeyFromUID(uid)

        val tasks = ids.map { id ->
            val fileRef = storageRef.child("${uid}/${id}.enc")
            fileRef.getBytes(Long.MAX_VALUE)
                .continueWith { task ->
                    if (task.isSuccessful) {
                        val encrypted = task.result
                        try {
                            val decrypted = CryptoManager.decrypt(encrypted, key)
                            val bitmap = BitmapFactory.decodeByteArray(decrypted, 0, decrypted.size)
                            id to bitmap
                        } catch (e: Exception) {
                            id to null
                        }
                    } else {
                        id to null
                    }
                }
        }

        Tasks.whenAllSuccess<Pair<String, Bitmap?>>(*tasks.toTypedArray())
            .addOnSuccessListener { results ->
                val resultMap = results.toMap()
                onComplete(resultMap)
            }
            .addOnFailureListener {
                onComplete(emptyMap())
            }
    }

    fun removeData(uid: String, id: String, onComplete: (Boolean) -> Unit) {
        storageRef.child("${uid}/${id}.enc")
            .delete()
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun removeMultipleData(uid: String, ids: List<String>, onComplete: (Boolean) -> Unit) {
        val deleteTasks = ids.map { id ->
            storageRef.child("${uid}/${id}.enc")
                .delete()
        }

        // 모든 삭제 작업이 완료될 때까지 기다린 후 onComplete 호출
        Tasks.whenAllComplete(deleteTasks).addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
    }
}
