package com.sumi.pockon.domain.usecase

import com.sumi.pockon.domain.repository.AlarmRepository
import javax.inject.Inject

class CancelGiftAlarmUseCase @Inject constructor(private val alarmRepository: AlarmRepository) {
    operator fun invoke(id: String, dDay: Int) = alarmRepository.cancel(id, dDay)
}
