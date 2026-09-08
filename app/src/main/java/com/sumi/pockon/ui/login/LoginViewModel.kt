package com.sumi.pockon.ui.login

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumi.pockon.domain.usecase.GetGoogleCredentialUseCase
import com.sumi.pockon.domain.usecase.GetSignInIntentUseCase
import com.sumi.pockon.domain.usecase.SignInUseCase
import com.sumi.pockon.domain.usecase.IsPinEnabledUseCase
import com.sumi.pockon.domain.usecase.SaveUserSessionUseCase
import com.sumi.pockon.domain.usecase.GetUserSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
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

    private val _events = MutableSharedFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = _events

    private val _isLogin = MutableLiveData(false)
    val isLogin: LiveData<Boolean> = _isLogin

    private val _isFail = MutableLiveData(false)
    val isFail: LiveData<Boolean> = _isFail

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val session = getUserSessionUseCase()
    private val _isFirstLogin = MutableLiveData(session.uid.isEmpty())
    val isFirstLogin: LiveData<Boolean> = _isFirstLogin

    private var isPinUse = isPinEnabledUseCase()

    init {
        if (session.uid.isNotEmpty()) {
            _isLogin.postValue(true)
        }
    }

    fun getIsPinUse(): Boolean {
        return isPinUse
    }

    fun requestSignInIntent() {
        viewModelScope.launch {
            getSignInIntentUseCase(session.email).onSuccess { intent ->
                _events.emit(LoginEvent.LaunchSignIn(intent))
            }.onFailure {
                _isFail.value = true
            }
        }
    }

    fun loginAsGuest() {
        isPinUse = true
        _isLogin.postValue(true)
        saveUserSessionUseCase(uid = UUID.randomUUID().toString(), isGuest = true)
    }

    fun loginForApiLower(idToken: String?, email: String?, name: String?, profileImg: Uri?) {
        if (idToken.isNullOrEmpty() || email.isNullOrEmpty()) {
            _isLogin.postValue(false)
            _isFail.postValue(true)
            return
        }

        _isLoading.postValue(true)
        viewModelScope.launch {
            signInUseCase(idToken).onSuccess { uid ->
                isPinUse = true
                _isLogin.value = true
                saveUserSessionUseCase(uid, email, name, profileImg?.toString())
            }.onFailure {
                _isLogin.postValue(false)
                _isFail.postValue(true)
            }
            _isLoading.postValue(false)
        }
    }

    fun loginForApiHigher() {
        _isLoading.postValue(true)
        viewModelScope.launch {
            getGoogleCredentialUseCase().onFailure {
                _isLoading.postValue(false)
                _events.emit(LoginEvent.RequestLegacySignIn)
            }.onSuccess { credential ->
                signInUseCase(credential.idToken).onSuccess { uid ->
                    isPinUse = true
                    _isLogin.postValue(true)
                    saveUserSessionUseCase(
                        uid,
                        credential.id,
                        credential.displayName,
                        credential.profilePictureUri?.toString()
                    )
                }.onFailure {
                    _isLogin.postValue(false)
                    _isFail.postValue(true)
                }
                _isLoading.postValue(false)
            }
        }
    }
}

sealed interface LoginEvent {
    data class LaunchSignIn(val intent: android.content.Intent) : LoginEvent
    data object RequestLegacySignIn : LoginEvent
}
