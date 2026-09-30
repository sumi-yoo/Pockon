package com.sumi.pockon.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.sumi.pockon.ui.main.MainActivity
import com.sumi.pockon.MainApplication.Companion.CHANNEL_ID
import com.sumi.pockon.MainApplication.Companion.GROUP_KEY
import com.sumi.pockon.R
import com.sumi.pockon.domain.usecase.RefreshGiftAlarmsUseCase
import com.sumi.pockon.domain.repository.GiftRepository
import com.sumi.pockon.util.getDdayInt
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var giftRepository: GiftRepository
    @Inject
    lateinit var refreshGiftAlarmsUseCase: RefreshGiftAlarmsUseCase

    override fun onReceive(context: Context, intent: Intent) {
        // 재부팅 후 알람 매니저 재등록
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            CoroutineScope(Dispatchers.IO).launch {
                refreshGiftAlarmsUseCase()
            }
        } else { // 등록된 알람 수신
            val giftId = intent.getStringExtra("gift") ?: return
            val dDay = intent.getIntExtra("dDay", 0)

            CoroutineScope(Dispatchers.IO).launch {
                giftRepository.observeGift(giftId).take(1).collectLatest { gift ->
                    if (gift.id.isEmpty()) return@collectLatest

                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                    val myIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    val pendingIntent: PendingIntent =
                        PendingIntent.getActivity(
                            context,
                            0,
                            myIntent,
                            PendingIntent.FLAG_IMMUTABLE
                        )

                    val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_noti_gift)
                        .setContentTitle("${gift.brand}\n${gift.name}")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setGroup(GROUP_KEY) // 그룹 키 지정
                    if (dDay == 0) {
                        notificationBuilder.setContentText(context.getString(R.string.msg_noti_end_dt_today))
                    } else {
                        notificationBuilder.setContentText(
                            context.getString(
                                R.string.msg_noti_end_dt,
                                dDay
                            )
                        )
                    }

                    notificationManager.notify(
                        "${gift.id}${getDdayInt(gift.endDt)}".hashCode(),
                        notificationBuilder.build()
                    )

                    // 그룹 요약 알림
                    val inboxStyle = NotificationCompat.InboxStyle()
                    repeat(giftRepository.getGiftCountByEndDate(gift.endDt).getOrDefault(0)) {
                        inboxStyle.addLine("${gift.brand}\n${gift.name}")
                    }
                    val summaryNotification = NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_noti_gift)
                        .setStyle(inboxStyle)
                        .setGroup(GROUP_KEY)
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .setGroupSummary(true)
                        .setAutoCancel(true)
                        .build()

                    notificationManager.notify(0, summaryNotification)
                }
            }
        }
    }
}
