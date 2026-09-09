package com.sumi.pockon.ui.add

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.sumi.pockon.R
import com.sumi.pockon.domain.usecase.GetUserSessionUseCase
import com.sumi.pockon.data.model.Gift
import com.sumi.pockon.domain.usecase.AddGiftUseCase
import com.sumi.pockon.util.GifticonParser
import com.sumi.pockon.util.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AddViewModel @Inject constructor(
    private val addGiftUseCase: AddGiftUseCase,
    private val getUserSessionUseCase: GetUserSessionUseCase,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _events = MutableSharedFlow<AddGiftEvent>()
    val events: SharedFlow<AddGiftEvent> = _events.asSharedFlow()

    private val session = getUserSessionUseCase()
    private val uid = session.uid
    private val isGuestMode = session.isGuest

    private val _uiState = MutableStateFlow(AddUiState())
    val uiState: StateFlow<AddUiState> = _uiState.asStateFlow()

    fun setGift(index: Int, value: String) {
        when (index) {
            0 -> _uiState.value = _uiState.value.copy(name = value)
            1 -> _uiState.value = _uiState.value.copy(brand = value)
            2 -> _uiState.value = _uiState.value.copy(cash = value)
            3 -> _uiState.value = _uiState.value.copy(endDate = value)
            4 -> _uiState.value = _uiState.value.copy(memo = value)
        }
    }

    fun addGift() {
        if (!isGuestMode && !networkMonitor.isConnected()) {
            _uiState.value = _uiState.value.copy(isShowNoInternetDialog = true)
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)
        val addDate = SimpleDateFormat(
            "yyyyMMddHHmmss",
            Locale.getDefault()
        ).format(Date(System.currentTimeMillis()))

        val state = _uiState.value
        val gift = if (state.isCheckedCash) {
            Gift(
                uid = uid,
                name = state.name.trim(),
                photo = state.photo,
                brand = state.brand.trim(),
                endDt = state.endDate,
                addDt = addDate,
                memo = state.memo,
                cash = state.cash,
                isFavorite = false
            )
        } else {
            Gift(
                uid = uid,
                name = state.name.trim(),
                photo = state.photo,
                brand = state.brand.trim(),
                endDt = state.endDate,
                addDt = addDate,
                memo = state.memo,
                isFavorite = false
            )
        }
        viewModelScope.launch {
            val result = runCatching { addGiftUseCase(isGuestMode, gift) }.getOrNull()
            _uiState.value = _uiState.value.copy(isLoading = false)
            _events.emit(
                if (result?.isSuccess == true) AddGiftEvent.Saved else AddGiftEvent.SaveFailed
            )
        }
    }

    fun setPhoto(photo: Bitmap?) {
        _uiState.value = _uiState.value.copy(photo = photo)
//        photo?.let { analyzeImage(it) }
    }

    fun changeDatePickerState() {
        _uiState.value = _uiState.value.copy(isShowDatePicker = !_uiState.value.isShowDatePicker)
    }

    fun changeNoInternetDialogState() {
        _uiState.value = _uiState.value.copy(isShowNoInternetDialog = false)
    }

    fun isValid(): Int? {
        var msg: Int? = null
        val state = _uiState.value
        if (state.photo == null) {
            msg = R.string.msg_no_photo
        } else if (state.name.isEmpty()) {
            msg = R.string.msg_no_name
        } else if (state.brand.isEmpty()) {
            msg = R.string.msg_no_brand
        } else if (state.endDate.isEmpty() || state.endDate.length < 8) {
            msg = R.string.msg_no_end_date
        } else if (state.isCheckedCash && state.cash.isEmpty()) {
            msg = R.string.msg_no_cash
        }

        return msg
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

    fun chgCheckedCash() {
        _uiState.value = _uiState.value.copy(isCheckedCash = !_uiState.value.isCheckedCash)
    }

    private fun analyzeImage(bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
                val result = recognizer.process(bitmap, 0).await()
                val info = GifticonParser.parse(result.text)
                _uiState.value = _uiState.value.copy(
                    name = info.name,
                    brand = info.brand,
                    cash = info.cash,
                    endDate = info.endDate,
                    isCheckedCash = info.cash.isNotEmpty()
                )
            } catch (_: Exception) { }
        }
    }
}

data class AddUiState(
    val photo: Bitmap? = null,
    val name: String = "",
    val brand: String = "",
    val cash: String = "",
    val endDate: String = "",
    val memo: String = "",
    val isShowDatePicker: Boolean = false,
    val isCheckedCash: Boolean = false,
    val isLoading: Boolean = false,
    val isShowNoInternetDialog: Boolean = false
)

sealed interface AddGiftEvent {
    data object Saved : AddGiftEvent
    data object SaveFailed : AddGiftEvent
}
