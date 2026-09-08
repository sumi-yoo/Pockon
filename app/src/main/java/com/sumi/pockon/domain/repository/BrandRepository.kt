package com.sumi.pockon.domain.repository

import com.sumi.pockon.data.model.Document

interface BrandRepository {
    suspend fun searchNearbyBrands(
        longitude: Double,
        latitude: Double,
        brandNames: List<String>
    ): Result<Map<String, List<Document>?>>
}
