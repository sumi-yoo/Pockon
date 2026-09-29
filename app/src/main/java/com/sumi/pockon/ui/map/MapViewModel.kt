package com.sumi.pockon.ui.map

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.sumi.pockon.domain.model.BrandLocation
import com.sumi.pockon.domain.model.Gift
import com.sumi.pockon.domain.usecase.GetCachedBrandsUseCase
import com.sumi.pockon.domain.usecase.ObserveAvailableGiftsUseCase
import com.sumi.pockon.domain.repository.GiftPhotoRepository
import com.sumi.pockon.util.getDdayInt
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getCachedBrandsUseCase: GetCachedBrandsUseCase,
    private val observeAvailableGiftsUseCase: ObserveAvailableGiftsUseCase,
    private val giftPhotoRepository: GiftPhotoRepository
) : ViewModel() {

    private val _displayInfoList = MutableStateFlow<List<Pair<BrandLocation, List<Gift>>>>(emptyList())
    val displayInfoList: StateFlow<List<Pair<BrandLocation, List<Gift>>>> = _displayInfoList.asStateFlow()
    private val _photoPaths = MutableStateFlow<Map<String, String>>(emptyMap())
    val photoPaths: StateFlow<Map<String, String>> = _photoPaths.asStateFlow()
    private val _cameraPosition = mutableStateOf<CameraPosition?>(null)
    val cameraPosition: State<CameraPosition?> = _cameraPosition

    private val _currentLocation = mutableStateOf<LatLng?>(null)
    val currentLocation: State<LatLng?> = _currentLocation
    // 선택된 마커 index
    private val _selectedMarkerIndex = mutableStateOf<Int?>(null)
    val selectedMarkerIndex: State<Int?> = _selectedMarkerIndex

    private var giftList = listOf<Gift>()
    private var brandInfoList: Map<String, List<BrandLocation>> = emptyMap()
    private var nearestDoc: BrandLocation? = null
    private var pageIndex = 0
    private var isInitialCameraMoved = false

    init {
        observeGiftList()
    }

    // 로컬 기프티콘 목록 변화 감지해서 가져오기
    private fun observeGiftList() {
        viewModelScope.launch(Dispatchers.IO) {
            observeAvailableGiftsUseCase().collectLatest { allGift ->
                giftList = allGift
                _photoPaths.value = giftPhotoRepository.getPhotoPaths(allGift.map(Gift::id))
                if (allGift.isNotEmpty()) {
                    getAllBrands()
                } else {
                    nearestDoc = null
                    _displayInfoList.value = emptyList()
                }
            }
        }
    }

    private suspend fun getAllBrands() {
        getCachedBrandsUseCase().onSuccess { brands ->
            brandInfoList = brands
            mappingInfo()
        }
    }

    // 브랜드별 사용 가능 기프티콘 매핑하기
    private fun mappingInfo() {
        val mappingList = mutableMapOf<BrandLocation, MutableSet<String>>()
        nearestDoc = null

        brandInfoList.forEach { (keyword, documents) ->
            documents.forEach { document ->
                // 가장 가까운곳 뽑아내기
                val currentNearest = nearestDoc
                if (currentNearest == null || currentNearest.distance.toDouble() > document.distance.toDouble()) {
                    nearestDoc = document
                }

                if (mappingList.keys.contains(document)) mappingList[document]?.add(keyword)
                else mappingList[document] = mutableSetOf(keyword)
            }
        }

        val markerInGiftList = ArrayList<Pair<BrandLocation, List<Gift>>>()
        mappingList.forEach { (document, keywordList) ->
            val filterGiftList = giftList.filter { keywordList.contains(it.brand) && getDdayInt(it.endDt) >= 0 }
            val sortedList = filterGiftList.sortedWith(
                compareBy(
                    { it.brand },     // 브랜드명 순
                    { it.name },      // 상품명 순
                    { it.endDt.ifEmpty { "99991231" } }
                )
            )
            markerInGiftList.add(Pair(document, sortedList))
        }

        nearestDoc?.let { doc ->
            val index = markerInGiftList.indexOfFirst { it.first.id == doc.id }
            selectMarker(index)
        }

        _displayInfoList.value = markerInGiftList
    }

    fun getNearestDoc() = this.nearestDoc

    fun updateCameraPosition(position: CameraPosition) {
        _cameraPosition.value = position
    }

    fun updateCurrentLocation(location: LatLng) {
        _currentLocation.value = location
    }

    fun selectMarker(index: Int) {
        _selectedMarkerIndex.value = index
    }

    fun getIsInitialCameraMoved() = this.isInitialCameraMoved

    fun setIsInitialCameraMoved(isInitialCameraMoved: Boolean) {
        this.isInitialCameraMoved = isInitialCameraMoved
    }

    fun setPageIndex(pageIndex: Int) {
        this.pageIndex = pageIndex
    }

    fun getPageIndex() = this.pageIndex
}
