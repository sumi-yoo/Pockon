package com.sumi.pockon.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumi.pockon.core.auth.GoogleCredentialProvider
import com.sumi.pockon.core.network.NetworkStatusProvider
import com.sumi.pockon.domain.usecase.DeleteAccountUseCase
import com.sumi.pockon.domain.usecase.LogoutUseCase
import com.sumi.pockon.domain.usecase.UpdateNotificationSettingsUseCase
import com.sumi.pockon.domain.usecase.GetUserSessionUseCase
import com.sumi.pockon.domain.repository.NotificationSettingsRepository
import com.sumi.pockon.domain.repository.PinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val googleCredentialProvider: GoogleCredentialProvider,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val updateNotificationSettingsUseCase: UpdateNotificationSettingsUseCase,
    private val notificationSettingsRepository: NotificationSettingsRepository,
    private val pinRepository: PinRepository,
    private val getUserSessionUseCase: GetUserSessionUseCase,
    networkStatusProvider: NetworkStatusProvider
) : ViewModel() {

    val isNetworkConnected = networkStatusProvider.isConnected

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
            googleCredentialProvider.getCredential().onSuccess { credential ->
                removeAccount(credential.idToken)
            }.onFailure {
                _events.emit(SettingsEvent.AccountDeletionFailed)
            }
        }
    }

    private fun removeAccount(idToken: String?) {
        viewModelScope.launch {
            deleteAccountUseCase(isGuestMode, uid, idToken).fold(
                onSuccess = { _events.emit(SettingsEvent.AccountDeleted) },
                onFailure = { _events.emit(SettingsEvent.AccountDeletionFailed) }
            )
        }
    }

    fun getName() = this.name

    fun getProfileImage() = this.profileImage

    fun getEmail() = this.email

    fun getIsGuestMode() = this.isGuestMode

}

sealed interface SettingsEvent {
    data object AccountDeleted : SettingsEvent
    data object AccountDeletionFailed : SettingsEvent
}
