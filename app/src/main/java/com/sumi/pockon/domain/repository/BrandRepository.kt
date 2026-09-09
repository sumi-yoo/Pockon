package com.sumi.pockon.domain.repository

import com.sumi.pockon.domain.model.BrandLocation

interface BrandRepository {
    suspend fun searchNearbyBrands(
        longitude: Double,
        latitude: Double,
        brandNames: List<String>
    ): Result<Map<String, List<BrandLocation>?>>

    suspend fun getCachedBrands(): Result<Map<String, List<BrandLocation>>>

    suspend fun clearCachedBrands(): Result<Unit>
}
