package com.sumi.pockon.data.mapper

import android.content.Context
import com.sumi.pockon.data.local.gift.GiftEntity
import com.sumi.pockon.domain.model.Gift
import com.sumi.pockon.util.loadImageFromPath
import com.sumi.pockon.util.saveBitmapToFile

fun Gift.toEntity(id: String, context: Context) = GiftEntity(
    id = id, uid = uid, photoPath = saveBitmapToFile(photo, context), name = name,
    brand = brand, endDt = endDt, addDt = addDt, memo = memo, usedDt = usedDt,
    cash = cash, isFavorite = isFavorite
)

fun GiftEntity.toDomain() = Gift(
    id = id, uid = uid, photo = loadImageFromPath(photoPath), name = name,
    brand = brand, endDt = endDt, addDt = addDt, memo = memo, usedDt = usedDt,
    cash = cash, isFavorite = isFavorite
)
