package com.sumi.pockon.ui.notification

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.sumi.pockon.R
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlin.math.abs

@Composable
fun NotificationSettingScreen(onBack: () -> Unit) {
    val notificationSettingViewModel = hiltViewModel<NotificationSettingViewModel>()
    val day = notificationSettingViewModel.getDayList()
    val scrollState = rememberScrollState()

    BackHandler {
        onBack()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        NotificationSettingScreenTopBar {
            onBack()
        }

        Column(
            Modifier.verticalScroll(scrollState)
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp, end = 15.dp, top = 10.dp, bottom = 10.dp),
                text = stringResource(id = R.string.txt_noti_of_imminent_use_select),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(MaterialTheme.colorScheme.outline)
            )
            Box(
                modifier = Modifier
                    .clickable {
                        notificationSettingViewModel.toggleIsShowTimePickerWheelDialog()
                    }
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 15.dp, end = 15.dp, top = 10.dp, bottom = 10.dp),
                    text = stringResource(
                        id = R.string.txt_noti_end_dt_time,
                        notificationSettingViewModel.seletedTime.value
                    ),
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(MaterialTheme.colorScheme.outline)
            )
            repeat(day.size) { idx ->
                Column(
                    modifier = Modifier
                        .clickable {
                            notificationSettingViewModel.setSeletedDay(day[idx])
                            notificationSettingViewModel.changeNotiEndDt()
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 15.dp, end = 15.dp, top = 10.dp, bottom = 10.dp)
                    ) {
                        Text(
                            modifier = Modifier.align(Alignment.CenterStart),
                            text = if (idx == 0) stringResource(id = R.string.txt_today) else stringResource(id = R.string.txt_day_before, day[idx])
                        )

                        if (notificationSettingViewModel.seletedDay.value == day[idx]) {
                            Icon(
                                modifier = Modifier.align(Alignment.CenterEnd),
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Check",
                                tint = MaterialTheme.colorScheme.primaryContainer
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(MaterialTheme.colorScheme.outline)
                    )
                }
            }
        }
    }

    // 시간 입력 다이얼로그
    if (notificationSettingViewModel.isShowTimePickerWheelDialog.value) {
        Dialog(onDismissRequest = {}) {
            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White
            ) {
                val time = notificationSettingViewModel.getNotiEndDtTime()
                TimePickerWheelDialog(
                    hour24 = time.first,
                    minute = time.second,
                    onCancel = {
                        notificationSettingViewModel.toggleIsShowTimePickerWheelDialog()
                    },
                    onChanged = { hour24, minute ->
                        notificationSettingViewModel.selectedTime(hour24, minute)
                    },
                    onConfirm = {
                        notificationSettingViewModel.confirmTime()
                        notificationSettingViewModel.toggleIsShowTimePickerWheelDialog()
                        notificationSettingViewModel.changeNotiEndDt()
                    }
                )
            }
        }
    }
}

@Composable
fun NotificationSettingScreenTopBar(onBack: () -> Unit) {
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
            .padding(top = 10.dp, bottom = 10.dp)
    ) {
        IconButton(
            modifier = Modifier.align(Alignment.CenterStart),
            onClick = {
                onBack()
            }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "back button"
            )
        }
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = stringResource(id = R.string.txt_noti_of_imminent_use),
            fontSize = 16.sp,
        )
    }
}

// 사용 금액 입력 다이얼로그
@Composable
fun TimePickerWheelDialog(hour24: Int, minute: Int, onCancel: () -> Unit, onChanged: (Int, Int) -> Unit, onConfirm: () -> Unit) {
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
                text = stringResource(id = R.string.title_select_time),
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

            // timepicker
            TimePickerWithAmPmView(
                initialHour = hour24,
                initialMinute = minute
            ) { hour24, minute ->
                onChanged(hour24, minute)
            }

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
                    onClick = {
                        onConfirm()
                    },
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
fun TimePickerWithAmPmView(
    initialHour: Int,  // 24시간 기준
    initialMinute: Int,
    onTimeChange: (hour24: Int, minute: Int) -> Unit
) {
    var hour by remember { mutableStateOf(if (initialHour == 0 || initialHour == 12) 12 else initialHour % 12) }
    var minute by remember { mutableStateOf(initialMinute) }
    var isAm by remember { mutableStateOf(initialHour < 12) }

    fun notifyTimeChanged() {
        val hour24 = if (isAm) {
            if (hour == 12) 0 else hour
        } else {
            if (hour == 12) 12 else hour + 12
        }
        onTimeChange(hour24, minute)
    }

    Row(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        val txtAm = stringResource(id = R.string.txt_am)
        val txtPm = stringResource(id = R.string.txt_pm)

        TimeWheelPicker(
            values = listOf(true, false),
            selectedValue = isAm,
            valueLabel = { if (it) txtAm else txtPm },
            onValueSelected = {
                isAm = it
                notifyTimeChanged()
            }
        )
        TimeWheelPicker(
            values = (1..12).toList(),
            selectedValue = hour,
            valueLabel = Int::toString,
            onValueSelected = {
                hour = it
                notifyTimeChanged()
            }
        )
        TimeWheelPicker(
            values = (0..59).toList(),
            selectedValue = minute,
            valueLabel = { "%02d".format(it) },
            onValueSelected = {
                minute = it
                notifyTimeChanged()
            }
        )
    }
}

@Composable
private fun <T> TimeWheelPicker(
    values: List<T>,
    selectedValue: T,
    valueLabel: (T) -> String,
    onValueSelected: (T) -> Unit
) {
    val itemHeight = 40.dp
    val listState = rememberLazyListState()
    val selectedIndex = values.indexOf(selectedValue).coerceAtLeast(0)
    val selectionLineColor = MaterialTheme.colorScheme.primaryContainer

    LaunchedEffect(selectedIndex) {
        listState.scrollToItem(selectedIndex)
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            layoutInfo.visibleItemsInfo
                .minByOrNull { item -> abs((item.offset + item.size / 2) - viewportCenter) }
                ?.index
        }
            .filterNotNull()
            .distinctUntilChanged()
            .collect { index ->
                values.getOrNull(index)?.let(onValueSelected)
            }
    }

    Box(
        modifier = Modifier
            .width(88.dp)
            .height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(listState),
            contentPadding = PaddingValues(vertical = itemHeight),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(values, key = { _, value -> value.hashCode() }) { index, value ->
                Text(
                    text = valueLabel(value),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .clickable { onValueSelected(value) }
                        .wrapContentHeight(Alignment.CenterVertically),
                    textAlign = TextAlign.Center,
                    color = if (index == selectedIndex) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .drawWithContent {
                    drawContent()
                    drawLine(
                        color = selectionLineColor,
                        start = Offset.Zero,
                        end = Offset(size.width, 0f),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = selectionLineColor,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
        )
    }
}
