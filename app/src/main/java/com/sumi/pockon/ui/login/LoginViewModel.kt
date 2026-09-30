package com.sumi.pockon.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumi.pockon.core.network.NetworkStatusProvider
import com.sumi.pockon.domain.usecase.GetGoogleCredentialUseCase
import com.sumi.pockon.domain.usecase.SignInUseCase
import com.sumi.pockon.domain.usecase.GetUserSessionUseCase
import com.sumi.pockon.domain.repository.PinRepository
import com.sumi.pockon.domain.repository.UserSessionRepository
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
    private val signInUseCase: SignInUseCase,
    private val pinRepository: PinRepository,
    private val userSessionRepository: UserSessionRepository,
    private val getUserSessionUseCase: GetUserSessionUseCase,
    networkStatusProvider: NetworkStatusProvider
) : ViewModel() {

    val isNetworkConnected = networkStatusProvider.isConnected

    private val session = getUserSessionUseCase()
    private val _uiState = MutableStateFlow(
        LoginUiState(
            isFirstLogin = session.uid.isEmpty(),
            isAuthenticated = session.uid.isNotEmpty()
        )
    )
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _loginFailureEvents = MutableSharedFlow<Unit>()
    val loginFailureEvents: SharedFlow<Unit> = _loginFailureEvents.asSharedFlow()

    private var isPinUse = pinRepository.isEnabled()

    fun getIsPinUse(): Boolean {
        return isPinUse
    }

    fun loginAsGuest() {
        isPinUse = true
        userSessionRepository.save(uid = UUID.randomUUID().toString(), isGuest = true)
        authenticate()
    }

    fun loginWithGoogleCredential() {
        setLoading(true)
        viewModelScope.launch {
            getGoogleCredentialUseCase().onFailure {
                setLoading(false)
                _loginFailureEvents.emit(Unit)
            }.onSuccess { credential ->
                signInUseCase(credential.idToken).onSuccess { uid ->
                    isPinUse = true
                    userSessionRepository.save(
                        uid,
                        credential.id,
                        credential.displayName,
                        credential.profilePictureUri?.toString()
                    )
                    authenticate()
                }.onFailure {
                    _loginFailureEvents.emit(Unit)
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

}

data class LoginUiState(
    val isFirstLogin: Boolean,
    val isAuthenticated: Boolean,
    val isLoading: Boolean = false
)
