package com.sumi.pockon.ui.map

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.sumi.pockon.databinding.FragmentMapBinding
import com.sumi.pockon.data.model.Document
import com.sumi.pockon.data.model.Gift
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import kotlinx.coroutines.launch

class MapFragment : Fragment(), OnMapReadyCallback {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1000
    private lateinit var binding: FragmentMapBinding
    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private lateinit var mapViewModel: MapViewModel
    private val markerList = mutableMapOf<String, Marker>()
    private var onClick: ((List<Gift>) -> Unit)? = null

    fun setOnClickCallback(callback: (List<Gift>) -> Unit) {
        onClick = callback
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater, container, false)
        mapView = binding.mapView
        mapView.getMapAsync(this)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        mapViewModel = ViewModelProvider(requireActivity())[MapViewModel::class.java]

        return binding.root
    }

    override fun onMapReady(map: NaverMap) {
        this.naverMap = map
        // 현재 위치
        naverMap.locationSource = locationSource
        // 현재 위치 버튼 기능
        naverMap.uiSettings.isLocationButtonEnabled = false
        // 위치를 추적하면서 카메라도 따라 움직인다.
        naverMap.locationTrackingMode = LocationTrackingMode.Follow
        // 현재 위치 버튼
        binding.btnLocation.map = naverMap

        show()
    }

    private fun show() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mapViewModel.displayInfoList.collect { list ->
                    renderMarkers(list)
                }
            }
        }
    }

    private fun renderMarkers(list: List<Pair<Document, List<Gift>>>) {
        markerList.values.forEach { it.map = null }
        markerList.clear()

        list.forEach { info ->
            // 마커 찍기
            val marker = Marker()
            marker.position = LatLng(info.first.y.toDouble(), info.first.x.toDouble())
            marker.width = 70
            marker.height = 100
            marker.captionText = info.first.placeName
            marker.captionTextSize = 9F
            marker.captionRequestedWidth = 200
            marker.map = naverMap
            marker.tag = info.first
            marker.icon = MarkerIcons.BLACK
            marker.onClickListener = Overlay.OnClickListener { overlay ->
                val document = overlay.tag as Document
                markerList.forEach { (id, marker) ->
                    if (id == document.id) {
                        // 뷰페이저 셋팅
                        onClick?.invoke(info.second)

                        marker.iconTintColor = Color.RED
                        marker.width = 90
                        marker.height = 120
                    } else {
                        marker.iconTintColor = Color.parseColor("#00db77")
                        marker.width = 70
                        marker.height = 100
                    }
                }
                // 카메라 이동
                val cameraUpdate =
                    CameraUpdate.scrollTo(LatLng(document.y.toDouble(), document.x.toDouble()))
                        .animate(CameraAnimation.Easing)
                naverMap.moveCamera(cameraUpdate)
                false
            }

            // 가장 가까운 곳
            mapViewModel.getNearestDoc()?.let { nearestDoc ->
                if (nearestDoc.x == info.first.x && nearestDoc.y == info.first.y) {
                    // 초기엔 가장 가까운곳으로
                    // 뷰페이저 셋팅
                    onClick?.invoke(info.second)

                    // 카메라 이동
                    val cameraUpdate = CameraUpdate.scrollTo(
                        LatLng(
                            nearestDoc.y.toDouble(),
                            nearestDoc.x.toDouble()
                        )
                    )
                    naverMap.moveCamera(cameraUpdate)

                    marker.iconTintColor = Color.RED
                    marker.width = 90
                    marker.height = 120
                } else {
                    marker.iconTintColor = Color.parseColor("#00db77")
                }
            }
            markerList[info.first.id] = marker
        }
    }
}
