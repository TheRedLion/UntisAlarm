package eu.karenfort.untisAlarm.dataPass

import android.content.Context
import java.time.LocalDateTime

class DataPass {
    companion object {
        fun passAlarmActive(context: Context, localDateTime: LocalDateTime?) {
            (context as OnDataPassedListener).onAlarmPreviewPassed(localDateTime)
        }

        fun passNotificationsAllowed(context: Context, notificationsAllowed: Boolean) {
            (context as OnDataPassedListener).onNotificationsAllowedPassed(notificationsAllowed)
        }
    }
}