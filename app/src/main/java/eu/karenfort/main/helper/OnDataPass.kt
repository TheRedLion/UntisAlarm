package eu.karenfort.main.helper

import java.time.LocalDateTime

interface OnDataPass {
    fun onAlarmPreviewPass(localDateTime: LocalDateTime)
    fun onNotificationsAllowedPass(areNotificationsAllowed: Boolean)
}