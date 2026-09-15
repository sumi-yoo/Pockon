package com.sumi.pockon.ui.pin

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sumi.pockon.R

@Composable
fun PinScreen(onSuccess: () -> Unit) {
    val pinViewModel = hiltViewModel<PinViewModel>()
    val mode = pinViewModel.mode.value
    val isSuccess = pinViewModel.showSuccess.value && mode != 0
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (mode == 4 || isSuccess) {
        LaunchedEffect(mode, isSuccess) {
            onSuccess()
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.background))
            .navigationBarsPadding()
    ) {
        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    PinHeader(
                        mode = mode,
                        isInputEnabled = !pinViewModel.isVerifying.value,
                        height = 48.dp,
                        onBack = { pinViewModel.setMode(0) }
                    )
                    PinTitle(pinViewModel.getTitle())
                    PinIndicator(
                        inputSize = pinViewModel.inputPin.size,
                        pinSize = pinViewModel.getPinSize()
                    )
                    PinError(pinViewModel.error.value)
                }
                PinKeypad(
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(max = 340.dp),
                    mode = mode,
                    keySize = 56.dp,
                    isInputEnabled = !pinViewModel.isVerifying.value,
                    onDigit = pinViewModel::addPinNum,
                    onDelete = pinViewModel::removeLastPin,
                    onSkip = { pinViewModel.setMode(4) }
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                PinHeader(
                    mode = mode,
                    isInputEnabled = !pinViewModel.isVerifying.value,
                    height = 100.dp,
                    onBack = { pinViewModel.setMode(0) }
                )
                PinTitle(pinViewModel.getTitle())
                Spacer(modifier = Modifier.height(30.dp))
                PinIndicator(
                    inputSize = pinViewModel.inputPin.size,
                    pinSize = pinViewModel.getPinSize()
                )
                PinError(pinViewModel.error.value)
                Spacer(modifier = Modifier.height(50.dp))
                PinKeypad(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    mode = mode,
                    keySize = 80.dp,
                    isInputEnabled = !pinViewModel.isVerifying.value,
                    onDigit = pinViewModel::addPinNum,
                    onDelete = pinViewModel::removeLastPin,
                    onSkip = { pinViewModel.setMode(4) }
                )
            }
        }
    }
}

@Composable
private fun PinHeader(
    mode: Int,
    isInputEnabled: Boolean,
    height: Dp,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(height)
            .fillMaxWidth(),
        contentAlignment = Alignment.TopStart
    ) {
        if (mode == 1) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier
                    .padding(15.dp)
                    .clickable(enabled = isInputEnabled, onClick = onBack),
                tint = colorResource(id = R.color.onPrimary)
            )
        }
    }
}

@Composable
private fun PinTitle(titleRes: Int) {
    Text(
        text = stringResource(titleRes),
        style = typography.titleMedium,
        modifier = Modifier.padding(16.dp),
        color = colorResource(id = R.color.onPrimary)
    )
}

@Composable
private fun PinIndicator(inputSize: Int, pinSize: Int) {
    Row {
        (0 until pinSize).forEach { index ->
            val backgroundColor = if (inputSize > index) R.color.onPrimary else R.color.background
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(15.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = colorResource(id = R.color.onPrimary),
                        shape = CircleShape
                    )
                    .background(colorResource(backgroundColor))
            )
        }
    }
}

@Composable
private fun PinError(errorRes: Int?) {
    Text(
        text = errorRes?.let { stringResource(it) } ?: "",
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
private fun PinKeypad(
    modifier: Modifier,
    mode: Int,
    keySize: Dp,
    isInputEnabled: Boolean,
    onDigit: (Int) -> Unit,
    onDelete: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (mode != 2) {
            Text(
                text = stringResource(R.string.txt_no_pin_auth),
                modifier = Modifier
                    .padding(bottom = 5.dp)
                    .clickable(enabled = isInputEnabled, onClick = onSkip),
                textAlign = TextAlign.Center,
                color = colorResource(id = R.color.onPrimary)
            )
        }
        PinNumberRow(1..3, keySize, isInputEnabled, onDigit)
        PinNumberRow(4..6, keySize, isInputEnabled, onDigit)
        PinNumberRow(7..9, keySize, isInputEnabled, onDigit)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PinKeyItem(enabled = false, minSize = keySize, onClick = {}) {
                Spacer(modifier = Modifier.padding(4.dp))
            }
            PinKeyItem(enabled = isInputEnabled, minSize = keySize, onClick = { onDigit(0) }) {
                Text(
                    text = "0",
                    style = typography.bodyLarge,
                    modifier = Modifier.padding(4.dp),
                    color = colorResource(id = R.color.onPrimary)
                )
            }
            PinKeyItem(enabled = isInputEnabled, minSize = keySize, onClick = onDelete) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Clear",
                    modifier = Modifier.size(20.dp),
                    tint = colorResource(id = R.color.onPrimary)
                )
            }
        }
    }
}

@Composable
private fun PinNumberRow(
    numbers: IntRange,
    keySize: Dp,
    isInputEnabled: Boolean,
    onDigit: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        numbers.forEach { number ->
            PinKeyItem(
                enabled = isInputEnabled,
                minSize = keySize,
                onClick = { onDigit(number) }
            ) {
                Text(
                    text = number.toString(),
                    style = typography.bodyLarge,
                    color = colorResource(id = R.color.onPrimary)
                )
            }
        }
    }
}

@Composable
private fun PinKeyItem(
    enabled: Boolean,
    minSize: Dp,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = minSize, minHeight = minSize)
            .padding(10.dp)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
