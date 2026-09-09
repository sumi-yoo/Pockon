package com.sumi.pockon.data.local.brand

import com.sumi.pockon.domain.model.BrandLocation

data class BrandLocationLocal(
    val id: String = "",
    val placeName: String = "",
    val distance: String = "",
    val x: String = "",
    val y: String = ""
)

fun BrandLocation.toLocal() = BrandLocationLocal(id, placeName, distance, x, y)

fun BrandLocationLocal.toDomain() = BrandLocation(id, placeName, distance, x, y)
