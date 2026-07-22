package com.sumi.pockon.ui.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sumi.pockon.R
import com.sumi.pockon.ui.loading.LoadingScreen
import com.sumi.pockon.ui.add.AddGifticon
import com.sumi.pockon.ui.detail.DetailScreen
import com.sumi.pockon.ui.home.HomeScreen
import com.sumi.pockon.ui.list.ListScreen
import com.sumi.pockon.ui.map.MapScreen
import com.sumi.pockon.ui.notification.NotificationSettingScreen
import com.sumi.pockon.ui.settings.CopyrightScreen
import com.sumi.pockon.ui.settings.SettingsScreen
import com.sumi.pockon.ui.used.UsedScreen

@Composable
fun BottomNavigationBar(
    movePinScreen: () -> Unit,
    moveLogInScreen: () -> Unit
) {
    val mainViewModel = hiltViewModel<MainViewModel>()
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination
    val bottomScreens = listOf(
        Screen.List,
        Screen.Home,
        Screen.Setting
    )
    val showBottomBar = navController
        .currentBackStackEntryAsState().value?.destination?.route in bottomScreens.map { it.route }

    val context = LocalContext.current

    // check permission
    val launcherPermissions = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        if (Build.VERSION.SDK_INT >= 33) {
            val notificationGranted = permissionsMap[Manifest.permission.POST_NOTIFICATIONS] ?: true

            if (!notificationGranted) {
                // 알림 권한이 거부된 경우 만료 임박 알림 off
                mainViewModel.disableNotification()
            }
        }
    }

    var isShowIndicator by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.primary, // 전체 배경색
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    bottomScreens.forEach { bottomNavigationItem ->
                        NavigationBarItem(
                            selected = currentRoute?.hierarchy?.any { it.route == bottomNavigationItem.route } == true,
                            onClick = {
                                navController.navigate(route = bottomNavigationItem.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = bottomNavigationItem.icon,
                                    contentDescription = stringResource(id = bottomNavigationItem.label)
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(id = bottomNavigationItem.label)
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                unselectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onAdd = {
                        navController.navigate(route = Screen.Add.route)
                    },
                    showMap = {
                        navController.navigate(route = Screen.Map.route)
                    },
                    onDetail = { id ->
                        navController.navigate(route = "${Screen.Detail.route}/${id}")
                    },
                    isLoading = {
                        isShowIndicator = it
                    }
                )
            }
            composable(Screen.List.route) {
                ListScreen(
                    onDetail = { id ->
                        navController.navigate(route = "${Screen.Detail.route}/${id}")
                    },
                    onAdd = {
                        navController.navigate(route = Screen.Add.route)
                    },
                    isLoading = {
                        isShowIndicator = it
                    }
                )
            }
            composable(Screen.Setting.route) {
                SettingsScreen(
                    onUsedGift = {
                        navController.navigate(route = Screen.Used.route)
                    },
                    movePinScreen = {
                        movePinScreen()
                    },
                    moveLogInScreen = {
                        moveLogInScreen()
                    },
                    moveCopyrightScreen = {
                        navController.navigate(route = Screen.Copyright.route)
                    },
                    moveNotiImminentUseScreen = {
                        navController.navigate(route = Screen.NotificationSetting.route)
                    },
                    isLoading = {
                        isShowIndicator = it
                    }
                )
            }
            composable(
                Screen.Add.route,
                enterTransition = {
                    fadeIn(animationSpec = tween(300, easing = LinearEasing)) +
                            slideIntoContainer(
                                animationSpec = tween(300, easing = EaseIn),
                                towards = AnimatedContentTransitionScope.SlideDirection.Start
                            )
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(300, easing = LinearEasing)) +
                            slideOutOfContainer(
                                animationSpec = tween(300, easing = EaseOut),
                                towards = AnimatedContentTransitionScope.SlideDirection.End
                            )
                }
            ) {
                AddGifticon(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(
                route = "${Screen.Detail.route}/{id}",
                arguments = listOf(
                    navArgument("id") {
                        type = NavType.StringType
                    }
                )
            ) { navBackStackEntry ->
                val id = navBackStackEntry.arguments?.getString("id") ?: ""
                DetailScreen(id) {
                    navController.popBackStack()
                }
            }
            composable(Screen.Map.route) {
                MapScreen (
                    onBack = {
                        navController.popBackStack()
                    },
                    onDetail = { id ->
                        navController.navigate(route = "${Screen.Detail.route}/${id}")
                    }
                )
            }
            composable(Screen.Used.route) {
                UsedScreen(
                    onDetail = { id ->
                        navController.navigate(route = "${Screen.Detail.route}/${id}")
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.Copyright.route) {
                CopyrightScreen()
            }
            composable(Screen.NotificationSetting.route) {
                NotificationSettingScreen {
                    navController.popBackStack()
                }
            }
        }
    }

    // 권한 체크
    val notGranted = checkPermission(context)
    if (notGranted.isNotEmpty() && !mainViewModel.isPermRationale.value) {
        PermissionExplanationDialog(
            onRequestPermission = {
                mainViewModel.saveIsPermRationale()
                launcherPermissions.launch(notGranted)
            },
            onDismissRequest = {
                mainViewModel.saveIsPermRationale()
            }
        )
    }

    // Loading Indicator
    if (isShowIndicator) LoadingScreen()
}

/** 앨범, 위치, 알림 권한 체크 */
private fun checkPermission(context: Context): Array<String> {
    val permissions = if (Build.VERSION.SDK_INT >= 33) {
        arrayOf(
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    return if (!permissions.all {
            ContextCompat.checkSelfPermission(
                context,
                it
            ) == PackageManager.PERMISSION_GRANTED
        })
    {
        permissions
    } else {
        arrayOf()
    }
}

@Composable
fun PermissionExplanationDialog(
    onRequestPermission: () -> Unit,
    onDismissRequest: () -> Unit
) {
    val permissionsText = if (Build.VERSION.SDK_INT >= 33) {
        stringResource(R.string.permission_explanation_api_33_up).trimIndent()
    } else {
        stringResource(R.string.permission_explanation_api_32_down).trimIndent()
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = stringResource(R.string.dialog_permission_title)) },
        text = { Text(text = permissionsText) },
        confirmButton = {
            TextButton(onClick = onRequestPermission) {
                Text(
                    text = stringResource(R.string.dialog_permission_confirm),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(
                    text = stringResource(R.string.btn_cancel),
                    color = Color.Gray
                )
            }
        }
    )
}



sealed class Screen(val route: String, val icon: ImageVector, @StringRes val label: Int) {
    data object Home : Screen("home", Icons.Filled.Home, R.string.home)
    data object List : Screen("list", Icons.AutoMirrored.Filled.List, R.string.list)
    data object Setting : Screen("setting", Icons.Filled.Settings, R.string.setting)

    data object Add : Screen("add", Icons.Filled.Add, R.string.add)
    data object Detail : Screen("detail", Icons.Filled.Search, R.string.detail)
    data object Map : Screen("map", Icons.Filled.LocationOn, R.string.map)
    data object Used : Screen("used", Icons.Filled.LocationOn, R.string.txt_usage_history)
    data object Copyright : Screen("copyright", Icons.Filled.LocationOn, R.string.txt_copyright)
    data object NotificationSetting : Screen("notification_setting", Icons.Filled.LocationOn, R.string.txt_noti_of_imminent_use)
}

fun isNetworkConnected(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    } else {
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}