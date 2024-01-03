package eu.karenfort.main.helper

import android.os.Build
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


fun isOnMainThread() = Looper.myLooper() == Looper.getMainLooper()

fun ensureBackgroundCoroutine(callback: () -> Unit) {
    if (isOnMainThread()) {
        CoroutineScope(Dispatchers.Default).launch {
            callback()
        }
    } else {
        callback()
    }
}

fun isUpsideDownCakePlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
fun isTiramisuPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
fun isOreoMr1Plus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1
fun isSnowConePlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S


