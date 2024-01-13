package eu.karenfort.main.helper

import android.content.Context
import java.time.LocalDateTime

class DataPass {
    companion object {
        fun passLocalDateTime(context: Context, localDateTime: LocalDateTime) {
            (context as OnDataPass).onAlarmPreviewPass(localDateTime)
        }

        fun passNotificationsAllowed(context: Context, notificationsAllowed: Boolean) {
            (context as OnDataPass).onNotificationsAllowedPass(notificationsAllowed)
        }
    }
}