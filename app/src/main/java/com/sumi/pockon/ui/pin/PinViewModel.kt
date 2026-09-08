package com.sumi.pockon.ui.pin

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumi.pockon.R
import com.sumi.pockon.domain.usecase.SavePinUseCase
import com.sumi.pockon.domain.usecase.GetPinUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PinViewModel @Inject constructor(
    private val savePinUseCase: SavePinUseCase,
    private val getPinUseCase: GetPinUseCase
) : ViewModel() {

    private val pinNumber = getPinUseCase()
    private val pinSize = 6
    private var checkPin = ""

    var inputPin = mutableStateListOf<Int>()
        private set

    private val _mode = mutableIntStateOf(0)
    val mode: State<Int> = _mode

    private val _error = mutableStateOf<Int?>(null)
    val error: State<Int?> = _error

    private val _showSuccess = mutableStateOf(false)
    val showSuccess: State<Boolean> = _showSuccess

    init {
        _mode.intValue = if (pinNumber.isEmpty()) 0 else 2
    }

    fun setMode(mode: Int) {
        this._mode.intValue = mode
        _error.value = null
        inputPin.clear()
    }

    fun getPinSize(): Int {
        return pinSize
    }

    fun addPinNum(num: Int) {
        inputPin.add(num)
        comparePinNum()
    }

    fun removeLastPin() {
        inputPin.removeAt(inputPin.lastIndex)
    }

    fun getTitle(): Int {
        return when (this._mode.intValue) {
            0 -> R.string.txt_create_pin
            1 -> R.string.txt_check_pin
            else -> R.string.txt_input_pin
        }
    }

    private fun comparePinNum() {
        if (inputPin.size == pinSize) {
            viewModelScope.launch {
                delay(300)
                when (_mode.intValue) {
                    0 -> {
                        _mode.intValue = 1
                        checkPin = inputPin.joinToString("")
                        inputPin.clear()
                        _error.value = null
                    }

                    1 -> {
                        if (inputPin.joinToString("") == checkPin) {
                            savePinUseCase(checkPin)
                            _error.value = null
                            _showSuccess.value = true
                        } else {
                            inputPin.clear()
                            _error.value = R.string.msg_pin_auth_fail
                        }
                    }

                    else -> {
                        if (inputPin.joinToString("") == pinNumber) {
                            _error.value = null
                            _showSuccess.value = true
                        } else {
                            inputPin.clear()
                            _error.value = R.string.msg_pin_auth_fail
                        }
                    }
                }
            }
        }
    }
}
