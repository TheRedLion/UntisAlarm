package eu.karenfort.main.helper

import android.os.Build
import android.os.Looper
import androidx.annotation.ChecksSdkIntAtLeast

const val ALARM_ID = "alarm_id"
const val ALARM_NOTIFICATION_CHANNEL_ID = "Alarm_Channel"
const val ALARM_NOTIF_ID = 9998
const val EARLY_ALARM_NOTIF_ID = 10003


fun isOnMainThread() = Looper.myLooper() == Looper.getMainLooper()

fun ensureBackgroundThread(callback: () -> Unit) {
    if (isOnMainThread()) {
        Thread {
            callback()
        }.start()
    } else {
        callback()
    }
}
@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
fun isOreoPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O