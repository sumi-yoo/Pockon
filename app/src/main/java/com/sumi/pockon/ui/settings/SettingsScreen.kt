package com.sumi.pockon.ui.settings

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sumi.pockon.R
import com.sumi.pockon.ui.common.PockonTopAppBar
import com.sumi.pockon.ui.common.PockonConfirmDialog
import com.sumi.pockon.ui.common.NetworkErrorDialog
import com.sumi.pockon.ui.common.PockonMessageDialog
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SettingsScreen(
    onUsedGift: () -> Unit,
    movePinScreen: () -> Unit,
    moveLogInScreen: () -> Unit,
    moveNotiImminentUseScreen: () -> Unit,
    isLoading: (Boolean) -> Unit
) {
    val settingViewModel = hiltViewModel<SettingsViewModel>()

    var showLogoutDlg by remember { mutableStateOf(false) }
    var showRemoveDlg by remember { mutableStateOf(false) }
    var showNotificationPermissionDialog by remember { mutableStateOf(false) }
    var checkedAlarm by rememberSaveable { mutableStateOf(settingViewModel.getIsNotiEndDt()) }
    var checkedPwd by rememberSaveable { mutableStateOf(settingViewModel.getIsAuthPin()) }

    // snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val scrollState = rememberScrollState()

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) {
        Column {
            SettingScreenTopBar()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(scrollState)
            ) {
                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    // 사용자 정보
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        if (settingViewModel.getProfileImage() != null) {
                            AsyncImage(
                                model = settingViewModel.getProfileImage(),
                                contentDescription = "profile image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = "profile image",
                                modifier = Modifier.size(32.dp),
                                tint = Color.Gray
                            )
                        }
                        Column(
                            modifier = Modifier.padding(start = 12.dp)
                        ) {
                            if (settingViewModel.getIsGuestMode()) {
                                Text(text = stringResource(id = R.string.txt_guest))
                            } else {
                                Text(
                                    text = settingViewModel.getName() ?: stringResource(id = R.string.txt_user),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = settingViewModel.getEmail(),
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    // 사용내역
                    SettingItem(
                        text = stringResource(id = R.string.txt_usage_history),
                        isTitle = true
                    )

                    SettingItem(
                        text = stringResource(id = R.string.txt_usage_history),
                        onClick = {
                            onUsedGift()
                        }
                    )

                    // 설정
                    SettingItem(
                        text = stringResource(id = R.string.setting),
                        isTitle = true
                    )

                    SettingItem(
                        text = stringResource(id = R.string.txt_noti_of_imminent_use),
                        isSwitch = true,
                        checked = checkedAlarm,
                        onCheck = {
                            if (checkNotificationPermission(context) || checkedAlarm) {
                                checkedAlarm = !checkedAlarm
                                settingViewModel.onOffNotiEndDt(checkedAlarm)
                            } else {
                                showNotificationPermissionDialog = true
                            }
                        },
                        onClick = {
                            if (checkedAlarm) moveNotiImminentUseScreen()
                        }
                    )

                    SettingItem(
                        text = stringResource(id = R.string.txt_use_pwd),
                        isSwitch = true,
                        checked = checkedPwd,
                        onCheck = {
                            checkedPwd = !checkedPwd
                            if (checkedPwd) {
                                movePinScreen()
                            } else {
                                settingViewModel.offAuthPin()
                            }
                        },
                    )

                    // 사용자
                    SettingItem(
                        text = stringResource(id = R.string.txt_user),
                        isTitle = true
                    )

                    SettingItem(
                        text = if (settingViewModel.getIsGuestMode()) {
                            stringResource(id = R.string.txt_logout_in_guest)
                        } else {
                            stringResource(id = R.string.txt_logout)
                        },
                        onClick = {
                            showLogoutDlg = true
                        }
                    )

                    if (!settingViewModel.getIsGuestMode()) {
                        SettingItem(
                            text = stringResource(id = R.string.txt_remove_account),
                            onClick = {
                                showRemoveDlg = true
                            }
                        )
                    }

                }
            }
        }

        if (showLogoutDlg) {
            PockonConfirmDialog(
                message = if (settingViewModel.getIsGuestMode()) {
                    R.string.dlg_msg_logout_in_guest
                } else {
                    R.string.dlg_msg_logout
                },
                onConfirm = {
                    isLoading(true)
                    showLogoutDlg = false
                    settingViewModel.logout()
                    moveLogInScreen()
                    isLoading(false)
                },
                onDismiss = {
                    showLogoutDlg = false
                }
            )
        }

        if (showRemoveDlg) {
            PockonConfirmDialog(
                message = R.string.dlg_msg_remove_account,
                onConfirm = {
                    showRemoveDlg = false
                    isLoading(true)
                    settingViewModel.removeAccountWithCredential()
                },
                onDismiss = {
                    showRemoveDlg = false
                }
            )
        }

        NetworkErrorDialog(
            visible = settingViewModel.isShowNoInternetDialog.value,
            onDismiss = settingViewModel::changeNoInternetDialogState
        )
    }

    if (showNotificationPermissionDialog) {
        PockonMessageDialog(
            message = R.string.msg_no_notification_permission,
            onConfirm = {
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                )
                showNotificationPermissionDialog = false
            },
            onDismiss = { showNotificationPermissionDialog = false }
        )
    }

    LaunchedEffect(Unit) {
        settingViewModel.events.collect { event ->
            when (event) {
                SettingsEvent.AccountDeleted -> {
                    isLoading(false)
                    moveLogInScreen()
                }
                SettingsEvent.AccountDeletionFailed -> {
                    isLoading(false)
                    snackbarHostState.showSnackbar(context.getString(R.string.msg_remove_account_fail))
                }
            }
        }
    }
}

