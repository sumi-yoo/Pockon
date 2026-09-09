package com.sumi.pockon.data.local.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.sumi.pockon.receiver.AlarmReceiver
import com.sumi.pockon.domain.model.Gift
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject

class AlarmDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(gift: Gift, dDay: Int, time: Pair<Int, Int>) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("gift", gift.id)
            putExtra("dDay", dDay)
        }
        val year = gift.endDt.substring(0, 4)
        val month = gift.endDt.substring(4, 6)
        val day = gift.endDt.substring(6, 8)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year.toInt())
            set(Calendar.MONTH, month.toInt() - 1) // 0이 1월
            set(Calendar.DATE, day.toInt())
            set(Calendar.HOUR_OF_DAY, time.first)
            set(Calendar.MINUTE, time.second)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            add(Calendar.DATE, -dDay)
        }

        if (calendar.timeInMillis <= Calendar.getInstance().timeInMillis) return

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            PendingIntent.getBroadcast(
                context,
                gift.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    fun cancel(id: String, dDay: Int) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("gift", id)
            putExtra("dDay", dDay)
        }

        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }
}
