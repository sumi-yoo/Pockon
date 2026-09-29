package com.sumi.pockon.ui.detail

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.sumi.pockon.util.DateTransformation
import com.sumi.pockon.R
import com.sumi.pockon.ui.loading.LoadingScreen
import com.sumi.pockon.ui.add.CustomDatePickerDialog
import com.sumi.pockon.ui.common.PockonConfirmDialog
import com.sumi.pockon.ui.common.GiftField
import com.sumi.pockon.ui.common.NetworkErrorDialog
import com.sumi.pockon.util.decimalFormat
import com.sumi.pockon.util.getBitmapFromUri
import com.sumi.pockon.util.thousandSeparatorTransformation
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DetailScreen(id: String, isEditMode: Boolean = true, onBack: () -> Unit) {
    val detailViewModel = hiltViewModel<DetailViewModel>()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        detailViewModel.events.collectLatest { event ->
            val message = when (event) {
                DetailEvent.GiftUpdated -> R.string.msg_ok_update
                DetailEvent.GiftUpdateFailed -> R.string.msg_no_update
                is DetailEvent.GiftUsageUpdateFailed -> {
                    if (event.isUsing) R.string.msg_no_use else R.string.msg_no_use_cancel
                }
            }
            snackbarHostState.showSnackbar(context.getString(message))
        }
    }

    // 중복호출 방지
    LaunchedEffect(id) {
        if (!detailViewModel.isEdit.value) detailViewModel.getGift(id)
    }

    // scroll
    val scrollSate = rememberScrollState()

    // input data
    val inputDataList = listOf(
        detailViewModel.name.value,
        detailViewModel.brand.value,
        detailViewModel.cash.value,
        detailViewModel.endDate.value,
        detailViewModel.memo.value
    )

    // select photo
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            detailViewModel.setPhoto(getBitmapFromUri(context.contentResolver, it))
        }
    }

    BackHandler {
        if (detailViewModel.isShowBottomSheet.value) {
            detailViewModel.setIsShowBottomSheet(false)
        } else {
            onBack()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = Modifier
            .fillMaxSize()
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // topbar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 10.dp)
            ) {
                IconButton(
                    modifier = Modifier.align(Alignment.CenterStart),
                    onClick = {
                        onBack()
                    }
                ) {
                    Icon(
                        Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "back button"
                    )
                }
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(id = R.string.title_detail_gift),
                    fontSize = 16.sp,
                )
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    IconButton(
                        onClick = {
                            detailViewModel.toggleFavorite()
                        }
                    ) {
                        Icon(
                            if (detailViewModel.isFavorite.value) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = "Star"
                        )

                    }
                    if (detailViewModel.gift.value.usedDt.isEmpty() && isEditMode) {
                        IconButton(
                            onClick = {
                                detailViewModel.setIsEdit(true)
                            }
                        ) {
                            if (!detailViewModel.isEdit.value) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit"
                                )
                            } else {
                                Text(
                                    modifier = Modifier.clickable {
                                        detailViewModel.setIsEdit(false)
                                        detailViewModel.init()
                                    },
                                    text = stringResource(id = R.string.btn_cancel),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 5.dp, bottom = 5.dp, start = 25.dp, end = 25.dp)
                    .verticalScroll(scrollSate)
            ) {
                // gift image
                GiftImage(
                    detailViewModel.isEdit.value,
                    detailViewModel.photo.value,
                    detailViewModel.photoPath.value,
                    detailViewModel.usedDt.value
                ) {
                    if (detailViewModel.isEdit.value) {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    } else if (detailViewModel.usedDt.value.isEmpty()) {
                        detailViewModel.setIsShowBottomSheet(true)
                    } else {
                        detailViewModel.setIsShowCancelDialog(true)
                    }
                }
                if (detailViewModel.isEdit.value) {
                    // cash
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(0.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                modifier = Modifier
                                    .fillMaxHeight(),
                                text = stringResource(R.string.txt_cash_certificate),
                                textAlign = TextAlign.Center
                            )
                            Checkbox(
                                modifier = Modifier
                                    .scale(0.8f)
                                    .padding(0.dp),
                                checked = detailViewModel.isCheckedCash.value,
                                onCheckedChange = {
                                    detailViewModel.chgCheckedCash()
                                },
                                colors = CheckboxDefaults.colors(
                                    uncheckedColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedColor = MaterialTheme.colorScheme.primaryContainer,
                                    checkmarkColor = MaterialTheme.colorScheme.background
                                )
                            )
                        }
                    }
                }

                // text field
                for (field in GiftField.entries) {
                    if (detailViewModel.isEdit.value && field == GiftField.Amount && !detailViewModel.isCheckedCash.value) continue
                    if (!detailViewModel.isEdit.value && field == GiftField.Amount && detailViewModel.cash.value.isEmpty()) continue
                    InputDataTextField(
                        value = inputDataList[field.index],
                        label = detailViewModel.getLabelList(field.index),
                        field = field,
                        detailViewModel.isEdit.value,
                        onValueChange = { changedField, value ->
                            detailViewModel.setGift(changedField.index, value)
                        },
                        onDatePicker = {
                            if (field == GiftField.ExpiryDate) {
                                detailViewModel.changeDatePickerState()
                            }
                        }
                    )
                }
            }
            // use or cancel button
            Button(
                onClick = {
                    if (detailViewModel.isEdit.value) {
                        val msg = detailViewModel.isValid()
                        if (msg != null) {
                            scope.launch {
                                snackbarHostState.showSnackbar(message = context.getString(msg))
                            }
                        } else {
                            detailViewModel.updateGift()

                        }
                    } else if (detailViewModel.usedDt.value.isEmpty()) {
                        detailViewModel.setIsShowBottomSheet(true)
                    } else {
                        detailViewModel.setIsShowCancelDialog(true)
                    }
                },
                shape = RectangleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 25.dp, end = 25.dp, bottom = 15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!detailViewModel.isEdit.value && detailViewModel.usedDt.value.isNotEmpty()) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            ) {
                if (detailViewModel.isEdit.value) {
                    Text(
                        color = MaterialTheme.colorScheme.onPrimary,
                        text = stringResource(id = R.string.btn_save)
                    )
                } else if (detailViewModel.usedDt.value.isEmpty()) {
                    Text(
                        color = MaterialTheme.colorScheme.onPrimary,
                        text = stringResource(id = R.string.btn_use)
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.btn_use_cancel),
                        color = colorResource(id = R.color.white),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (detailViewModel.isShowBottomSheet.value) {
            GiftBottomSheet(
                image = detailViewModel.photo.value ?: detailViewModel.photoPath.value,
                isVisible = detailViewModel.isShowBottomSheet.value
            ) { isUsed ->
                if (isUsed) {
                    if (detailViewModel.cash.value.isNotEmpty()) {
                        detailViewModel.setIsShowUseCashDialog(true)
                        detailViewModel.setIsShowBottomSheet(false)
                    } else {
                        detailViewModel.setIsUsed(true)
                    }
                } else {
                    detailViewModel.setIsShowBottomSheet(false)
                }
            }
        }
        if (detailViewModel.isShowCancelDialog.value) {
            PockonConfirmDialog(
                message = R.string.dlg_msg_use_cancel,
                onConfirm = {
                    detailViewModel.setIsUsed(false)
                    detailViewModel.setIsShowCancelDialog(false)
                },
                onDismiss = {
                    detailViewModel.setIsShowCancelDialog(false)
                }
            )
        }

        // 금액권 사용금액 입력 다이얼로그
        if (detailViewModel.isShowUseCashDialog.value) {
            Dialog(onDismissRequest = {}) {
                Surface(
                    modifier = Modifier
                        .wrapContentWidth()
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White
                ) {
                    UseCashDialog(
                        remainCash = detailViewModel.cash.value,
                        onCancel = {
                            detailViewModel.setIsShowUseCashDialog(false)
                        },
                        onConfirm = { useCash ->
                            detailViewModel.setIsUsed(true, useCash)
                        }
                    )
                }
            }
        }

        // DatePicker
        if (detailViewModel.isShowDatePicker.value) {
            CustomDatePickerDialog(
                dateString = detailViewModel.endDate.value,
                onCancel = { detailViewModel.changeDatePickerState() },
                onConfirm = {
                    detailViewModel.changeDatePickerState()
                    detailViewModel.setGift(GiftField.ExpiryDate.index, value = it)
                }
            )
        }

        NetworkErrorDialog(
            visible = detailViewModel.isShowNoInternetDialog.value,
            onDismiss = detailViewModel::changeNoInternetDialogState
        )
    }

    if (detailViewModel.isShowIndicator.value) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                LoadingScreen()
            }
        }
    }

}

