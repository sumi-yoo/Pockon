package com.sumi.pockon.domain.usecase

import com.sumi.pockon.domain.repository.AlarmRepository
import com.sumi.pockon.domain.repository.AuthRepository
import com.sumi.pockon.domain.repository.BrandRepository
import com.sumi.pockon.domain.repository.GiftRepository
import com.sumi.pockon.domain.repository.NotificationSettingsRepository
import com.sumi.pockon.domain.repository.UserSessionRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val giftRepository: GiftRepository,
    private val brandRepository: BrandRepository,
    private val notificationSettingsRepository: NotificationSettingsRepository,
    private val alarmRepository: AlarmRepository,
    private val userSessionRepository: UserSessionRepository
) {
    suspend operator fun invoke(
        isGuestMode: Boolean,
        uid: String,
        idToken: String? = null
    ): Result<Unit> = runCatching {
        val gifts = giftRepository.observeAllGifts().first()
        giftRepository.deleteGifts(isGuestMode, uid, gifts.map { it.id }).getOrThrow()

        if (!isGuestMode) {
            requireNotNull(idToken) { "Google ID token is required for account deletion." }
            authRepository.deleteAccount(idToken).getOrThrow()
        }

        val daysBeforeExpiry = notificationSettingsRepository.get().daysBeforeExpiry
        gifts.forEach { gift -> alarmRepository.cancel(gift.id, daysBeforeExpiry) }
        giftRepository.clearAllGifts().getOrThrow()
        brandRepository.clearCachedBrands().getOrThrow()
        if (!isGuestMode) authRepository.signOut()
        userSessionRepository.clear()
    }
}
