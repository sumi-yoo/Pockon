package com.sumi.pockon.data.mapper

import com.sumi.pockon.data.local.gift.GiftEntity
import com.sumi.pockon.domain.model.Gift

fun Gift.toEntity(id: String, photoPath: String) = GiftEntity(
    id = id, uid = uid, photoPath = photoPath, name = name,
    brand = brand, endDt = endDt, addDt = addDt, memo = memo, usedDt = usedDt,
    cash = cash, isFavorite = isFavorite
)

fun GiftEntity.toDomain() = Gift(
    id = id, uid = uid, name = name,
    brand = brand, endDt = endDt, addDt = addDt, memo = memo, usedDt = usedDt,
    cash = cash, isFavorite = isFavorite
)
