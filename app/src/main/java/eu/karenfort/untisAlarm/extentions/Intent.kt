/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 */
package eu.karenfort.untisAlarm.extentions

import android.content.Intent
import android.os.Parcelable
import eu.karenfort.untisAlarm.helper.isTiramisuPlus

inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
    isTiramisuPlus() -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}