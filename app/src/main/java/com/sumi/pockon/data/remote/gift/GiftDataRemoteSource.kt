package com.sumi.pockon.data.remote.gift

import com.sumi.pockon.data.model.Gift
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class GiftDataRemoteSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun createGift(gift: Gift): String {
        val document = firestore
            .collection("gift")
            .document()

        document.set(gift.copy(id = document.id)).await()
        return document.id
    }

    suspend fun updateGift(gift: Gift) {
        firestore
            .collection("gift")
            .document(gift.id)
            .set(gift)
            .await()
    }

    suspend fun updateGiftFavorite(id: String, isFavorite: Boolean) {
        firestore
            .collection("gift")
            .document(id)
            .update("favorite", isFavorite)
            .await()
    }

    fun uploadData(gift: Gift, onComplete: (String?) -> Unit) {
        val document = firestore
            .collection("gift")
            .document()
        gift.id = document.id
        document
            .set(gift)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) onComplete(document.id) else onComplete(null)
            }
    }

    fun loadAllData(uid: String, onComplete: (List<Gift>) -> Unit) {
        firestore
            .collection("gift")
            .whereEqualTo("uid", uid)
            .get()
            .addOnCompleteListener { task ->
                val giftList: MutableList<Gift> = mutableListOf()
                if (task.isSuccessful) {
                    for (document in task.result) {
                        giftList.add(document.toObject(Gift::class.java))
                    }
                }
                onComplete(giftList)
            }
    }

    fun updateData(gift: Gift, onComplete: (Boolean) -> Unit) {
        firestore
            .collection("gift")
            .document(gift.id)
            .set(gift)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun updateDataIsFavorite(id: String, isFavorite: Boolean, onComplete: (Boolean) -> Unit) {
        firestore
            .collection("gift")
            .document(id)
            .update("favorite", isFavorite)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun deleteData(document: String, onComplete: (Boolean) -> Unit) {
        firestore
            .collection("gift")
            .document(document)
            .delete()
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun deleteMultipleData(documents: List<String>, onComplete: (Boolean) -> Unit) {
        val deleteTasks = documents.map { document ->
            firestore
                .collection("gift")
                .document(document)
                .delete()
        }

        // 모든 삭제 작업이 완료될 때까지 기다린 후 onComplete 호출
        Tasks.whenAllComplete(deleteTasks).addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
    }
}
