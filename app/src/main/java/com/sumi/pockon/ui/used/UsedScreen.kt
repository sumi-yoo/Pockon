package com.sumi.pockon.ui.used

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sumi.pockon.R
import com.sumi.pockon.domain.model.Gift
import com.sumi.pockon.ui.loading.LoadingScreen
import com.sumi.pockon.ui.detail.UsedStamp
import com.sumi.pockon.ui.common.PockonConfirmDialog
import com.sumi.pockon.util.formatString
import kotlinx.coroutines.flow.collectLatest

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun UsedScreen(onDetail: (String) -> Unit, onBack: () -> Unit) {
    val usedViewModel = hiltViewModel<UsedViewModel>()
    val uiState by usedViewModel.uiState.collectAsState()

    var showRemoveDlg by rememberSaveable { mutableStateOf(false) }
    var isEdit by rememberSaveable { mutableStateOf(false) }

    // snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        usedViewModel.events.collectLatest { event ->
            when (event) {
                UsedEvent.Deleted -> {
                    isEdit = false
                    usedViewModel.setIsAllSelect(false)
                    usedViewModel.clearCheckedGiftList()
                }
                UsedEvent.DeleteFailed -> snackbarHostState.showSnackbar(
                    context.getString(R.string.msg_no_delete)
                )
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // topbar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawWithContent {
                        drawContent()
                        drawLine(
                            color = Color.LightGray,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 2f
                        )
                    }
                    .padding(top = 10.dp, bottom = 10.dp),
            ) {
                IconButton(modifier = Modifier.align(Alignment.CenterStart), onClick = {
                    onBack()
                }) {
                    Icon(
                        Icons.AutoMirrored.Default.ArrowBack, contentDescription = "back button"
                    )
                }
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(id = R.string.title_used_gift),
                    fontSize = 16.sp,
                )
                if (uiState.giftList.isNotEmpty()) {
                    val title = if (isEdit && uiState.checkedGiftIds.isEmpty()) {
                        R.string.btn_cancel
                    } else if (isEdit && uiState.checkedGiftIds.isNotEmpty()) {
                        R.string.btn_delete
                    } else {
                        R.string.btn_edit
                    }
                    Text(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 15.dp)
                            .clickable {
                                // 삭제
                                if (isEdit) {
                                    if (uiState.checkedGiftIds.isEmpty()) isEdit =
                                        false
                                    else showRemoveDlg = true
                                } else { // 편집
                                    isEdit = true
                                    usedViewModel.setIsAllSelect(false)
                                    usedViewModel.clearCheckedGiftList()
                                }
                            },
                        text = stringResource(id = title),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // all delete
            if (isEdit) {
                Row(
                    modifier = Modifier
                        .padding(start = 10.dp, top = 8.dp)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(5.dp)
                    ) {
                        Checkbox(
                            modifier = Modifier.scale(0.8f),
                            checked = uiState.isAllSelect,
                            onCheckedChange = {
                                usedViewModel.onClickAllSelect()
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
                        text = stringResource(id = R.string.txt_no_used_gift),
                    )
                }
            }
            // gift item
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(items = uiState.giftList, key = { it.id }) { gift ->
                    UsedGiftItem(gift = gift,
                        photoPath = uiState.photoPaths[gift.id],
                        formattedEndDate = formatString(gift.endDt),
                        isEdit = isEdit,
                        isCheck = uiState.checkedGiftIds.contains(gift.id),
                        onClick = {
                            if (isEdit) usedViewModel.checkedGift(gift.id)
                            else onDetail(gift.id)
                        })
                }

            }
        }
    }

    if (showRemoveDlg) {
        PockonConfirmDialog(message = R.string.dlg_msg_delete, onConfirm = {
            showRemoveDlg = false
            usedViewModel.deleteSelection()
        }, onDismiss = {
            showRemoveDlg = false

        })
    }

    if (uiState.isLoading) {
        LoadingScreen()
    }
}


/** 기프티콘 각각의 카드*/
@Composable
private fun UsedGiftItem(
    gift: Gift, photoPath: String?, formattedEndDate: String, isEdit: Boolean, isCheck: Boolean, onClick: () -> Unit
) {
    Box(modifier = Modifier
        .clip(shape = RoundedCornerShape(10.dp))
        .fillMaxWidth()
        .wrapContentHeight()
        .clickable { onClick() }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondaryContainer),
            shape = RoundedCornerShape(10.dp), // 모서리 둥글기
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline) // 테두리 색상과 두께 지정
        ) {
            // 카드 콘텐츠
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .fillMaxWidth()
            ) {
                Box {
                    val lightGray = colorResource(id = R.color.light_gray)
                    AsyncImage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .drawBehind {
                                val strokeWidth = 1.5.dp.toPx()
                                drawLine(
                                    color = lightGray,
                                    start = Offset(0f, size.height),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = strokeWidth
                                )
                            },
                        model = photoPath,
                        contentDescription = "add photo",
                        contentScale = ContentScale.Crop
                    )
                    if (gift.usedDt.isNotEmpty()) {
                        UsedStamp(gift.usedDt)
                    }
                }
                Column(
                    modifier = Modifier.padding(5.dp)
                ) {
                    Text(
                        text = gift.brand,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = gift.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = "~ $formattedEndDate",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.End,
                            color = MaterialTheme.colorScheme.onPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 선택된 경우, 어두운 배경과 체크 아이콘
        if (isEdit && isCheck) {
            Box(
                modifier = Modifier
                    .matchParentSize()
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
