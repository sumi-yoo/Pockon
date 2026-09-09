package com.sumi.pockon.domain.usecase

import com.sumi.pockon.domain.model.BrandLocation
import com.sumi.pockon.domain.model.Gift
import com.sumi.pockon.domain.repository.BrandRepository
import com.sumi.pockon.util.getDdayInt
import javax.inject.Inject

class SearchNearbyBrandUseCase @Inject constructor(
    private val brandRepository: BrandRepository
) {
    suspend operator fun invoke(
        gifts: List<Gift>,
        longitude: Double,
        latitude: Double
    ): Result<List<Pair<Gift, BrandLocation>>> {
        val availableGifts = gifts.filter { it.usedDt.isEmpty() && getDdayInt(it.endDt) >= 0 }
        val brandNames = availableGifts.map(Gift::brand).distinct()
        if (brandNames.isEmpty()) return Result.success(emptyList())

        return brandRepository.searchNearbyBrands(longitude, latitude, brandNames).map { brandInfo ->
            availableGifts.mapNotNull { gift ->
                val nearestStore = brandInfo[gift.brand]
                    ?.mapNotNull { document -> document.distance.toIntOrNull()?.let { document to it } }
                    ?.minByOrNull { (_, distance) -> distance }
                    ?.first

                nearestStore?.let { gift to it }
            }.sortedWith(
                compareBy(
                    { (_, document) -> document.distance.toDouble() },
                    { (gift, _) -> gift.brand },
                    { (gift, _) -> gift.name },
                    { (gift, _) -> gift.endDt.ifEmpty { "99991231" } }
                )
            )
        }
    }
}
