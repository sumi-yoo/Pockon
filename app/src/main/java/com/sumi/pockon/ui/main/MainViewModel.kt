package com.sumi.pockon.ui.main

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumi.pockon.domain.repository.AppPreferencesRepository
import com.sumi.pockon.domain.usecase.UpdateNotificationSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val updateNotificationSettingsUseCase: UpdateNotificationSettingsUseCase,
    private val appPreferencesRepository: AppPreferencesRepository
) : ViewModel() {

    private val _isPermRationale = mutableStateOf(appPreferencesRepository.isNotificationPermissionRationaleShown())
    val isPermRationale: State<Boolean> = _isPermRationale

    fun disableNotification() {
        viewModelScope.launch { updateNotificationSettingsUseCase.disable() }
    }

    fun saveIsPermRationale() {
        if (!_isPermRationale.value) appPreferencesRepository.markNotificationPermissionRationaleShown()
        _isPermRationale.value = true
    }
}
