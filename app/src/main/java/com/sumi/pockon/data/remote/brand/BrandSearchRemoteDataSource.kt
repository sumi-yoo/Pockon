package com.sumi.pockon.data.remote.brand

import com.sumi.pockon.BuildConfig
import com.sumi.pockon.data.model.Brands
import javax.inject.Inject

class BrandSearchRemoteDataSource @Inject constructor(
    private val api: KaKaoSearchAPI
) {

    private val REST_API_KEY = "KakaoAK ${BuildConfig.KAKAO_REST_API_KEY}"

    suspend fun searchBrand(
        longitude: Double,
        latitude: Double,
        brandName: String
    ): Brands = api.searchBrand(
        authorization = REST_API_KEY,
        query = brandName,
        x = longitude.toString(),
        y = latitude.toString()
    )
}
