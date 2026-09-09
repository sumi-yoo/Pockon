package com.sumi.pockon.data.repository

import com.sumi.pockon.data.local.brand.BrandLocalDataSource
import com.sumi.pockon.data.local.brand.toDomain
import com.sumi.pockon.data.local.brand.toLocal
import com.sumi.pockon.data.remote.brand.BrandSearchRemoteDataSource
import com.sumi.pockon.data.remote.brand.toDomain
import com.sumi.pockon.domain.model.BrandLocation
import com.sumi.pockon.domain.repository.BrandRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BrandRepositoryImpl @Inject constructor(
    private val remoteDataSource: BrandSearchRemoteDataSource,
    private val localDataSource: BrandLocalDataSource
) : BrandRepository {

    override suspend fun searchNearbyBrands(
        longitude: Double,
        latitude: Double,
        brandNames: List<String>
    ): Result<Map<String, List<BrandLocation>?>> = withContext(Dispatchers.IO) {
        try {
            val brandInfo = coroutineScope {
                brandNames.map { brandName ->
                    async {
                        brandName to try {
                            remoteDataSource.searchBrand(longitude, latitude, brandName).documents.map { it.toDomain() }
                        } catch (exception: CancellationException) {
                            throw exception
                        } catch (exception: Exception) {
                            null
                        }
                    }
                }.awaitAll().toMap()
            }

            localDataSource.deleteAllBrands()
            brandInfo.forEach { (keyword, documents) ->
                documents?.let { localDataSource.insertBrands(keyword, it.map { location -> location.toLocal() }) }
            }

            Result.success(brandInfo)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override suspend fun getCachedBrands(): Result<Map<String, List<BrandLocation>>> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(
                    localDataSource.getAllBrands().associate { brand ->
                        brand.keyword to brand.documents.map { it.toDomain() }
                    }
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Result.failure(exception)
            }
        }

    override suspend fun clearCachedBrands(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            localDataSource.deleteAllBrands()
            Result.success(Unit)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}
