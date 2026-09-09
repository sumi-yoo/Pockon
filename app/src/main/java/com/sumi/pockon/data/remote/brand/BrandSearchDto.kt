package com.sumi.pockon.data.remote.brand

import com.google.gson.annotations.SerializedName
import com.sumi.pockon.domain.model.BrandLocation

data class BrandSearchDto(val documents: List<PlaceDocumentDto> = emptyList())

data class PlaceDocumentDto(
    val id: String = "",
    @SerializedName("place_name") val placeName: String = "",
    val distance: String = "",
    val x: String = "",
    val y: String = ""
)

fun PlaceDocumentDto.toDomain() = BrandLocation(id, placeName, distance, x, y)
