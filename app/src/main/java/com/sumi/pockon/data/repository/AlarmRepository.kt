package com.sumi.pockon.data.repository

import com.sumi.pockon.data.local.alarm.AlarmDataSource
import com.sumi.pockon.data.model.Gift
import com.sumi.pockon.domain.repository.AlarmRepository
import javax.inject.Inject

class AlarmRepositoryImpl @Inject constructor(
    private val alarmDataSource: AlarmDataSource
) : AlarmRepository {

    override fun schedule(gift: Gift, dDay: Int, time: Pair<Int, Int>) {
        alarmDataSource.schedule(gift, dDay, time)
    }

    override fun cancel(id: String, dDay: Int) {
        alarmDataSource.cancel(id, dDay)
    }
}
