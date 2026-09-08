package com.sumi.pockon.ui.settings

import android.content.Intent
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.sumi.pockon.data.repository.PreferenceRepository
import com.sumi.pockon.data.repository.GiftRepository
import com.sumi.pockon.domain.usecase.DeleteAccountUseCase
import com.sumi.pockon.domain.usecase.GetGoogleCredentialUseCase
import com.sumi.pockon.domain.usecase.GetSignInIntentUseCase
import com.sumi.pockon.domain.usecase.SignOutUseCase
import com.sumi.pockon.data.model.Gift
import com.sumi.pockon.data.repository.AlarmRepository
import com.sumi.pockon.domain.usecase.ClearBrandCacheUseCase
import com.sumi.pockon.util.NetworkMonitor
import com.sumi.pockon.util.loadImageFromPath
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSignInIntentUseCase: GetSignInIntentUseCase,
    private val getGoogleCredentialUseCase: GetGoogleCredentialUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val giftRepository: GiftRepository,
    private val clearBrandCacheUseCase: ClearBrandCacheUseCase,
    private val preferenceRepository: PreferenceRepository,
    private val alarmRepository: AlarmRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

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
            giftRepository.getAllGift(1).take(1).collectLatest { allGift ->
                allGift.forEach { gift ->
                    val tempGift = Gift(
                        id = gift.id,
                        uid = gift.uid,
                        photo = loadImageFromPath(gift.photoPath),
                        name = gift.name,
                        brand = gift.brand,
                        endDt = gift.endDt,
                        addDt = gift.addDt,
                        memo = gift.memo,
                        usedDt = gift.usedDt,
                        cash = gift.cash,
                        isFavorite = gift.isFavorite
                    )
                    alarmRepository.cancelAlarm(tempGift.id, preferenceRepository.getNotiEndDtDay())
                    if (isNotiEndDt && tempGift.usedDt.isEmpty()) {
                        // 알림 등록
                        alarmRepository.setAlarm(tempGift, preferenceRepository.getNotiEndDtDay(), preferenceRepository.getNotiEndDtTime())
                    }
                }
            }
        }
    }

    fun offAuthPin() {
        preferenceRepository.offAuthPin()
    }

    fun logout() {
        if (!isGuestMode) signOutUseCase()
        preferenceRepository.removeAll()
        viewModelScope.launch(Dispatchers.IO) {
            giftRepository.getAllGift(1).take(1).collectLatest { gifts ->
                gifts.forEach { gift ->
                    alarmRepository.cancelAlarm(gift.id, preferenceRepository.getNotiEndDtDay())
                }
                giftRepository.deleteAllGift()
                clearBrandCacheUseCase()
            }
        }
    }

    fun getSignInIntent(onComplete: (Intent) -> Unit) {
        viewModelScope.launch {
            getSignInIntentUseCase(preferenceRepository.getEmail()).onSuccess(onComplete)
        }
    }

    fun getIdToken(onComplete: (GoogleIdTokenCredential?) -> Unit) {
        viewModelScope.launch {
            onComplete(getGoogleCredentialUseCase().getOrNull())
        }
    }

    fun removeAccount(idToken: String?, credential: GoogleIdTokenCredential? = null, onSuccess: (Boolean) -> Unit) {
        if (!isGuestMode && !networkMonitor.isConnected()) {
            onSuccess(false)
            _isShowNoInternetDialog.value = true
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            giftRepository.getAllGift(1).take(1).collectLatest { gifts ->
                // remote 데이터 삭제
                giftRepository.removeGifts(isGuestMode, uid, gifts.map { it.id }) { result ->
                    if (result) {
                        // local 데이터 삭제
                        viewModelScope.launch(Dispatchers.IO) {
                            gifts.forEach { gift ->
                                alarmRepository.cancelAlarm(gift.id, preferenceRepository.getNotiEndDtDay())
                            }
                            giftRepository.deleteAllGift()
                            clearBrandCacheUseCase()
                        }
                        if (!isGuestMode) {
                            if (credential == null) {
                                viewModelScope.launch {
                                    deleteAccountUseCase(idToken).onSuccess {
                                        signOutUseCase()
                                        preferenceRepository.removeAll()
                                        onSuccess(true)
                                    }.onFailure { onSuccess(false) }
                                }
                            } else {
                                viewModelScope.launch {
                                    deleteAccountUseCase(preferenceRepository.getEmail(), credential).onSuccess {
                                        signOutUseCase()
                                        preferenceRepository.removeAll()
                                        onSuccess(true)
                                    }.onFailure { onSuccess(false) }
                                }
                            }
                        } else {
                            onSuccess(true)
                        }
                    } else {
                        onSuccess(false)
                    }
                }
            }
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
