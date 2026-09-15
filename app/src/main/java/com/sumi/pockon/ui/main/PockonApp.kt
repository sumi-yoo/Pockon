package com.sumi.pockon.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sumi.pockon.R
import com.sumi.pockon.ui.login.LoginScreen
import com.sumi.pockon.ui.login.LoginViewModel
import com.sumi.pockon.ui.pin.PinScreen

private object AppRoute {
    const val Login = "login"
    const val Pin = "pin"
    const val Main = "main"
}

@Composable
fun PockonApp() {
    val colorScheme = lightColorScheme(
        primary = colorResource(R.color.primary),
        onPrimary = colorResource(R.color.onPrimary),
        primaryContainer = colorResource(R.color.primaryContainer),
        onPrimaryContainer = colorResource(R.color.onPrimary),
        secondary = colorResource(R.color.secondary),
        secondaryContainer = colorResource(R.color.secondaryContainer),
        tertiary = colorResource(R.color.tertiary),
        tertiaryContainer = colorResource(R.color.tertiaryContainer),
        outline = colorResource(R.color.light_gray),
        error = colorResource(R.color.red),
        errorContainer = colorResource(R.color.light_gray),
        background = colorResource(R.color.background)
    )

    MaterialTheme(colorScheme = colorScheme) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.gray))
        ) {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = AppRoute.Login,
                modifier = Modifier.statusBarsPadding()
            ) {
            composable(AppRoute.Login) {
                val loginViewModel = hiltViewModel<LoginViewModel>()
                LoginScreen(loginViewModel) { isPinEnabled ->
                    navController.navigate(
                        if (isPinEnabled) AppRoute.Pin else AppRoute.Main
                    ) {
                        popUpTo(AppRoute.Login) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            composable(AppRoute.Pin) {
                PinScreen {
                    navController.navigate(AppRoute.Main) {
                        popUpTo(AppRoute.Pin) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            composable(AppRoute.Main) {
                MainScreen(
                    movePinScreen = {
                        navController.navigate(AppRoute.Pin) {
                            launchSingleTop = true
                        }
                    },
                    moveLogInScreen = {
                        navController.navigate(AppRoute.Login) {
                            popUpTo(AppRoute.Main) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            }
        }
    }
}
