/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 */
package eu.karenfort.main

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors
import kotlinx.coroutines.runBlocking

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        // check if api version below 31 and load dark mode from storedata to apply it if needed
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) {
            runBlocking {
                val darkMode = StoreData(this@App).loadDarkMode()
                if (darkMode == 0) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                } else if (darkMode == 1) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                } else if (darkMode == 2) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
            }
        }
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}