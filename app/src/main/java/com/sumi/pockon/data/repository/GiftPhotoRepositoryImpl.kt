package com.sumi.pockon.data.repository

import com.sumi.pockon.data.local.gift.GiftLocalDataSource
import com.sumi.pockon.domain.repository.GiftPhotoRepository
import javax.inject.Inject

class GiftPhotoRepositoryImpl @Inject constructor(
    private val giftLocalDataSource: GiftLocalDataSource
) : GiftPhotoRepository {
    override suspend fun getPhotoPath(id: String): String? = giftLocalDataSource.getPhotoPath(id)

    override suspend fun getPhotoPaths(ids: List<String>): Map<String, String> =
        giftLocalDataSource.getPhotoPaths(ids).associate { it.id to it.photoPath }
}
