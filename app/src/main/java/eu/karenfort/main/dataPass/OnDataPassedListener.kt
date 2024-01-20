package eu.karenfort.main.dataPass

import java.time.LocalDateTime

interface OnDataPassedListener {
    /**
     * @param newAlarmClockTime is the time of the next alarm clock
     *
     * This method is called when the user has updated the alarm clock time,
     * to update MainActivity's alarm clock time preview.
     */
    fun onAlarmPreviewPassed(newAlarmClockTime: LocalDateTime)

    /**
     * @param areNotificationsAllowed is true if the user has allowed notifications
     *
     * This method can set weather MainActivity will prompt the user to
     * allow notifications.
     */
    fun onNotificationsAllowedPassed(areNotificationsAllowed: Boolean)
}