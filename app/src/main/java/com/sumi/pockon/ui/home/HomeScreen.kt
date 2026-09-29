package com.sumi.pockon.ui.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sumi.pockon.R
import com.sumi.pockon.ui.common.NetworkErrorDialog
import com.sumi.pockon.ui.common.PockonMessageDialog
import com.sumi.pockon.ui.common.PockonTopAppBar
import com.sumi.pockon.domain.model.BrandLocation
import com.sumi.pockon.domain.model.Gift
import com.sumi.pockon.util.formatString
import com.sumi.pockon.util.getDday
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun HomeScreen(
    onAdd: () -> Unit,
    showMap: () -> Unit,
    onDetail: (String) -> Unit,
    isLoading: (Boolean) -> Unit
) {
    val homeViewModel = hiltViewModel<HomeViewModel>()
    val context = LocalContext.current
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var longitude: Double? by rememberSaveable { mutableStateOf(null) }
    var latitude: Double? by rememberSaveable { mutableStateOf(null) }
    var isShowNoInternetDialog by rememberSaveable { mutableStateOf(false) }
    var isShowLocationPermissionDialog by rememberSaveable { mutableStateOf(false) }
    var isShowLocationServiceDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        getLocation(context, fusedLocationClient)?.let { location ->
            longitude = location.longitude
            latitude = location.latitude
        }
    }

    LaunchedEffect(longitude, latitude) {
        homeViewModel.setLocation(longitude, latitude)
    }

    Column {
        HomeScreenTopBar()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 10.dp, end = 20.dp, start = 20.dp)
                    .verticalScroll(scrollState)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.txt_use_near),
                            fontSize = 18.sp,
                            softWrap = true
                        )
                        Box(modifier = Modifier.size(28.dp)) {
                            IconButton(
                                onClick = {
                                    // 권한 체크
                                    if (!homeViewModel.isNetworkConnected()) {
                                        isShowNoInternetDialog = true
                                    } else if (!checkLocationPermission(context)) {
                                        isShowLocationPermissionDialog = true
                                    } else if (!isLocationEnabled(context)) {
                                        isShowLocationServiceDialog = true
                                    } else {
                                        scope.launch {
                                            getLocation(context, fusedLocationClient)?.let { location ->
                                                longitude = location.longitude
                                                latitude = location.latitude
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .padding(0.dp)
                                    .padding(start = 5.dp)
                            ) {
                                Icon(
                                    modifier = Modifier.padding(0.dp),
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "refresh button",
                                )
                            }
                        }
                    }

                    Text(
                        text = stringResource(id = R.string.btn_open_map),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .clickable {
                                if (homeViewModel.nearGiftList.value.isNotEmpty()) {
                                    showMap()
                                }
                            },
                        softWrap = true
                    )
                }

                if (homeViewModel.nearGiftList.value.isEmpty()) {
                    EmptyNear(R.string.txt_no_near_gift)
                } else {
                    // gift item
                    LazyRow(
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .wrapContentHeight(Alignment.CenterVertically),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        items(
                            items = homeViewModel.nearGiftList.value,
                            key = { gift -> gift.first.id }
                        ) { gift ->
                            HomeGiftItem(
                                gift.first,
                                formatString(gift.first.endDt),
                                getDday(gift.first.endDt),
                                gift.second
                            ) {
                                onDetail(gift.first.id)
                            }
                        }
                    }
                }

                Box(modifier = Modifier.height(10.dp))

                // 기한 만료
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(bottom = 3.dp),
                        text = stringResource(id = R.string.txt_favorite),
                        fontSize = 18.sp,
                        textAlign = TextAlign.Start
                    )
                }

                if (homeViewModel.favoriteGiftList.value.isEmpty()) {
                    EmptyNear(R.string.txt_no_favorite_gift)
                } else {
                    // gift item
                    LazyRow(
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .wrapContentHeight(Alignment.CenterVertically),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        items(
                            items = homeViewModel.favoriteGiftList.value,
                            key = { gift -> gift.id }
                        ) { gift ->
                            HomeGiftItem(gift, formatString(gift.endDt), getDday(gift.endDt)) {
                                onDetail(gift.id)
                            }
                        }
                    }
                }
            }

            // + 추가 버튼
            SmallFloatingActionButton(
                onClick = {
                    onAdd()
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(15.dp)
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "")
            }
        }
    }

    NetworkErrorDialog(
        visible = isShowNoInternetDialog,
        onDismiss = { isShowNoInternetDialog = false }
    )

    if (isShowLocationPermissionDialog) {
        PockonMessageDialog(
            message = R.string.msg_no_location_permission,
            onConfirm = {
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                )
                isShowLocationPermissionDialog = false
            },
            onDismiss = { isShowLocationPermissionDialog = false }
        )
    }

    if (isShowLocationServiceDialog) {
        PockonMessageDialog(
            message = R.string.msg_no_location_permission,
            onConfirm = {
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                isShowLocationServiceDialog = false
            },
            onDismiss = { isShowLocationServiceDialog = false }
        )
    }

    isLoading(homeViewModel.isShowIndicator.value)
}

