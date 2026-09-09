package com.sumi.pockon.domain.usecase

import com.sumi.pockon.domain.model.BrandLocation
import com.sumi.pockon.domain.repository.BrandRepository
import javax.inject.Inject

class GetCachedBrandsUseCase @Inject constructor(
    private val brandRepository: BrandRepository
) {
    suspend operator fun invoke(): Result<Map<String, List<BrandLocation>>> =
        brandRepository.getCachedBrands()
}
