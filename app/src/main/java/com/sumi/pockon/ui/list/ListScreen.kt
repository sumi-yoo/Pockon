package com.sumi.pockon.ui.list

import android.annotation.SuppressLint
import androidx.appcompat.app.AlertDialog
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import com.sumi.pockon.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sumi.pockon.data.model.Gift
import com.sumi.pockon.util.formatString
import com.sumi.pockon.util.getDday
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.text.style.TextOverflow

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ListScreen(onDetail: (String) -> Unit, onAdd: () -> Unit, isLoading: (Boolean) -> Unit) {
    val listViewModel = hiltViewModel<ListViewModel>()
    val uiState by listViewModel.uiState.collectAsState()

    var showRemoveDlg by rememberSaveable { mutableStateOf(false) }
    var isEdit by rememberSaveable { mutableStateOf(false) }

    // snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        listViewModel.events.collectLatest { event ->
            when (event) {
                ListEvent.GiftUseFailed -> snackbarHostState.showSnackbar(
                    context.getString(R.string.msg_no_use)
                )
                is ListEvent.GiftDeleted -> {
                    isLoading(false)
                    if (event.isBulk) {
                        isEdit = false
                        listViewModel.setIsAllSelect(false)
                        listViewModel.clearCheckedGiftList()
                    }
                }
                ListEvent.GiftDeleteFailed -> {
                    isLoading(false)
                    snackbarHostState.showSnackbar(context.getString(R.string.msg_no_delete))
                }
            }
        }
    }

    var isRefreshing by remember { mutableStateOf(false) }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)

    val listState = rememberLazyListState()
    val chipState = rememberLazyListState()

    // LaunchedEffect를 사용하여 새로 고침 처리
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            listViewModel.refreshGiftList()
            listViewModel.setTopTitle(R.string.top_app_bar_recent)
            isRefreshing = false
            isEdit = false
            listViewModel.clearCheckedGiftList()
        }
    }

    LaunchedEffect(uiState.isScrollTop) {
        if (uiState.isScrollTop) {
            if (listViewModel.getFilterList().isEmpty()) {
                chipState.scrollToItem(0)
            }
            listState.scrollToItem(0)
            listViewModel.toggleIsScrollTop()
        }
    }

    if (showRemoveDlg) {
        ConfirmDialog(
            text = R.string.dlg_msg_delete,
            onConfirm = {
                showRemoveDlg = false
                isLoading(true)
                if (isEdit) {
                    listViewModel.deleteSelection()
                } else {
                    listViewModel.removeGift()
                }
            },
            onDismiss = {
                showRemoveDlg = false

            }
        )
    }

    // NoInternetDialog
    if (uiState.isShowNoInternetDialog) {
        AlertDialog.Builder(context)
            .setTitle(stringResource(id = R.string.txt_alert))
            .setMessage(stringResource(id = R.string.msg_no_internet))
            .setPositiveButton(stringResource(id = R.string.btn_confirm)) { dialog, which ->
                listViewModel.changeNoInternetDialogState()
            }
            .show()
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { _ ->
        val title = if (isEdit && uiState.checkedGiftIds.isEmpty()) {
            R.string.btn_cancel
        } else if (isEdit && uiState.checkedGiftIds.isNotEmpty()) {
            R.string.btn_delete
        } else {
            R.string.btn_edit
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column {
                ListScreenTopBar(
                    title = uiState.topTitle,
                    actionText = title,
                    onDropDown = {
                        listViewModel.setTopTitle(it)
                        listViewModel.orderBy()
                        listViewModel.toggleIsScrollTop()
                    },
                    onClick = {
                        if (uiState.giftList.isEmpty()) return@ListScreenTopBar
                        // 삭제
                        if (isEdit) {
                            if (uiState.checkedGiftIds.isEmpty()) isEdit =
                                false
                            else showRemoveDlg = true
                        } else { // 편집
                            isEdit = true
                            listViewModel.setIsAllSelect(false)
                            listViewModel.clearCheckedGiftList()
                        }
                    }
                )

                // 기프티콘 목록
                // SwipeRefresh로 새로 고침 기능 구현
                SwipeRefresh(
                    state = swipeRefreshState,
                    onRefresh = { isRefreshing = true }, // 새로 고침 시작
                    modifier = Modifier.fillMaxSize(),
                    refreshTriggerDistance = 100.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        // empty screen
                        if (uiState.giftList.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.outline,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    text = stringResource(id = R.string.txt_no_gift),
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier.padding(end = 20.dp, start = 20.dp, top = 10.dp)
                            ) {
                                // chip
                                LazyRow(
                                    state = chipState
                                ) {
                                    items(
                                        items = uiState.chipElement.keys.toList(),
                                        key = { chip -> chip }
                                    ) { key ->
                                            FilterChip(
                                                onClick = {
                                                    listViewModel.setIsAllSelect(false)
                                                    listViewModel.changeChipState(listOf(key))
                                                    scope.launch {
                                                        listState.scrollToItem(0)
                                                    }
                                                },
                                                label = {
                                                    if (key == "") {
                                                        Text(
                                                            color = Color.White,
                                                            text = stringResource(id = R.string.chip_all),
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    } else {
                                                        Text(
                                                            color = Color.White,
                                                            text = key,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                },
                                                selected = uiState.chipElement[key] ?: false,
                                                shape = RoundedCornerShape(50.dp),
                                                colors = FilterChipDefaults.filterChipColors().copy(
                                                    containerColor = MaterialTheme.colorScheme.primary,
                                                    selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer
                                                ),
                                                border = null
                                            )
                                            Spacer(modifier = Modifier.padding(3.dp))
                                    }
                                }

                                // all delete
                                if (isEdit) {
                                    Row(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .padding(bottom = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier.size(5.dp)
                                        ) {
                                            Checkbox(
                                                modifier = Modifier
                                                    .scale(0.8f),
                                                checked = uiState.isAllSelect,
                                                onCheckedChange = {
                                                    listViewModel.onClickAllSelect()
                                                },
                                                colors = CheckboxDefaults.colors(
                                                    uncheckedColor = MaterialTheme.colorScheme.onPrimary,
                                                    checkedColor = MaterialTheme.colorScheme.primaryContainer,  // 체크된 상태에서 배경 색상 (체크박스 색상)
                                                    checkmarkColor = MaterialTheme.colorScheme.background,  // 체크 표시 색상
                                                )
                                            )
                                        }
                                        Text(
                                            modifier = Modifier.padding(start = 10.dp),
                                            text = stringResource(id = R.string.txt_all_select)
                                        )
                                    }
                                }

                                // gift item
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = 10.dp)
                                ) {
                                    items(
                                        items = uiState.copyGiftList,
                                        key = { gift -> gift.id }
                                    ) { gift ->
                                        SwipeToDismissItem(
                                            onDismiss = { offsetX ->
                                                if (offsetX < 0) { // 사용완료
                                                    listViewModel.usedGift(gift)
                                                } else { // 삭제
                                                    listViewModel.setRemoveGift(gift)
                                                    showRemoveDlg = true
                                                }
                                            }
                                        ) {
                                            GiftItem(
                                                isEdit = isEdit,
                                                gift = gift,
                                                formattedEndDate = formatString(gift.endDt),
                                                dDay = getDday(gift.endDt),
                                                isCheck = uiState.checkedGiftIds.contains(
                                                    gift.id
                                                ),
                                                onClick = {
                                                    if (isEdit) listViewModel.checkedGift(gift.id)
                                                    else onDetail(gift.id)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            SmallFloatingActionButton(
                onClick = {
                    onAdd()
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(15.dp)
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "")
            }
        }
    }
}

@Composable
fun GiftItem(
    isEdit: Boolean,
    gift: Gift,
    formattedEndDate: String,
    dDay: Pair<String, Boolean>,
    isCheck: Boolean,
    onClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape = RoundedCornerShape(10.dp))
            .clickable {
                onClick()
            }
    ) {
        Card(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.secondaryContainer),
            shape = RoundedCornerShape(10.dp), // 모서리 둥글기
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline) // 테두리 색상과 두께 지정
        ) {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    val lightGray = colorResource(id = R.color.light_gray)
                    AsyncImage(
                        modifier = Modifier.fillMaxSize()
                            .drawBehind {
                                val strokeWidth = 1.5.dp.toPx()
                                drawLine(
                                    color = lightGray,
                                    start = Offset(size.width, 0f),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = strokeWidth
                                )
                            },
                        model = gift.photo,
                        contentDescription = "photo",
                        contentScale = ContentScale.Crop
                    )
                }

                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                        .verticalScroll(scrollState)
                        .nestedScroll(rememberNestedScrollInteropConnection())
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = gift.brand,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodyMedium,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        text = gift.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right,
                        text = "~ $formattedEndDate",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        val color = if (dDay.second) {
            MaterialTheme.colorScheme.outline
        } else {
            MaterialTheme.colorScheme.tertiary
        }
        Text(
            text = dDay.first,
            fontSize = 14.sp,
            modifier = Modifier
                .padding(2.dp)
                .align(Alignment.TopEnd)
                .background(
                    color = color,
                    shape = CardDefaults.shape
                )
                .padding(start = 15.dp, end = 15.dp, top = 5.dp, bottom = 5.dp),
            color = colorResource(id = R.color.white)
        )

        // selected color
        if (isEdit && isCheck) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.3f))
            ) {
                CompositionLocalProvider(LocalContentColor provides Color.White) {
                    Icon(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(30.dp),
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "check"
                    )
                }
            }
        }
    }
}

@Composable
fun TopAppBarDropDownMenu(topTitle: Int, setTopTitle: (Int) -> Unit) {
    val expanded = remember {
        mutableStateOf(false)
    }

    Row(
        modifier = Modifier.clickable {
            expanded.value = true
        },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = topTitle),
            fontSize = 20.sp
        )
        Icon(
            Icons.Filled.KeyboardArrowDown,
            contentDescription = "More Filter"
        )
    }

    DropdownMenu(
        expanded = expanded.value,
        onDismissRequest = { expanded.value = false },
        modifier = Modifier.background(MaterialTheme.colorScheme.secondary),
    ) {
        val recentTitle = R.string.top_app_bar_recent
        DropdownMenuItem(
            onClick = {
                expanded.value = false
                setTopTitle(recentTitle)
            },
            text = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = stringResource(id = recentTitle),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.background)

        val dDayTitle = R.string.top_app_bar_end_date
        DropdownMenuItem(
            onClick = {
                expanded.value = false
                setTopTitle(dDayTitle)
            },
            text = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = stringResource(id = dDayTitle),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.background)

        val alphabetTitle = R.string.top_app_bar_alphabet
        DropdownMenuItem(
            onClick = {
                expanded.value = false
                setTopTitle(alphabetTitle)
            },
            text = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = stringResource(id = alphabetTitle),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        )
    }
}

