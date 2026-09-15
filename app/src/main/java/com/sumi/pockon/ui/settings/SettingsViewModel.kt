package com.sumi.pockon.ui.settings

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumi.pockon.domain.usecase.DeleteAccountUseCase
import com.sumi.pockon.domain.usecase.GetGoogleCredentialUseCase
import com.sumi.pockon.domain.usecase.SignOutUseCase
import com.sumi.pockon.domain.usecase.DeleteGiftsUseCase
import com.sumi.pockon.domain.usecase.ObserveAllGiftsUseCase
import com.sumi.pockon.domain.usecase.ClearAllGiftsUseCase
import com.sumi.pockon.domain.usecase.CancelGiftAlarmUseCase
import com.sumi.pockon.domain.usecase.ScheduleGiftAlarmUseCase
import com.sumi.pockon.domain.usecase.DisablePinUseCase
import com.sumi.pockon.domain.usecase.IsPinEnabledUseCase
import com.sumi.pockon.domain.usecase.GetUserSessionUseCase
import com.sumi.pockon.domain.usecase.ClearUserSessionUseCase
import com.sumi.pockon.domain.usecase.ClearBrandCacheUseCase
import com.sumi.pockon.domain.usecase.GetNotificationSettingsUseCase
import com.sumi.pockon.domain.usecase.SaveNotificationSettingsUseCase
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
    private val getGoogleCredentialUseCase: GetGoogleCredentialUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val deleteGiftsUseCase: DeleteGiftsUseCase,
    private val observeAllGiftsUseCase: ObserveAllGiftsUseCase,
    private val clearAllGiftsUseCase: ClearAllGiftsUseCase,
    private val clearBrandCacheUseCase: ClearBrandCacheUseCase,
    private val getNotificationSettingsUseCase: GetNotificationSettingsUseCase,
    private val saveNotificationSettingsUseCase: SaveNotificationSettingsUseCase,
    private val cancelGiftAlarmUseCase: CancelGiftAlarmUseCase,
    private val scheduleGiftAlarmUseCase: ScheduleGiftAlarmUseCase,
    private val disablePinUseCase: DisablePinUseCase,
    private val isPinEnabledUseCase: IsPinEnabledUseCase,
    private val getUserSessionUseCase: GetUserSessionUseCase,
    private val clearUserSessionUseCase: ClearUserSessionUseCase,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events

    private val session = getUserSessionUseCase()
    private var uid = session.uid
    private var isAuthPin = isPinEnabledUseCase()
    private var notificationSettings = getNotificationSettingsUseCase()
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
        saveNotificationSettingsUseCase(notificationSettings)

        viewModelScope.launch(Dispatchers.IO) {
            observeAllGiftsUseCase().take(1).collectLatest { allGift ->
                allGift.forEach { gift ->
                    cancelGiftAlarmUseCase(gift.id, notificationSettings.daysBeforeExpiry)
                    if (notificationSettings.isEnabled && gift.usedDt.isEmpty()) {
                        // 알림 등록
                        scheduleGiftAlarmUseCase(
                            gift,
                            notificationSettings.daysBeforeExpiry,
                            notificationSettings.hour to notificationSettings.minute
                        )
                    }
                }
            }
        }
    }

    fun offAuthPin() {
        disablePinUseCase()
        isAuthPin = false
    }

    fun logout() {
        if (!isGuestMode) signOutUseCase()
        clearUserSessionUseCase()
        viewModelScope.launch(Dispatchers.IO) {
            observeAllGiftsUseCase().take(1).collectLatest { gifts ->
                gifts.forEach { gift ->
                    cancelGiftAlarmUseCase(gift.id, notificationSettings.daysBeforeExpiry)
                }
                clearAllGiftsUseCase()
                clearBrandCacheUseCase()
            }
        }
    }

    fun removeAccountWithCredential() {
        viewModelScope.launch {
            getGoogleCredentialUseCase().onSuccess { credential ->
                removeAccount(credential)
            }.onFailure {
                _events.emit(SettingsEvent.AccountDeletionFailed)
            }
        }
    }

    private fun removeAccount(credential: com.google.android.libraries.identity.googleid.GoogleIdTokenCredential) {
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
                deleteAccountUseCase(credential)
            }
            if (accountDeletion.isFailure) {
                _events.emit(SettingsEvent.AccountDeletionFailed)
                return@launch
            }

            gifts.forEach { gift ->
                cancelGiftAlarmUseCase(gift.id, notificationSettings.daysBeforeExpiry)
            }
            clearAllGiftsUseCase()
            clearBrandCacheUseCase()
            if (!isGuestMode) signOutUseCase()
            clearUserSessionUseCase()
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
    data object AccountDeleted : SettingsEvent
    data object AccountDeletionFailed : SettingsEvent
}
