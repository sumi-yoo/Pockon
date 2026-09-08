package com.sumi.pockon.data.remote.brand

import com.sumi.pockon.data.model.Brands
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface KaKaoSearchAPI {

    @GET("v2/local/search/keyword.json")
    suspend fun searchBrand(
        @Header("Authorization") authorization: String,
        @Query("query") query: String,
        @Query("x") x: String? = null,
        @Query("y") y: String? = null,
        @Query("radius") radius: Int? = 10000
    ): Brands
}