@Composable
private fun SettingItem(
    text: String,
    isTitle: Boolean = false,
    isSwitch: Boolean = false,
    checked: Boolean = false,
    onCheck: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
    ) {
        val modifier = if (isTitle) {
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondary)
                .padding(start = 15.dp, end = 15.dp, top = 10.dp, bottom = 10.dp)
        } else {
            Modifier
                .wrapContentSize()
                .padding(start = 15.dp, end = 15.dp, top = 10.dp, bottom = 10.dp)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = modifier,
                text = text,
            )

            if (isSwitch) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Switch(
                        modifier = Modifier
                            .padding(end = 5.dp)
                            .scale(0.6f)
                            .align(Alignment.CenterEnd),
                        checked = checked,
                        onCheckedChange = {
                            onCheck()
                        },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                            checkedThumbColor = MaterialTheme.colorScheme.background,
                            checkedBorderColor = MaterialTheme.colorScheme.primaryContainer,
                            uncheckedTrackColor = colorResource(id = R.color.light_gray),
                            uncheckedThumbColor = MaterialTheme.colorScheme.background,
                            uncheckedBorderColor = colorResource(id = R.color.light_gray)
                        )
                    )
                }
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

@Composable
private fun SettingScreenTopBar() {
    PockonTopAppBar(
        title = {
        Text(
            text = stringResource(id = R.string.setting),
            fontSize = 18.sp,
        )
        }
    )
}


@Composable
private fun CopyrightItem(name: String, url: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        Text(
            text = stringResource(id = R.string.txt_copyright_info, name),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = stringResource(id = R.string.txt_copyright_info_url, url),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth(),
            color = Color.Gray
        )
    }
}

@Composable
fun CopyrightScreen() {
    val nameList = listOf("Md Tanvirul Haque", "Kiranshastry")
    val urlList = listOf(
        "https://www.flaticon.com/free-icon/user_9131646?term=user&page=2&position=35&origin=tag&related_id=9131646",
        "https://www.flaticon.com/free-icon/gallery_833281?term=photo&page=1&position=1&origin=tag&related_id=833281"
    )
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items((0..1).toList()) { idx ->
            CopyrightItem(
                name = nameList[idx],
                url = urlList[idx]
            )
        }
    }
}

/** 알림 권한 체크 */
fun checkNotificationPermission(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < 33) return true

    val permissions = arrayOf(Manifest.permission.POST_NOTIFICATIONS)

    return permissions.all {
        ContextCompat.checkSelfPermission(
            context,
            it
        ) == PackageManager.PERMISSION_GRANTED
    }
}
