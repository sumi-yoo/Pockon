package com.sumi.pockon.domain.usecase

import com.sumi.pockon.domain.repository.BrandRepository
import javax.inject.Inject

class ClearBrandCacheUseCase @Inject constructor(
    private val brandRepository: BrandRepository
) {
    suspend operator fun invoke(): Result<Unit> = brandRepository.clearCachedBrands()
}
