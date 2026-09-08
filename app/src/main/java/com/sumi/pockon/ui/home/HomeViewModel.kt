package com.sumi.pockon.ui.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumi.pockon.data.repository.PreferenceRepository
import com.sumi.pockon.data.model.Document
import com.sumi.pockon.data.model.Gift
import com.sumi.pockon.data.repository.AlarmRepository
import com.sumi.pockon.domain.usecase.SyncGiftListUseCase
import com.sumi.pockon.domain.usecase.SearchNearbyBrandUseCase
import com.sumi.pockon.domain.usecase.ObserveAllGiftsUseCase
import com.sumi.pockon.util.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val observeAllGiftsUseCase: ObserveAllGiftsUseCase,
    private val syncGiftListUseCase: SyncGiftListUseCase,
    private val searchNearbyBrandUseCase: SearchNearbyBrandUseCase,
    private val preferenceRepository: PreferenceRepository,
    private val alarmRepository: AlarmRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private var longitude: Double? = null
    private var latitude: Double? = null

    private val uid = preferenceRepository.getUid()
    private val isGuestMode = preferenceRepository.isGuestMode()
    private val isFirstLogin = preferenceRepository.isFirstLogin()
    private val isNotiEndDt = preferenceRepository.isNotiEndDt()

    private var giftList: List<Gift> = listOf()

    private val _nearGiftList = mutableStateOf<List<Pair<Gift, Document>>>(listOf())
    val nearGiftList: State<List<Pair<Gift, Document>>> = _nearGiftList

    private val _favoriteGiftList = mutableStateOf<List<Gift>>(listOf())
    val favoriteGiftList: State<List<Gift>> = _favoriteGiftList

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
                    preferenceRepository.saveIsFirstLogin(false)
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
                showGiftList(allGift)
            }
        }
    }

    private fun showGiftList(allGift: List<Gift>) {
        if (allGift.isNotEmpty()) {
            giftList = allGift

            giftList.forEach { gift ->
                alarmRepository.cancelAlarm(gift.id, preferenceRepository.getNotiEndDtDay())
                if (isNotiEndDt && gift.usedDt.isEmpty()) {
                    // 알림 등록
                    alarmRepository.setAlarm(gift, preferenceRepository.getNotiEndDtDay(), preferenceRepository.getNotiEndDtTime())
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
