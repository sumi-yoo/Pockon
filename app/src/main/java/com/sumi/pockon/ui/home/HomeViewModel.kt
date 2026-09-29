package com.sumi.pockon.ui.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumi.pockon.domain.usecase.GetNotificationSettingsUseCase
import com.sumi.pockon.domain.usecase.GetUserSessionUseCase
import com.sumi.pockon.domain.usecase.IsInitialGiftSyncRequiredUseCase
import com.sumi.pockon.domain.usecase.MarkInitialGiftSyncCompletedUseCase
import com.sumi.pockon.domain.model.BrandLocation
import com.sumi.pockon.domain.model.Gift
import com.sumi.pockon.domain.usecase.CancelGiftAlarmUseCase
import com.sumi.pockon.domain.usecase.ScheduleGiftAlarmUseCase
import com.sumi.pockon.domain.usecase.SyncGiftListUseCase
import com.sumi.pockon.domain.usecase.SearchNearbyBrandUseCase
import com.sumi.pockon.domain.usecase.ObserveAllGiftsUseCase
import com.sumi.pockon.domain.repository.GiftPhotoRepository
import com.sumi.pockon.util.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val observeAllGiftsUseCase: ObserveAllGiftsUseCase,
    private val giftPhotoRepository: GiftPhotoRepository,
    private val syncGiftListUseCase: SyncGiftListUseCase,
    private val searchNearbyBrandUseCase: SearchNearbyBrandUseCase,
    private val getUserSessionUseCase: GetUserSessionUseCase,
    private val isInitialGiftSyncRequiredUseCase: IsInitialGiftSyncRequiredUseCase,
    private val markInitialGiftSyncCompletedUseCase: MarkInitialGiftSyncCompletedUseCase,
    private val getNotificationSettingsUseCase: GetNotificationSettingsUseCase,
    private val cancelGiftAlarmUseCase: CancelGiftAlarmUseCase,
    private val scheduleGiftAlarmUseCase: ScheduleGiftAlarmUseCase,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private var longitude: Double? = null
    private var latitude: Double? = null

    private val session = getUserSessionUseCase()
    private val uid = session.uid
    private val isGuestMode = session.isGuest
    private val isFirstLogin = isInitialGiftSyncRequiredUseCase()
    private val notificationSettings = getNotificationSettingsUseCase()

    private var giftList: List<Gift> = listOf()

    private val _nearGiftList = mutableStateOf<List<Pair<Gift, BrandLocation>>>(listOf())
    val nearGiftList: State<List<Pair<Gift, BrandLocation>>> = _nearGiftList

    private val _favoriteGiftList = mutableStateOf<List<Gift>>(listOf())
    val favoriteGiftList: State<List<Gift>> = _favoriteGiftList
    private val _photoPaths = mutableStateOf<Map<String, String>>(emptyMap())
    val photoPaths: State<Map<String, String>> = _photoPaths

    private val _isShowIndicator = mutableStateOf(false)
    val isShowIndicator: State<Boolean> = _isShowIndicator

    init {
        observeGiftList()
        getGiftList()
    }

    // 서버에서 기프티콘 리스트 가져오기
    private fun getGiftList() {
        _isShowIndicator.value = true

        if (isGuestMode || !isFirstLogin || !networkMonitor.isConnected()) {
            _isShowIndicator.value = false
            return
        } // 게스트 모드 또는 최초 로그인이 아니면 서버 안탐

        viewModelScope.launch {
            try {
                val result = syncGiftListUseCase(uid)
                if (result.isSuccess) {
                    markInitialGiftSyncCompletedUseCase()
                }
            } finally {
                _isShowIndicator.value = false
            }
        }
    }

    // 로컬 기프티콘 목록 변화 감지해서 가져오기
    private fun observeGiftList() {
        viewModelScope.launch(Dispatchers.IO) {
            observeAllGiftsUseCase().collectLatest { allGift ->
                _photoPaths.value = giftPhotoRepository.getPhotoPaths(allGift.map(Gift::id))
                showGiftList(allGift)
            }
        }
    }

    private fun showGiftList(allGift: List<Gift>) {
        if (allGift.isNotEmpty()) {
            giftList = allGift

            giftList.forEach { gift ->
                cancelGiftAlarmUseCase(gift.id, notificationSettings.daysBeforeExpiry)
                if (notificationSettings.isEnabled && gift.usedDt.isEmpty()) {
                    // 알림 등록
                    scheduleGiftAlarmUseCase(
                        gift,
                        notificationSettings.daysBeforeExpiry,
                        notificationSettings.hour to notificationSettings.minute
                    )
                }
            }

            // 즐겨찾기 기프티콘 목록
            _favoriteGiftList.value = giftList.filter { it.isFavorite }.sortedWith(
                compareBy(
                    { it.brand },
                    { it.name },
                    { it.endDt.ifEmpty { "99991231" } }
                )
            )
            getBrandInfoList() // 브랜드 검색
        } else {
            _favoriteGiftList.value = listOf()
            _nearGiftList.value = listOf()
            giftList = listOf()
        }
    }

    // 브랜드 검색 후 로컬에 저장
    private fun getBrandInfoList() {
        if (!networkMonitor.isConnected()) return

        val longitude = longitude ?: return
        val latitude = latitude ?: return

        viewModelScope.launch {
            searchNearbyBrandUseCase(giftList, longitude, latitude)
                .onSuccess { nearGifts -> _nearGiftList.value = nearGifts }
        }
    }

    fun setLocation(longitude: Double?, latitude: Double?) {
        this.longitude = longitude
        this.latitude = latitude

        if (giftList.isNotEmpty()) {
            _favoriteGiftList.value = giftList.filter { it.isFavorite }.sortedWith(
                compareBy(
                    { it.brand },
                    { it.name },
                    { it.endDt.ifEmpty { "99991231" } }
                )
            )
            getBrandInfoList()
        } else {
            _favoriteGiftList.value = listOf()
            _nearGiftList.value = listOf()
        }
    }

    fun isNetworkConnected() = networkMonitor.isConnected()
}
