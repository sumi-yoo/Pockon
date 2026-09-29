package com.sumi.pockon.data.local.gift

import javax.inject.Inject

class GiftLocalDataSource @Inject constructor(
    private val giftDao: GiftDao
) {

    fun insertGift(gift: GiftEntity) {
        giftDao.insertGift(gift)
    }

    fun getAllGift() = giftDao.getAllGift()

    fun getAllNotUsedGift() = giftDao.getAllNotUsedGift()

    fun getGift(id: String) = giftDao.getGift(id)

    fun getPhotoPaths(ids: List<String>) = giftDao.getPhotoPaths(ids)

    fun getPhotoPath(id: String) = giftDao.getPhotoPath(id)

    fun getGiftCountByEndDate(endDt: String) = giftDao.getGiftCountByEndDate(endDt)

    fun getAllUsedGift() = giftDao.getAllUsedGift()

    fun updateGiftIsFavorite(id: String, isFavorite: Boolean) = giftDao.updateGiftIsFavorite(id, isFavorite)

    fun deleteGift(id: String) = giftDao.deleteGift(id)

    fun deleteGifts(ids: List<String>) = giftDao.deleteGifts(ids)

    fun deleteAllGift() = giftDao.deleteAllGift()

    fun deleteAllAndInsertGifts(gifts: List<GiftEntity>) = giftDao.deleteAllAndInsertGifts(gifts)
}
