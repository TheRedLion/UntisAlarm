package eu.karenfort.untisAlarm.dataPass

import android.content.Context
import java.time.LocalDateTime

class DataPass {
    companion object {
        fun passAlarmActive(context: Context, localDateTime: LocalDateTime?) {
            try {
                (context as OnDataPassedListener).onAlarmPreviewPassed(localDateTime)
            } catch (_: ClassCastException) {}
        }

        fun passNotificationsAllowed(context: Context, notificationsAllowed: Boolean) {
            try {
                (context as OnDataPassedListener).onNotificationsAllowedPassed(notificationsAllowed)
            } catch (_: ClassCastException) {}
        }
    }
}