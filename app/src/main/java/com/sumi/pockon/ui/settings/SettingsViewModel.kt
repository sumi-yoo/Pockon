package com.sumi.pockon.ui.settings

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumi.pockon.data.repository.PreferenceRepository
import com.sumi.pockon.domain.usecase.DeleteAccountUseCase
import com.sumi.pockon.domain.usecase.GetGoogleCredentialUseCase
import com.sumi.pockon.domain.usecase.GetSignInIntentUseCase
import com.sumi.pockon.domain.usecase.SignOutUseCase
import com.sumi.pockon.domain.usecase.DeleteGiftsUseCase
import com.sumi.pockon.domain.usecase.ObserveAllGiftsUseCase
import com.sumi.pockon.domain.usecase.ClearAllGiftsUseCase
import com.sumi.pockon.data.model.Gift
import com.sumi.pockon.data.repository.AlarmRepository
import com.sumi.pockon.domain.usecase.ClearBrandCacheUseCase
import com.sumi.pockon.util.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSignInIntentUseCase: GetSignInIntentUseCase,
    private val getGoogleCredentialUseCase: GetGoogleCredentialUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val deleteGiftsUseCase: DeleteGiftsUseCase,
    private val observeAllGiftsUseCase: ObserveAllGiftsUseCase,
    private val clearAllGiftsUseCase: ClearAllGiftsUseCase,
    private val clearBrandCacheUseCase: ClearBrandCacheUseCase,
    private val preferenceRepository: PreferenceRepository,
    private val alarmRepository: AlarmRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events

    private var uid = preferenceRepository.getUid()
    private var isAuthPin = preferenceRepository.isAuthPin()
    private var isNotiEndDt = preferenceRepository.isNotiEndDt()
    private var isGuestMode = preferenceRepository.isGuestMode()
    private var profileImage = preferenceRepository.getProfileImage()
    private var name = preferenceRepository.getName()
    private var email = preferenceRepository.getEmail()

    private val _isShowNoInternetDialog = mutableStateOf(false)
    val isShowNoInternetDialog: State<Boolean> = _isShowNoInternetDialog

    fun getIsNotiEndDt() = isNotiEndDt

    fun getIsAuthPin() = isAuthPin

    fun onOffNotiEndDt(flag: Boolean) {
        preferenceRepository.onOffNotiEndDt(flag)
        isNotiEndDt = flag

        viewModelScope.launch(Dispatchers.IO) {
            observeAllGiftsUseCase().take(1).collectLatest { allGift ->
                allGift.forEach { gift ->
                    alarmRepository.cancelAlarm(gift.id, preferenceRepository.getNotiEndDtDay())
                    if (isNotiEndDt && gift.usedDt.isEmpty()) {
                        // 알림 등록
                        alarmRepository.setAlarm(gift, preferenceRepository.getNotiEndDtDay(), preferenceRepository.getNotiEndDtTime())
                    }
                }
            }
        }
    }

    fun offAuthPin() {
        preferenceRepository.offAuthPin()
        isAuthPin = false
    }

    fun logout() {
        if (!isGuestMode) signOutUseCase()
        preferenceRepository.removeAll()
        viewModelScope.launch(Dispatchers.IO) {
            observeAllGiftsUseCase().take(1).collectLatest { gifts ->
                gifts.forEach { gift ->
                    alarmRepository.cancelAlarm(gift.id, preferenceRepository.getNotiEndDtDay())
                }
                clearAllGiftsUseCase()
                clearBrandCacheUseCase()
            }
        }
    }

    fun requestLegacySignIn() {
        viewModelScope.launch {
            getSignInIntentUseCase(preferenceRepository.getEmail()).onSuccess { intent ->
                _events.emit(SettingsEvent.LaunchSignIn(intent))
            }.onFailure {
                _events.emit(SettingsEvent.AccountDeletionFailed)
            }
        }
    }

    fun removeAccountWithCredential() {
        viewModelScope.launch {
            getGoogleCredentialUseCase().onSuccess { credential ->
                removeAccount(preferenceRepository.getEmail(), credential)
            }.onFailure {
                _events.emit(SettingsEvent.AccountDeletionFailed)
            }
        }
    }

    fun removeAccount(idToken: String?, credential: com.google.android.libraries.identity.googleid.GoogleIdTokenCredential? = null) {
        if (!isGuestMode && !networkMonitor.isConnected()) {
            _isShowNoInternetDialog.value = true
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val gifts = observeAllGiftsUseCase().first()
            val remoteDeletion = deleteGiftsUseCase(isGuestMode, uid, gifts.map { it.id })
            if (remoteDeletion.isFailure) {
                _events.emit(SettingsEvent.AccountDeletionFailed)
                return@launch
            }

            val accountDeletion = if (isGuestMode) Result.success(Unit) else {
                deleteAccountUseCase(idToken, credential)
            }
            if (accountDeletion.isFailure) {
                _events.emit(SettingsEvent.AccountDeletionFailed)
                return@launch
            }

            gifts.forEach { gift -> alarmRepository.cancelAlarm(gift.id, preferenceRepository.getNotiEndDtDay()) }
            clearAllGiftsUseCase()
            clearBrandCacheUseCase()
            if (!isGuestMode) signOutUseCase()
            preferenceRepository.removeAll()
            _events.emit(SettingsEvent.AccountDeleted)
        }
    }

    fun getName() = this.name

    fun getProfileImage() = this.profileImage

    fun getEmail() = this.email

    fun getIsGuestMode() = this.isGuestMode

    fun changeNoInternetDialogState() {
        _isShowNoInternetDialog.value = !_isShowNoInternetDialog.value
    }
}

sealed interface SettingsEvent {
    data class LaunchSignIn(val intent: android.content.Intent) : SettingsEvent
    data object AccountDeleted : SettingsEvent
    data object AccountDeletionFailed : SettingsEvent
}
