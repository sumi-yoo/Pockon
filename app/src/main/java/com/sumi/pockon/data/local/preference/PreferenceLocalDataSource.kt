package com.sumi.pockon.data.local.preference

import android.content.SharedPreferences
import com.sumi.pockon.util.SharedPreferenceKeys
import javax.inject.Inject

class PreferenceLocalDataSource @Inject constructor(
    private val prefs: SharedPreferences
) {

    fun getUid(): String = prefs.getString(SharedPreferenceKeys.UID, "") ?: ""

    fun getEmail(): String = prefs.getString(SharedPreferenceKeys.EMAIL, "") ?: ""

    fun getName(): String? = prefs.getString(SharedPreferenceKeys.NAME, null)

    fun getProfileImage(): String? = prefs.getString(SharedPreferenceKeys.PROFILE_IMAGE, null)

    fun getPinNum(): String = prefs.getString(SharedPreferenceKeys.PIN_NUM, "") ?: ""

    fun isAuthPin() = prefs.getBoolean(SharedPreferenceKeys.AUTH_PIN, false)

    fun isGuestMode() = prefs.getBoolean(SharedPreferenceKeys.GUEST_MODE, false)

    fun isFirstLogin() = prefs.getBoolean(SharedPreferenceKeys.FIRST_LOGIN, true)

    fun isPermRationale() = prefs.getBoolean(SharedPreferenceKeys.PERM_RATIONALE, false)

    fun isNotiEndDt() = prefs.getBoolean(SharedPreferenceKeys.NOTI_END_DT, true)

    fun getNotiEndDtDay() = prefs.getInt(SharedPreferenceKeys.NOTI_END_DT_DAY, 0)

    fun getNotiEndDtHour() = prefs.getInt(SharedPreferenceKeys.NOTI_END_DT_HOUR, 10)

    fun getNotiEndDtMinute() = prefs.getInt(SharedPreferenceKeys.NOTI_END_DT_MINUTE, 0)

    fun saveUid(uid: String) {
        prefs.edit().putString(SharedPreferenceKeys.UID, uid).apply()
    }

    fun saveEmail(email: String) {
        prefs.edit().putString(SharedPreferenceKeys.EMAIL, email).apply()
    }

    fun saveName(name: String?) {
        prefs.edit().putString(SharedPreferenceKeys.NAME, name).apply()
    }

    fun saveProfileImage(profileImage: String?) {
        prefs.edit().putString(SharedPreferenceKeys.PROFILE_IMAGE, profileImage).apply()
    }

    fun savePinNum(pinNum: String) {
        prefs.edit().putString(SharedPreferenceKeys.PIN_NUM, pinNum).apply()
    }

    fun saveIsAuthPin(isAuthPin: Boolean) {
        prefs.edit().putBoolean(SharedPreferenceKeys.AUTH_PIN, isAuthPin).apply()
    }

    fun saveIsGuestMode(isGuestMode: Boolean) {
        prefs.edit().putBoolean(SharedPreferenceKeys.GUEST_MODE, isGuestMode).apply()
    }

    fun saveIsFirstLogin(isFirstLogin: Boolean) {
        prefs.edit().putBoolean(SharedPreferenceKeys.FIRST_LOGIN, isFirstLogin).apply()
    }

    fun saveIsPermRationale(isPermRationale: Boolean) {
        prefs.edit().putBoolean(SharedPreferenceKeys.PERM_RATIONALE, isPermRationale).apply()
    }

    fun saveIsNotiEndDt(isNotiEndDtDay: Boolean) {
        prefs.edit().putBoolean(SharedPreferenceKeys.NOTI_END_DT, isNotiEndDtDay).apply()
    }

    fun saveNotiEndDtDay(day: Int) {
        prefs.edit().putInt(SharedPreferenceKeys.NOTI_END_DT_DAY, day).apply()
    }

    fun saveNotiEndDtHour(hour: Int) {
        prefs.edit().putInt(SharedPreferenceKeys.NOTI_END_DT_HOUR, hour).apply()
    }

    fun saveNotiEndDtMinute(minute: Int) {
        prefs.edit().putInt(SharedPreferenceKeys.NOTI_END_DT_MINUTE, minute).apply()
    }

    fun removePinNum() {
        prefs.edit().remove(SharedPreferenceKeys.PIN_NUM).apply()
    }

    fun removeAuthPin() {
        prefs.edit().putBoolean(SharedPreferenceKeys.AUTH_PIN, false).apply()
    }

    fun removeAll() {
        prefs.edit().clear().apply()
    }
}
