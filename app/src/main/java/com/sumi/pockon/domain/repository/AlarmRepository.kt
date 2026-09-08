package com.sumi.pockon.domain.repository

import com.sumi.pockon.data.model.Gift

interface AlarmRepository {
    fun schedule(gift: Gift, dDay: Int, time: Pair<Int, Int>)
    fun cancel(id: String, dDay: Int)
}