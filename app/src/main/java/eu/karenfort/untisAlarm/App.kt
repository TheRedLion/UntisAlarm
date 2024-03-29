/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 */
package eu.karenfort.untisAlarm

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.google.android.material.color.DynamicColors
import eu.karenfort.untisAlarm.extentions.changeDarkMode
import eu.karenfort.untisAlarm.helper.COROUTINE_EXCEPTION_HANDLER
import eu.karenfort.untisAlarm.helper.DarkMode
import eu.karenfort.untisAlarm.helper.SUPPORTED_LANGUAGES
import eu.karenfort.untisAlarm.helper.SUPPORTED_LANGUAGES_TAG
import eu.karenfort.untisAlarm.helper.StoreData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO + COROUTINE_EXCEPTION_HANDLER).launch {
            val storeData = StoreData(this@App)

            val darkMode = storeData.loadDarkMode() ?: DarkMode.DEFAULT
            this@App.changeDarkMode(darkMode)

            val language = storeData.loadLanguage()
            if (language != null) {
                val languageTag = SUPPORTED_LANGUAGES_TAG[SUPPORTED_LANGUAGES.indexOf(language)]
                val appLocale = LocaleListCompat.forLanguageTags(languageTag)
                AppCompatDelegate.setApplicationLocales(appLocale)
            }
        }
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}