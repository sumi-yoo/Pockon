package com.sumi.pockon.domain.repository

interface GiftPhotoRepository {
    suspend fun getPhotoPath(id: String): String?

    suspend fun getPhotoPaths(ids: List<String>): Map<String, String>
}
