package com.sumi.pockon.ui.login

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumi.pockon.domain.usecase.GetGoogleCredentialUseCase
import com.sumi.pockon.domain.usecase.GetSignInIntentUseCase
import com.sumi.pockon.domain.usecase.SignInUseCase
import com.sumi.pockon.domain.usecase.IsPinEnabledUseCase
import com.sumi.pockon.domain.usecase.SaveUserSessionUseCase
import com.sumi.pockon.domain.usecase.GetUserSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val getGoogleCredentialUseCase: GetGoogleCredentialUseCase,
    private val getSignInIntentUseCase: GetSignInIntentUseCase,
    private val signInUseCase: SignInUseCase,
    private val isPinEnabledUseCase: IsPinEnabledUseCase,
    private val saveUserSessionUseCase: SaveUserSessionUseCase,
    private val getUserSessionUseCase: GetUserSessionUseCase
) : ViewModel() {

    private val session = getUserSessionUseCase()
    private val _uiState = MutableStateFlow(
        LoginUiState(
            isFirstLogin = session.uid.isEmpty(),
            isAuthenticated = session.uid.isNotEmpty()
        )
    )
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    private var isPinUse = isPinEnabledUseCase()

    fun getIsPinUse(): Boolean {
        return isPinUse
    }

    fun requestSignInIntent() {
        viewModelScope.launch {
            getSignInIntentUseCase(session.email).onSuccess { intent ->
                _events.emit(LoginEvent.LaunchSignIn(intent))
            }.onFailure {
                _events.emit(LoginEvent.ShowLoginFailure)
            }
        }
    }

    fun loginAsGuest() {
        isPinUse = true
        saveUserSessionUseCase(uid = UUID.randomUUID().toString(), isGuest = true)
        authenticate()
    }

    fun loginForApiLower(idToken: String?, email: String?, name: String?, profileImg: Uri?) {
        if (idToken.isNullOrEmpty() || email.isNullOrEmpty()) {
            showLoginFailure()
            return
        }

        setLoading(true)
        viewModelScope.launch {
            signInUseCase(idToken).onSuccess { uid ->
                isPinUse = true
                saveUserSessionUseCase(uid, email, name, profileImg?.toString())
                authenticate()
            }.onFailure {
                _events.emit(LoginEvent.ShowLoginFailure)
            }
            setLoading(false)
        }
    }

    fun loginForApiHigher() {
        setLoading(true)
        viewModelScope.launch {
            getGoogleCredentialUseCase().onFailure {
                setLoading(false)
                _events.emit(LoginEvent.RequestLegacySignIn)
            }.onSuccess { credential ->
                signInUseCase(credential.idToken).onSuccess { uid ->
                    isPinUse = true
                    saveUserSessionUseCase(
                        uid,
                        credential.id,
                        credential.displayName,
                        credential.profilePictureUri?.toString()
                    )
                    authenticate()
                }.onFailure {
                    _events.emit(LoginEvent.ShowLoginFailure)
                }
                setLoading(false)
            }
        }
    }

    private fun authenticate() {
        _uiState.value = _uiState.value.copy(isAuthenticated = true)
    }

    private fun setLoading(isLoading: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = isLoading)
    }

    private fun showLoginFailure() {
        viewModelScope.launch {
            _events.emit(LoginEvent.ShowLoginFailure)
        }
    }
}

data class LoginUiState(
    val isFirstLogin: Boolean,
    val isAuthenticated: Boolean,
    val isLoading: Boolean = false
)

sealed interface LoginEvent {
    data class LaunchSignIn(val intent: android.content.Intent) : LoginEvent
    data object RequestLegacySignIn : LoginEvent
    data object ShowLoginFailure : LoginEvent
}