// 확인 다이얼로그
@Composable
fun ConfirmDialog(text: Int, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = {}) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.background,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline) // 테두리 색상과 두께 지정
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // title
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "info",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(10.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(color = MaterialTheme.colorScheme.outline)
                )

                // text
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    text = stringResource(id = text),
                    fontSize = 16.sp
                )

                // bottom button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(color = MaterialTheme.colorScheme.outline)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    Button(
                        onClick = { onConfirm() },
                        shape = RectangleShape,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.outline,
                            disabledContentColor = MaterialTheme.colorScheme.background
                        ),
                    ) {
                        Text(
                            text = stringResource(id = R.string.btn_confirm),
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                            .background(color = MaterialTheme.colorScheme.outline)
                    )

                    Button(
                        onClick = { onDismiss() },
                        shape = RectangleShape,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.error,
                            disabledContainerColor = MaterialTheme.colorScheme.outline,
                            disabledContentColor = MaterialTheme.colorScheme.background
                        )
                    ) {
                        Text(
                            text = stringResource(id = R.string.btn_cancel),
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ListScreenTopBar(title: Int, actionText: Int, onDropDown: (Int) -> Unit, onClick: () -> Unit) {
    // topbar
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(10.dp)
            .padding(top = 10.dp, bottom = 10.dp)
    ) {
        Box(modifier = Modifier.align(Alignment.Center)) {
            TopAppBarDropDownMenu(title) { title ->
                onDropDown(title)
            }
        }
        Text(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 15.dp)
                .clickable {
                    onClick()
                },
            text = stringResource(id = actionText),
            fontSize = 14.sp
        )
    }
}

