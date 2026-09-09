package com.sumi.pockon.domain.usecase

import com.sumi.pockon.domain.model.Gift
import com.sumi.pockon.domain.repository.AlarmRepository
import javax.inject.Inject

class ScheduleGiftAlarmUseCase @Inject constructor(private val alarmRepository: AlarmRepository) {
    operator fun invoke(gift: Gift, dDay: Int, time: Pair<Int, Int>) = alarmRepository.schedule(gift, dDay, time)
}
