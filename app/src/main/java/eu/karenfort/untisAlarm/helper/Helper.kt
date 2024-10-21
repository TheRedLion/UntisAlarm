/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 */
package eu.karenfort.untisAlarm.helper

import android.os.Build
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun isOnMainThread(): Boolean = Looper.myLooper() == Looper.getMainLooper()

fun ensureBackgroundCoroutine(callback: () -> Unit) {
    if (isOnMainThread()) {
        CoroutineScope(Dispatchers.Default).launch {
            callback()
        }
    } else {
        callback()
    }
}

fun isOreoMr1Plus(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1
fun isSnowConePlus(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
fun isTiramisuPlus(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
fun isUpsideDownCakePlus(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
