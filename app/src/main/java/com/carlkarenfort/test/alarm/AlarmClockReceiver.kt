package com.carlkarenfort.test.alarm

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.carlkarenfort.test.MainActivity
import com.carlkarenfort.test.R
import com.carlkarenfort.test.vibration.FiredHourVibration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmClockReceiver: BroadcastReceiver() {
    private val TAG = "AlarmClockReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(TAG, "atleast you called alarmClockReceiver")
        if (context == null) {
            return
        }
        val vibration = FiredHourVibration(context)
        val mainActivityIntent = Intent(context, MainActivity::class.java)

        vibration.launch(
            longArrayOf(
                0L,
                125L,
                350L,
                125L
            ),
            -1
        )

        val notificationManagerCompat = NotificationManagerCompat.from(context)
        notificationManagerCompat.cancelAll()

        val channelData = "channelData"
        var channelId = "channelId"
        val channelName = "channelName"

        if (Build.VERSION.SDK_INT >= 26) {

            val ex = notificationManagerCompat.getNotificationChannel(channelId)

            ex?.let {
                notificationManagerCompat.deleteNotificationChannel(it.id)
                channelId += "${System.currentTimeMillis()}"
            }

            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.description = "Hour"

            notificationManagerCompat.createNotificationChannel(notificationChannel)
        }

        val title = "Title"

        CoroutineScope(Dispatchers.Default).launch {
            val content = "I dont know what this is "

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentTitle(title)
                .setContentText(content)
                .setDefaults(android.app.Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setTimeoutAfter(60 * 1000L)
                .setOnlyAlertOnce(false)
                .setOngoing(false)
                .setLocalOnly(true)
                .addAction(
                    R.drawable.empty,
                    context.getString(R.string.label_got_it),
                    PendingIntent.getActivity(
                        context,
                        1000,
                        mainActivityIntent,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )
                .setCustomHeadsUpContentView(
                    RemoteViews(
                        context.packageName,
                        R.layout.view_heads_up_drink_alarm
                    ).apply {
                        this.setTextViewText(
                            R.id.viewNotificationDrinkAlarmTvDataDescription,
                            content
                        )
                        this.setTextViewText(R.id.viewNotificationDrinkAlarmTvDataTitle, title)
                    }
                )

            val result = builder.build()
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return@launch
            }
            notificationManagerCompat.notify(111, result)
        }
    }
}