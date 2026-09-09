package com.sumi.pockon.data.remote.gift

import com.google.firebase.firestore.PropertyName
import com.sumi.pockon.domain.model.Gift

data class GiftDto(
    var id: String = "",
    val uid: String = "",
    val name: String = "",
    val brand: String = "",
    val endDt: String = "",
    val addDt: String = "",
    val memo: String = "",
    val usedDt: String = "",
    val cash: String = "",
    @get:PropertyName("favorite") @set:PropertyName("favorite") var isFavorite: Boolean = false
)

fun Gift.toDto() = GiftDto(id, uid, name, brand, endDt, addDt, memo, usedDt, cash, isFavorite)

fun GiftDto.toDomain(photo: android.graphics.Bitmap? = null) = Gift(
    id, uid, photo, name, brand, endDt, addDt, memo, usedDt, cash, isFavorite
)
