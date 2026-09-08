package com.sumi.pockon.data.remote.gift

import com.sumi.pockon.data.model.Gift
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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

    suspend fun loadGifts(uid: String): List<Gift> = firestore
        .collection("gift")
        .whereEqualTo("uid", uid)
        .get()
        .await()
        .documents
        .mapNotNull { document -> document.toObject(Gift::class.java) }

    suspend fun deleteGift(id: String) {
        firestore.collection("gift").document(id).delete().await()
    }

    suspend fun deleteGifts(ids: List<String>) = coroutineScope {
        ids.map { id -> async { deleteGift(id) } }.awaitAll()
    }
}