@Composable
fun InputDataTextField(
    value: String,
    label: Int,
    field: GiftField,
    isEdit: Boolean,
    onValueChange: (GiftField, String) -> Unit,
    onDatePicker: () -> Unit
) {
    var modifier = Modifier
        .fillMaxWidth()
        .padding(end = 0.dp, start = 0.dp, bottom = 5.dp, top = 0.dp)
    if (field == GiftField.Memo) {
        modifier = modifier.height(170.dp)
    }

    OutlinedTextField(
        readOnly = !isEdit,
        modifier = modifier,
        value = value,
        textStyle = TextStyle(MaterialTheme.colorScheme.onPrimary),
        onValueChange = {
            if (field == GiftField.Name && it.length > 100 || (field == GiftField.Brand && it.length > 50)) return@OutlinedTextField
            if (field == GiftField.Memo && it.length > 300) return@OutlinedTextField
            if (((it.length > 9 || it == "00") && field == GiftField.Amount) || (it.length > 8 && field == GiftField.ExpiryDate)) return@OutlinedTextField
            // 공백만 입력했는지 확인 (중간 공백 허용, 전체 공백은 차단)
            if (it.isNotEmpty() && it.all { it.isWhitespace() }) return@OutlinedTextField
            if (field == GiftField.Amount || field == GiftField.ExpiryDate) {
                val text = it.filter { char -> char.isDigit() }
                onValueChange(field, text)
            } else {
                onValueChange(field, it)
            }
        },
        maxLines = if (field == GiftField.Memo) 50 else 1,
        label = {
            Text(
                text = stringResource(id = label),
                color = MaterialTheme.colorScheme.onPrimary
            )
        },
        visualTransformation = when (field) {
            GiftField.Amount -> thousandSeparatorTransformation(true)
            GiftField.ExpiryDate -> DateTransformation()
            else -> VisualTransformation.None
        },
        trailingIcon = {
            if (field == GiftField.ExpiryDate && isEdit) {
                IconButton(onClick = { onDatePicker() }) {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = "DateRange",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        keyboardOptions = if (isEdit && (field == GiftField.Amount || field == GiftField.ExpiryDate)) KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Number
        ) else KeyboardOptions.Default
    )
}

@Composable
fun GiftImage(
    isEdit: Boolean,
    selectedImage: Bitmap?,
    photoPath: String?,
    usedDt: String,
    onClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (isEdit) 8.dp else 20.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(200.dp)
                .height(200.dp)
                .background(Color.LightGray)
                .clickable {
                    onClick()
                }
        ) {
            if (selectedImage == null && photoPath == null) {
                Image(
                    modifier = Modifier
                        .width(90.dp)
                        .height(90.dp),
                    painter = painterResource(id = R.drawable.ic_add_photo_40),
                    contentDescription = "add photo",
                    contentScale = ContentScale.Crop
                )
            } else {
                AsyncImage(
                    modifier = Modifier
                        .fillMaxSize(),
                    model = selectedImage ?: photoPath,
                    contentDescription = "detail photo",
                    contentScale = ContentScale.Crop
                )
            }

            if (usedDt.isNotEmpty()) {
                UsedStamp(usedDt)
            }
        }
    }
}

