package com.sumi.pockon.ui.settings

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumi.pockon.domain.usecase.DeleteAccountUseCase
import com.sumi.pockon.domain.usecase.GetGoogleCredentialUseCase
import com.sumi.pockon.domain.usecase.LogoutUseCase
import com.sumi.pockon.domain.usecase.UpdateNotificationSettingsUseCase
import com.sumi.pockon.domain.usecase.GetUserSessionUseCase
import com.sumi.pockon.domain.repository.NotificationSettingsRepository
import com.sumi.pockon.domain.repository.PinRepository
import com.sumi.pockon.util.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getGoogleCredentialUseCase: GetGoogleCredentialUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val updateNotificationSettingsUseCase: UpdateNotificationSettingsUseCase,
    private val notificationSettingsRepository: NotificationSettingsRepository,
    private val pinRepository: PinRepository,
    private val getUserSessionUseCase: GetUserSessionUseCase,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events

    private val session = getUserSessionUseCase()
    private var uid = session.uid
    private var isAuthPin = pinRepository.isEnabled()
    private var notificationSettings = notificationSettingsRepository.get()
    private var isGuestMode = session.isGuest
    private var profileImage = session.profileImage
    private var name = session.name
    private var email = session.email

    private val _isShowNoInternetDialog = mutableStateOf(false)
    val isShowNoInternetDialog: State<Boolean> = _isShowNoInternetDialog

    fun getIsNotiEndDt() = notificationSettings.isEnabled

    fun getIsAuthPin() = isAuthPin

    fun onOffNotiEndDt(flag: Boolean) {
        notificationSettings = notificationSettings.copy(isEnabled = flag)
        viewModelScope.launch { updateNotificationSettingsUseCase(notificationSettings) }
    }

    fun offAuthPin() {
        pinRepository.disable()
        isAuthPin = false
    }

    fun logout() {
        viewModelScope.launch { logoutUseCase(isGuestMode) }
    }

    fun removeAccountWithCredential() {
        if (isGuestMode) {
            removeAccount(null)
            return
        }

        viewModelScope.launch {
            getGoogleCredentialUseCase().onSuccess { credential ->
                removeAccount(credential)
            }.onFailure {
                _events.emit(SettingsEvent.AccountDeletionFailed)
            }
        }
    }

    private fun removeAccount(credential: com.google.android.libraries.identity.googleid.GoogleIdTokenCredential?) {
        if (!isGuestMode && !networkMonitor.isConnected()) {
            _isShowNoInternetDialog.value = true
            return
        }

        viewModelScope.launch {
            deleteAccountUseCase(isGuestMode, uid, credential).fold(
                onSuccess = { _events.emit(SettingsEvent.AccountDeleted) },
                onFailure = { _events.emit(SettingsEvent.AccountDeletionFailed) }
            )
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
    data object AccountDeleted : SettingsEvent
    data object AccountDeletionFailed : SettingsEvent
}
