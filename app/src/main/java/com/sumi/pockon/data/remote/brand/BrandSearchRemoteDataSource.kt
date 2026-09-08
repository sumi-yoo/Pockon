package com.sumi.pockon.data.remote.brand

import com.sumi.pockon.BuildConfig
import com.sumi.pockon.data.model.Brands
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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

    fun getBrandInfo(
        longitude: Double,
        latitude: Double,
        brandName: String,
        onComplete: (String, Brands?) -> Unit
    ) {
//        val call = api.searchBrand(
//            REST_API_KEY,
//            brandName,
//            x = longitude.toString(),
//            y = latitude.toString()
//        )
//
//        call.enqueue(object : Callback<Brands> {
//            override fun onResponse(call: Call<Brands>, response: Response<Brands>) {
//                if (response.isSuccessful && response.body() != null) onComplete(
//                    brandName,
//                    response.body()
//                )
//                else onComplete(brandName, null)
//            }
//
//            override fun onFailure(call: Call<Brands>, t: Throwable) {
//                t.printStackTrace()
//                onComplete(brandName, null)
//            }
//        })
    }
}