@Composable
fun UsedStamp(usedDate: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color.Black.copy(0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(140.dp)
                .height(80.dp)
                .rotate(-20f)
                .border(
                    width = 3.dp,
                    color = Color.White
                )
        )
        Box(
            modifier = Modifier
                .width(130.dp)
                .height(70.dp)
                .rotate(-20f)
        )
        Box(
            modifier = Modifier
                .width(125.dp)
                .height(65.dp)
                .rotate(-20f)
                .border(
                    width = 7.dp,
                    color = Color.White
                )
        )

        Text(
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            text = stringResource(id = R.string.txt_used_stamp, usedDate),
            fontSize = 20.sp,
            modifier = Modifier
                .width(110.dp)
                .height(50.dp)
                .rotate(-20f)
        )
    }
}

// 사용 금액 입력 다이얼로그
@Composable
fun UseCashDialog(remainCash: String, onCancel: () -> Unit, onConfirm: (Int) -> Unit) {
    var inputCash by rememberSaveable { mutableStateOf("") }
    Surface(
        modifier = Modifier
            .wrapContentWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // title
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                text = stringResource(id = R.string.txt_use_cash_inpur),
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(color = MaterialTheme.colorScheme.outline)
            )

            // cash button
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                val useCashList = listOf(1000, 5000, 10000, remainCash.toInt())
                items(useCashList.size) { idx ->
                    Spacer(modifier = Modifier.width(4.dp))
                    FilterChip(
                        onClick = {
                            val total =
                                ((if (inputCash.isEmpty()) 0 else inputCash.toInt()) + useCashList[idx]).toString()
                            inputCash =
                                if (total.toInt() >= remainCash.toInt()) remainCash else total
                        },
                        label = {
                            Text(
                                text = if (idx == useCashList.lastIndex) {
                                    stringResource(id = R.string.btn_all_cash)
                                } else {
                                    stringResource(
                                        id = R.string.format_cash,
                                        decimalFormat(useCashList[idx])
                                    )
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        },
                        selected = false,
                        shape = RoundedCornerShape(50.dp),
                        colors = FilterChipDefaults.filterChipColors().copy(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        border = null
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }

            // input cash
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 20.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    modifier = Modifier
                        .wrapContentWidth()
                        .alignByBaseline(),
                    value = inputCash,
                    onValueChange = {
                        if (it.length > 9 || it == "00") return@BasicTextField
                        val text = it.filter { char -> char.isDigit() }
                        val input = if (text.isEmpty()) 0 else text.toInt()
                        inputCash = if (input >= remainCash.toInt()) remainCash else text
                    },
                    visualTransformation = thousandSeparatorTransformation(false),
                    textStyle = TextStyle(
                        fontSize = 24.sp,
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.onPrimary
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
                Text(
                    modifier = Modifier
                        .wrapContentWidth()
                        .alignByBaseline(),
                    text = stringResource(id = R.string.format_cash, ""),
                    fontSize = 24.sp,
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            // remain cash
            Text(
                text = stringResource(
                    id = R.string.format_remain_cash,
                    decimalFormat(remainCash.toInt() - (if (inputCash.isEmpty()) 0 else inputCash.toInt()))
                ),
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 20.dp, bottom = 10.dp),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onPrimary
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
                    onClick = { onConfirm(remainCash.toInt() - (if (inputCash.isEmpty()) 0 else inputCash.toInt())) },
                    shape = RectangleShape,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.outline,
                        disabledContentColor = MaterialTheme.colorScheme.background
                    )
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
                    onClick = { onCancel() },
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

@Composable
fun GiftBottomSheet(image: Any?, isVisible: Boolean, onDismiss: (Boolean) -> Unit) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val screenHeightToPx =
        LocalDensity.current.run { LocalConfiguration.current.screenHeightDp.dp.toPx() }
    val sheetHeight = screenHeight * 0.95f // BottomSheet 높이 (화면의 95%)
    val sheetOffsetY = remember { Animatable(screenHeight.value) } // 초기 위치: 화면 아래
    val coroutineScope = rememberCoroutineScope()
    var isFullScreen by rememberSaveable { mutableStateOf(false) }

    BackHandler {
        if (isFullScreen) isFullScreen = false
        else onDismiss(false)
    }

    // isVisible 변경 시 애니메이션 적용
    LaunchedEffect(isVisible) {
        if (isVisible) {
            sheetOffsetY.animateTo(
                screenHeight.value - sheetHeight.value,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            ) // 탄성 효과로 부드럽게 올라옴
        } else {
            sheetOffsetY.animateTo(
                screenHeight.value,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            ) // 탄성 효과로 부드럽게 내려감
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)) // 반투명 배경
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(sheetHeight)
                .offset { IntOffset(0, sheetOffsetY.value.toInt()) }
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                )
                .align(Alignment.BottomCenter)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            // 일정 거리 이상 드래그하면 닫힘
                            if (sheetOffsetY.value > screenHeightToPx * 0.2f) {
                                coroutineScope.launch {
                                    sheetOffsetY.snapTo(
                                        screenHeight.value,
                                    )
                                    onDismiss(false)
                                }
                            } else {
                                coroutineScope.launch {
                                    sheetOffsetY.animateTo(
                                        screenHeight.value - sheetHeight.value,
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                    )
                                }
                            }
                        },
                        onVerticalDrag = { change, dragAmount ->
                            if (dragAmount < 0) return@detectVerticalDragGestures
                            change.consume() // 이벤트 소비
                            coroutineScope.launch {
                                sheetOffsetY.snapTo(sheetOffsetY.value + dragAmount)
                            }
                        }
                    )
                }
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(top = 20.dp, start = 25.dp, end = 25.dp, bottom = 20.dp)
            ) {
                // 드래그 핸들
                Box(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color.Gray, RoundedCornerShape(50))
                )
                Spacer(Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .weight(8f)
                        .padding(bottom = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (image == null) {
                        Image(
                            modifier = Modifier.fillMaxWidth(),
                            painter = painterResource(id = R.drawable.ic_add_photo_40),
                            contentDescription = "use photo",
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        AsyncImage(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    // 클릭 이벤트만 처리
                                    detectTapGestures {
                                        isFullScreen = true
                                    }
                                },
                            model = image,
                            contentDescription = "use photo",
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary),
                    shape = RectangleShape,
                    onClick = {
                        if (!isFullScreen) onDismiss(true)
                    }
                ) {
                    Text(
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        text = stringResource(id = R.string.btn_use_complete)
                    )
                }
            }
        }

        // 전체화면(확대/축소)
        if (isFullScreen) {
            ImageFullScreenDialog(
                image = image,
                onDismiss = { isFullScreen = false },
            )
        }
    }
}

@Composable
fun ImageFullScreenDialog(image: Any?, onDismiss: () -> Unit) {
    var scale by remember { mutableFloatStateOf(1f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Image(
            painter = rememberAsyncImagePainter(image),
            contentDescription = "Full Screen Gifticon",
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 3f)
                    }
                }
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale
                )
        )

        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White
            )
        }
    }
}
