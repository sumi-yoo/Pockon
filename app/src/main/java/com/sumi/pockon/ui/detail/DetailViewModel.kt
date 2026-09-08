package com.sumi.pockon.ui.detail

import android.graphics.Bitmap
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumi.pockon.R
import com.sumi.pockon.data.repository.PreferenceRepository
import com.sumi.pockon.data.repository.GiftRepository
import com.sumi.pockon.data.model.Gift
import com.sumi.pockon.data.repository.AlarmRepository
import com.sumi.pockon.domain.usecase.UpdateGiftFavoriteUseCase
import com.sumi.pockon.domain.usecase.UpdateGiftUseCase
import com.sumi.pockon.util.NetworkMonitor
import com.sumi.pockon.util.loadImageFromPath
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val legacyGiftRepository: GiftRepository,
    private val updateGiftUseCase: UpdateGiftUseCase,
    private val updateGiftFavoriteUseCase: UpdateGiftFavoriteUseCase,
    private val alarmRepository: AlarmRepository,
    private val preferenceRepository: PreferenceRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _events = MutableSharedFlow<DetailEvent>()
    val events: SharedFlow<DetailEvent> = _events

    private val isGuestMode = preferenceRepository.isGuestMode()

    private val _gift = mutableStateOf(Gift())
    val gift: State<Gift> = _gift

    private val _photo = mutableStateOf<Bitmap?>(null)
    val photo: State<Bitmap?> = _photo
    private val _name = mutableStateOf("")
    val name: State<String> = _name
    private val _brand = mutableStateOf("")
    val brand: State<String> = _brand
    private val _cash = mutableStateOf("")
    val cash: State<String> = _cash
    private val _endDate = mutableStateOf("")
    val endDate: State<String> = _endDate
    private val _memo = mutableStateOf("")
    val memo: State<String> = _memo
    private val _usedDt = mutableStateOf("")
    val usedDt: State<String> = _usedDt
    private val _isFavorite = mutableStateOf(false)
    val isFavorite: State<Boolean> = _isFavorite

    private val _isShowBottomSheet = mutableStateOf(false)
    val isShowBottomSheet: State<Boolean> = _isShowBottomSheet

    private val _isShowCancelDialog = mutableStateOf(false)
    val isShowCancelDialog: State<Boolean> = _isShowCancelDialog

    private val _isShowUseCashDialog = mutableStateOf(false)
    val isShowUseCashDialog: State<Boolean> = _isShowUseCashDialog

    private val _isShowDatePicker = mutableStateOf(false)
    val isShowDatePicker: State<Boolean> = _isShowDatePicker

    private val _isCheckedCash = mutableStateOf(false)
    val isCheckedCash: State<Boolean> = _isCheckedCash

    private val _isEdit = mutableStateOf(false)
    val isEdit: State<Boolean> = _isEdit

    private val _isShowIndicator = mutableStateOf(false)
    val isShowIndicator: State<Boolean> = _isShowIndicator

    private val _isShowNoInternetDialog = mutableStateOf(false)
    val isShowNoInternetDialog: State<Boolean> = _isShowNoInternetDialog

    fun getGift(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            legacyGiftRepository.getGift(id).collectLatest { gift ->
                setGift(
                    Gift(
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
                )
            }
        }
    }

    private fun setGift(gift: Gift) {
        if (!_isEdit.value) this._gift.value = gift
        _name.value = gift.name
        _brand.value = gift.brand
        _cash.value = gift.cash
        _endDate.value = gift.endDt
        _memo.value = gift.memo
        _usedDt.value = gift.usedDt
        _photo.value = gift.photo
        _isCheckedCash.value = gift.cash.isNotEmpty()
        _isFavorite.value = gift.isFavorite
    }

    fun setGift(index: Int, value: String) {
        when (index) {
            0 -> _name.value = value
            1 -> _brand.value = value
            2 -> _cash.value = value
            3 -> _endDate.value = value
            4 -> _memo.value = value
        }
    }

    fun getLabelList(index: Int): Int {
        return when (index) {
            0 -> R.string.txt_name
            1 -> R.string.txt_brand
            2 -> R.string.txt_cash
            3 -> R.string.txt_end_date
            else -> R.string.txt_memo
        }
    }

    fun setIsShowBottomSheet(flag: Boolean) {
        _isShowBottomSheet.value = flag
    }

    fun setIsShowCancelDialog(flag: Boolean) {
        _isShowCancelDialog.value = flag
    }

    fun setIsShowUseCashDialog(flag: Boolean) {
        _isShowUseCashDialog.value = flag
    }

    fun setIsEdit(flag: Boolean) {
        _isEdit.value = flag
    }

    fun setPhoto(photo: Bitmap?) {
        _photo.value = photo
    }

    fun toggleFavorite() {
        if (!isGuestMode && !networkMonitor.isConnected()) {
            _isShowNoInternetDialog.value = true
            return
        }

        val isFavorite = !_isFavorite.value
        viewModelScope.launch {
            updateGiftFavoriteUseCase(
                isGuestMode = isGuestMode,
                id = _gift.value.id,
                isFavorite = isFavorite
            ).onSuccess {
                _isFavorite.value = isFavorite
            }
        }
    }

    fun updateGift() {
        if (!isGuestMode && !networkMonitor.isConnected()) {
            _isShowNoInternetDialog.value = true
            return
        }

        _isShowIndicator.value = true
        val updateGift = if (_isCheckedCash.value) {
            Gift(
                id = _gift.value.id,
                uid = _gift.value.uid,
                photo = _photo.value,
                name = _name.value.trim(),
                brand = _brand.value.trim(),
                endDt = _endDate.value,
                addDt = _gift.value.addDt,
                memo = _memo.value,
                usedDt = _gift.value.usedDt,
                cash = _cash.value,
                isFavorite = _isFavorite.value
            )
        } else {
            Gift(
                id = _gift.value.id,
                uid = _gift.value.uid,
                photo = _photo.value,
                name = _name.value.trim(),
                brand = _brand.value.trim(),
                endDt = _endDate.value,
                addDt = _gift.value.addDt,
                memo = _memo.value,
                usedDt = _gift.value.usedDt,
                cash = "",
                isFavorite = _isFavorite.value
            )
        }
        viewModelScope.launch {
            try {
                val result = updateGiftUseCase(
                    isGuestMode = isGuestMode,
                    gift = updateGift,
                    shouldUploadPhoto = true
                )

                if (result.isSuccess) {
                    alarmRepository.cancelAlarm(
                        updateGift.id,
                        preferenceRepository.getNotiEndDtDay()
                    )
                    _gift.value = updateGift
                    _isEdit.value = false
                    _events.emit(DetailEvent.GiftUpdated)
                } else {
                    _events.emit(DetailEvent.GiftUpdateFailed)
                }
            } finally {
                _isShowIndicator.value = false
            }
        }
    }

    fun setIsUsed(flag: Boolean, cash: Int? = null) {
        if (!isGuestMode && !networkMonitor.isConnected()) {
            _isShowNoInternetDialog.value = true
            return
        }

        _isShowIndicator.value = true
        var nowDt = ""
        if ((flag && cash == null) || (flag && cash == 0)) {
            nowDt = SimpleDateFormat(
                "yyyy.MM.dd",
                Locale.getDefault()
            ).format(Date(System.currentTimeMillis()))
        }
        val gift = if (cash == null) _gift.value.copy(usedDt = nowDt) else _gift.value.copy(
            usedDt = nowDt,
            cash = cash.toString()
        )
        viewModelScope.launch {
            try {
                val result = updateGiftUseCase(
                    isGuestMode = isGuestMode,
                    gift = gift,
                    shouldUploadPhoto = false
                )

                if (result.isSuccess) {
                    _gift.value = gift
                    _isShowBottomSheet.value = false
                    _isShowUseCashDialog.value = false
                } else {
                    _events.emit(DetailEvent.GiftUsageUpdateFailed(flag))
                }
            } finally {
                _isShowIndicator.value = false
            }
        }
    }

    fun chgCheckedCash() {
        _isCheckedCash.value = !_isCheckedCash.value
    }

    fun changeDatePickerState() {
        _isShowDatePicker.value = !_isShowDatePicker.value
    }

    fun changeNoInternetDialogState() {
        _isShowNoInternetDialog.value = !_isShowNoInternetDialog.value
    }

    fun isValid(): Int? {
        var msg: Int? = null
        if (_photo.value == null) {
            msg = R.string.msg_no_photo
        } else if (_name.value.isEmpty()) {
            msg = R.string.msg_no_name
        } else if (_brand.value.isEmpty()) {
            msg = R.string.msg_no_brand
        } else if (_endDate.value.isEmpty() || _endDate.value.length < 8) {
            msg = R.string.msg_no_end_date
        } else if (_isCheckedCash.value && _cash.value.isEmpty()) {
            msg = R.string.msg_no_cash
        }

        return msg
    }

    fun init() {
        _name.value = _gift.value.name
        _brand.value = _gift.value.brand
        _cash.value = _gift.value.cash
        _endDate.value = _gift.value.endDt
        _memo.value = _gift.value.memo
        _usedDt.value = _gift.value.usedDt
        _photo.value = _gift.value.photo
        _isCheckedCash.value = _gift.value.cash.isNotEmpty()
        _isFavorite.value = _gift.value.isFavorite
    }
}

sealed interface DetailEvent {
    data object GiftUpdated : DetailEvent
    data object GiftUpdateFailed : DetailEvent
    data class GiftUsageUpdateFailed(val isUsing: Boolean) : DetailEvent
}
