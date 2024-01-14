package eu.karenfort.main.dataPass

import java.time.LocalDateTime

interface OnDataPassedListener {
    fun onAlarmPreviewPassed(localDateTime: LocalDateTime)
    fun onNotificationsAllowedPassed(areNotificationsAllowed: Boolean)
}