/** 기프티콘 각각의 카드*/
@Composable
private fun HomeGiftItem(
    gift: Gift,
    formattedEndDate: String,
    dDay: Pair<String, Boolean>,
    document: BrandLocation? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(shape = RoundedCornerShape(10.dp))
            .clickable {
                onClick()
            }
    ) {
        Card(
            modifier = Modifier
                .width(160.dp),
            shape = RoundedCornerShape(10.dp), // 모서리 둥글기
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline) // 테두리 색상과 두께 지정
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .width(160.dp)
            ) {
                val lightGray = colorResource(id = R.color.light_gray)
                AsyncImage(
                    modifier = Modifier
                        .size(160.dp)
                        .drawBehind {
                            val strokeWidth = 1.5.dp.toPx()
                            drawLine(
                                color = lightGray,
                                start = Offset(0f, size.height),
                                end = Offset(size.width, size.height),
                                strokeWidth = strokeWidth
                            )
                        },
                    model = gift.photo,
                    contentDescription = "add photo",
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier.padding(5.dp)
                ) {
                    Text(
                        text = gift.brand,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth(),
                        softWrap = true
                    )
                    Text(
                        text = gift.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .fillMaxWidth(),
                        softWrap = true
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val distance = if (document == null) {
                            ""
                        } else {
                            "${document.distance}m"
                        }
                        Text(
                            text = distance,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Start,
                            color = MaterialTheme.colorScheme.error,
                            softWrap = true
                        )
                        Text(
                            text = "~ $formattedEndDate",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            textAlign = TextAlign.End,
                            softWrap = true,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        val color = if (dDay.second || gift.usedDt.isNotEmpty()) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.tertiary
        }
        Text(
            text = if (gift.usedDt.isNotEmpty()) stringResource(id = R.string.txt_used_no_enter) else dDay.first,
            fontSize = 14.sp,
            modifier = Modifier
                .padding(2.dp)
                .align(Alignment.TopEnd)
                .background(
                    color = color,
                    shape = CardDefaults.shape
                )
                .padding(start = 15.dp, end = 15.dp, top = 5.dp, bottom = 5.dp),
            color = colorResource(id = R.color.white),
            softWrap = true
        )
    }
}

@Composable
private fun EmptyNear(msg: Int) {
    Box(
        modifier = Modifier
            .padding(top = 5.dp)
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(10.dp)
            ),
    ) {
        Column(
            modifier = Modifier
                .width(160.dp)
        ) {
            Box(modifier = Modifier.size(160.dp))

            Column(
                modifier = Modifier.padding(5.dp)
            ) {
                Text(
                    text = "",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Text(
                    text = "",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .align(Alignment.Center),
            text = stringResource(id = msg),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.outline,
            fontWeight = FontWeight.Bold,
            softWrap = true
        )
    }
}

private suspend fun getLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient
): Location? {
    val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    if (permissions.all {
            ContextCompat.checkSelfPermission(
                context,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }) {
        return try {
            fusedLocationClient.lastLocation.await()
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            null
        }
    } else {
        return null
    }
}

@Composable
private fun HomeScreenTopBar() {
    PockonTopAppBar(
        title = {
        Text(
            text = stringResource(id = R.string.home),
            fontSize = 18.sp,
        )
        }
    )
}

/** 위치 권한 체크 */
fun checkLocationPermission(context: Context): Boolean {
    val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    return permissions.all {
            ContextCompat.checkSelfPermission(
                context,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
}

/** 위치 활성화 체크 */
fun isLocationEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}
