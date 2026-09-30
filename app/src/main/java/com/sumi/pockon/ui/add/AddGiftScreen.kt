package com.sumi.pockon.ui.add

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sumi.pockon.util.DateTransformation
import com.sumi.pockon.R
import com.sumi.pockon.ui.common.GiftField
import com.sumi.pockon.ui.common.PockonTopAppBar
import com.sumi.pockon.ui.loading.LoadingScreen
import com.sumi.pockon.util.formatDateToYYYYMMDD
import com.sumi.pockon.util.getBitmapFromUri
import com.sumi.pockon.util.thousandSeparatorTransformation
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AddGifticon(onBack: (Boolean) -> Unit) {
    val addViewModel = hiltViewModel<AddViewModel>()
    val uiState by addViewModel.uiState.collectAsState()

    // snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        addViewModel.events.collectLatest { event ->
            when (event) {
                AddGiftEvent.Saved -> onBack(true)
                AddGiftEvent.SaveFailed -> snackbarHostState.showSnackbar(
                    message = context.getString(R.string.msg_no_register)
                )
            }
        }
    }

    // select photo
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            val resizedBitmap = getBitmapFromUri(context.contentResolver, it, 1024, 1024)
            resizedBitmap?.let { bitmap ->
                addViewModel.setPhoto(bitmap) // 리사이징된 비트맵 전달
            }
        }
    }
    // scroll
    val scrollSate = rememberScrollState()

    // input data
    val inputDataList = listOf(
        uiState.name,
        uiState.brand,
        uiState.cash,
        uiState.endDate,
        uiState.memo
    )

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = Modifier
            .fillMaxSize()
    ) { _ ->
        Column {
            AddGiftScreenTopBar {
                onBack(false)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(scrollSate)
                    .padding(top = 5.dp, bottom = 5.dp, start = 25.dp, end = 25.dp)
            ) {
                // gift image
                GiftImage(uiState.photo, galleryLauncher)
                // cash
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(end = 15.dp),
                            text = stringResource(R.string.txt_cash_certificate),
                            textAlign = TextAlign.Center
                        )
                        Box(modifier = Modifier.size(1.dp)) {
                            Checkbox(
                                modifier = Modifier
                                    .scale(0.8f),
                                checked = uiState.isCheckedCash,
                                onCheckedChange = {
                                    addViewModel.chgCheckedCash()
                                },
                                colors = CheckboxDefaults.colors(
                                    uncheckedColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedColor = MaterialTheme.colorScheme.primaryContainer,  // 체크된 상태에서 배경 색상 (체크박스 색상)
                                    checkmarkColor = MaterialTheme.colorScheme.background,  // 체크 표시 색상
                                )
                            )
                        }
                    }
                }
                // text field
                for (field in GiftField.entries) {
                    if (field == GiftField.Amount && !uiState.isCheckedCash) continue
                    InputDataTextField(
                        value = inputDataList[field.index],
                        label = addViewModel.getLabelList(field.index),
                        field = field,
                        onValueChange = { changedField, value ->
                            addViewModel.setGift(changedField.index, value)
                        },
                        onDatePicker = {
                            if (field == GiftField.ExpiryDate) {
                                addViewModel.changeDatePickerState()
                            }
                        }
                    )
                }
            }
            // add button
            Button(
                onClick = {
                    val msg = addViewModel.isValid()
                    if (msg != null) {
                        scope.launch {
                            snackbarHostState.showSnackbar(message = context.getString(msg))
                        }
                    } else {
                        addViewModel.addGift()
                    }
                },
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 25.dp, end = 25.dp, bottom = 15.dp)
            ) {
                Text(
                    color = MaterialTheme.colorScheme.onPrimary,
                    text = stringResource(id = R.string.btn_add),
                )
            }
        }

        // DatePicker
        if (uiState.isShowDatePicker) {
            CustomDatePickerDialog(
                dateString = uiState.endDate,
                onCancel = { addViewModel.changeDatePickerState() },
                onConfirm = {
                    addViewModel.changeDatePickerState()
                    addViewModel.setGift(GiftField.ExpiryDate.index, value = it)
                }
            )
        }
    }

    // Loading Indicator
    if (uiState.isLoading) {
        LoadingScreen()
    }

}

@Composable
fun InputDataTextField(
    value: String,
    label: Int,
    field: GiftField,
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
            if (field == GiftField.ExpiryDate) {
                IconButton(onClick = { onDatePicker() }) {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = "DateRange",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        keyboardOptions = if (field == GiftField.Amount || field == GiftField.ExpiryDate) KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number) else KeyboardOptions.Default
    )
}

@Composable
fun GiftImage(
    selectedImage: Bitmap?,
    galleryLauncher: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(200.dp)
                .height(200.dp)
                .background(MaterialTheme.colorScheme.outline)
                .clickable {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                }
        ) {
            if (selectedImage == null) {
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
                    modifier = Modifier.fillMaxSize(),
                    model = selectedImage,
                    contentDescription = "add photo",
                    contentScale = ContentScale.Crop
                )
            }
        }
    }

}

@Composable
fun AddGiftScreenTopBar(onBack: () -> Unit) {
    PockonTopAppBar(
        title = {
        Text(
            text = stringResource(id = R.string.title_add_gift),
            fontSize = 16.sp,
        )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "back button"
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        showBottomDivider = true
    )
}

@Composable
fun CustomDatePickerDialog(
    dateString: String,
    onCancel: () -> Unit,
    onConfirm: (String) -> Unit
) {
    // 문자열을 Date 객체로 변환
    val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    dateFormat.timeZone = TimeZone.getTimeZone("UTC") // UTC로 시간대 설정
    val dateObj = if (dateString.isNotEmpty() && dateString.length == 8) dateFormat.parse(dateString) else null

    val calendar = Calendar.getInstance()
    if (dateObj != null) {
        calendar.time = dateObj
    }

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        LocalContext.current,
        R.style.CustomDialogTheme,
        { _, selectedYear, selectedMonth, selectedDayOfMonth ->
            val selectedDate = formatDateToYYYYMMDD(selectedYear, selectedMonth, selectedDayOfMonth)
            onConfirm(selectedDate)
        },
        year,
        month,
        day
    )

    // 취소 버튼 리스너
    datePickerDialog.setButton(
        DatePickerDialog.BUTTON_NEGATIVE, stringResource(id = R.string.btn_cancel)
    ) { _, _ ->
        onCancel()
    }

    val txtConfirm = stringResource(id = R.string.btn_confirm)
    datePickerDialog.setOnShowListener {
        datePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE).text = txtConfirm
    }

    // 다이얼로그 표시
    datePickerDialog.show()
}
