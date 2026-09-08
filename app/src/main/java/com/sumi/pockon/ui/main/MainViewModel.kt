package com.sumi.pockon.ui.main

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.sumi.pockon.domain.usecase.GetNotificationSettingsUseCase
import com.sumi.pockon.domain.usecase.IsNotificationPermissionRationaleShownUseCase
import com.sumi.pockon.domain.usecase.MarkNotificationPermissionRationaleShownUseCase
import com.sumi.pockon.domain.usecase.SaveNotificationSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getNotificationSettingsUseCase: GetNotificationSettingsUseCase,
    private val saveNotificationSettingsUseCase: SaveNotificationSettingsUseCase,
    private val isNotificationPermissionRationaleShownUseCase: IsNotificationPermissionRationaleShownUseCase,
    private val markNotificationPermissionRationaleShownUseCase: MarkNotificationPermissionRationaleShownUseCase,
) : ViewModel() {

    private val _isPermRationale = mutableStateOf(isNotificationPermissionRationaleShownUseCase())
    val isPermRationale: State<Boolean> = _isPermRationale

    fun disableNotification() {
        saveNotificationSettingsUseCase(getNotificationSettingsUseCase().copy(isEnabled = false))
    }

    fun saveIsPermRationale() {
        if (!_isPermRationale.value) markNotificationPermissionRationaleShownUseCase()
        _isPermRationale.value = true
    }
}
