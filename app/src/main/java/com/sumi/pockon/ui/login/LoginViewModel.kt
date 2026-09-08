package com.sumi.pockon.ui.login

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumi.pockon.data.repository.PreferenceRepository
import com.sumi.pockon.domain.usecase.GetGoogleCredentialUseCase
import com.sumi.pockon.domain.usecase.GetSignInIntentUseCase
import com.sumi.pockon.domain.usecase.SignInUseCase
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
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {

    private val _events = MutableSharedFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = _events

    private val _isLogin = MutableLiveData(false)
    val isLogin: LiveData<Boolean> = _isLogin

    private val _isFail = MutableLiveData(false)
    val isFail: LiveData<Boolean> = _isFail

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isFirstLogin = MutableLiveData(preferenceRepository.getUid().isEmpty())
    val isFirstLogin: LiveData<Boolean> = _isFirstLogin

    private var isPinUse = preferenceRepository.isAuthPin()

    init {
        if (preferenceRepository.getUid().isNotEmpty()) {
            _isLogin.postValue(true)
        }
    }

    fun getIsPinUse(): Boolean {
        return isPinUse
    }

    fun requestSignInIntent() {
        viewModelScope.launch {
            getSignInIntentUseCase(preferenceRepository.getEmail()).onSuccess { intent ->
                _events.emit(LoginEvent.LaunchSignIn(intent))
            }.onFailure {
                _isFail.value = true
            }
        }
    }

    fun loginAsGuest() {
        isPinUse = true
        _isLogin.postValue(true)
        preferenceRepository.saveUid(UUID.randomUUID().toString())
        preferenceRepository.saveIsGuestMode(true)
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
                preferenceRepository.saveUid(uid)
                preferenceRepository.saveEmail(email)
                name?.let(preferenceRepository::saveName)
                profileImg?.let { preferenceRepository.saveProfileImage(it.toString()) }
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
                    preferenceRepository.saveUid(uid)
                    preferenceRepository.saveEmail(credential.id)
                    credential.displayName?.let { name -> preferenceRepository.saveName(name) }
                    credential.profilePictureUri?.let { uri -> preferenceRepository.saveProfileImage(uri.toString()) }
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