@Composable
fun SwipeToDismissItem(onDismiss: (Float) -> Unit, content: @Composable () -> Unit) {
    val animatableOffsetX = remember { Animatable(0f) } // 애니메이션을 위한 Animatable
    val screenWidth =
        LocalDensity.current.run { LocalConfiguration.current.screenWidthDp.dp.toPx() } // 화면의 너비 계산
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .height(120.dp)
            .fillMaxWidth()
            .padding(top = 3.dp, bottom = 3.dp)
            .clip(shape = RoundedCornerShape(10.dp))
            .background(
                color = if (animatableOffsetX.value < 0) colorResource(id = R.color.dark_green) else Color.Red.copy(
                    alpha = 0.8f
                ),
                shape = RoundedCornerShape(10.dp)
            )
    ) {
        if (animatableOffsetX.value < 0) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(start = 15.dp, end = 15.dp),
                color = Color.White,
                text = stringResource(id = R.string.txt_used),
                fontSize = 18.sp
            )
        } else {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 15.dp, end = 15.dp),
                color = Color.White,
                text = stringResource(id = R.string.txt_delete),
                fontSize = 18.sp
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(animatableOffsetX.value.toInt(), 0) } // 애니메이션을 적용하여 스와이프 효과
                .pointerInput(Unit) {
                    // 드래그 종료 시점 감지
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            // 원위치로 돌아가기
                            // 손을 떴을 때 원위치로 돌아가게
                            scope.launch {
                                animatableOffsetX.animateTo(
                                    targetValue = 0f, // 원위치로 돌아가기
                                    animationSpec = tween(durationMillis = 300)
                                )
                            }
                            if (animatableOffsetX.value < -screenWidth / 2 || animatableOffsetX.value > screenWidth / 2) {
                                // 화면 너비의 반 이상 밀었으면 삭제
                                onDismiss(animatableOffsetX.value) // 삭제 처리
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            scope.launch {
                                animatableOffsetX.snapTo(animatableOffsetX.value + dragAmount) // 드래그 양만큼 offsetX 업데이트
                            }
                        }
                    )
                }
        ) {
            content()
        }
    }
}
