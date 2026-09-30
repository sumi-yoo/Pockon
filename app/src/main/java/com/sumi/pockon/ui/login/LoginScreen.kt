package com.sumi.pockon.ui.login

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sumi.pockon.R
import com.sumi.pockon.ui.loading.LoadingScreen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onAuthenticated: (isPinEnabled: Boolean) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isNetworkConnected by viewModel.isNetworkConnected.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val loginFailureMessage = stringResource(R.string.msg_login_fail)
    val noInternetMessage = stringResource(R.string.msg_no_internet)
    val privacyConsentRequiredMessage = stringResource(R.string.privacy_consent_required)
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    var showPrivacyConsentDialog by rememberSaveable { mutableStateOf(false) }
    val requestGoogleLogin: () -> Unit = {
        if (isNetworkConnected) {
            showPrivacyConsentDialog = true
        } else {
            coroutineScope.launch { snackbarHostState.showSnackbar(message = noInternetMessage) }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.loginFailureEvents.collectLatest {
            snackbarHostState.showSnackbar(message = loginFailureMessage)
        }
    }

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onAuthenticated(viewModel.getIsPinUse())
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.primary))
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 40.dp)
        ) {
            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LoginBrand(
                        modifier = Modifier.weight(1f),
                        imageHeight = 120.dp
                    )
                    if (uiState.isFirstLogin) {
                        LoginActions(
                            modifier = Modifier
                                .weight(1f)
                                .widthIn(max = 320.dp)
                                .padding(start = 24.dp),
                            onGoogleLogin = requestGoogleLogin,
                            onGuestLogin = viewModel::loginAsGuest
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LoginBrand(
                        modifier = Modifier.weight(1f),
                        imageHeight = 150.dp
                    )
                    if (uiState.isFirstLogin) {
                        LoginActions(
                            modifier = Modifier.fillMaxWidth(),
                            onGoogleLogin = requestGoogleLogin,
                            onGuestLogin = viewModel::loginAsGuest
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )

        if (uiState.isLoading) {
            LoadingScreen()
        }
    }

    if (showPrivacyConsentDialog) {
        PrivacyConsentDialog(
            onAgree = {
                showPrivacyConsentDialog = false
                viewModel.loginWithGoogleCredential()
            },
            onDismiss = {
                showPrivacyConsentDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(message = privacyConsentRequiredMessage)
                }
            }
        )
    }
}

@Composable
private fun LoginBrand(
    modifier: Modifier,
    imageHeight: Dp
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.ic_gift),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.height(imageHeight),
                contentScale = ContentScale.Fit
            )
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.onPrimary)
            )
        }
    }
}

@Composable
private fun LoginActions(
    modifier: Modifier,
    onGoogleLogin: () -> Unit,
    onGuestLogin: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedButton(
            onClick = onGoogleLogin,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = colorResource(R.color.background),
                contentColor = colorResource(R.color.onPrimary)
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.ic_google_logo),
                contentDescription = "Google",
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.btn_goggle_login))
        }
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onGuestLogin,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.background),
                contentColor = colorResource(R.color.onPrimary)
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_account_circle_24),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.Gray
            )
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.btn_guest_login))
        }
        }
    }
}

@Composable
private fun PrivacyConsentDialog(
    onAgree: () -> Unit,
    onDismiss: () -> Unit
) {
    val maxContentHeight = (LocalConfiguration.current.screenHeightDp * 0.45f).dp

    AlertDialog(
        onDismissRequest = {},
        title = { Text(stringResource(R.string.privacy_title)) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = maxContentHeight)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.privacy_content),
                    textAlign = TextAlign.Start
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onAgree) {
                Text(
                    text = stringResource(R.string.btn_agree),
                    color = Color.Black
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.btn_disagree),
                    color = Color.Black
                )
            }
        }
    )
}
