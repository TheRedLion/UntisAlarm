/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 *
 * Description: This class checks if the Alarm Clock is set properly and adjusts accordingly.
 */
package eu.karenfort.untisAlarm.alarmClock

import android.content.Context
import android.util.Log
import eu.karenfort.untisAlarm.alarm.AlarmScheduler
import eu.karenfort.untisAlarm.api.UntisApiCalls
import eu.karenfort.untisAlarm.dataPass.DataPass
import eu.karenfort.untisAlarm.extentions.isScreenOn
import eu.karenfort.untisAlarm.extentions.sendLoggedOutNotif
import eu.karenfort.untisAlarm.helper.DEFAULT_TBS_MIN
import eu.karenfort.untisAlarm.helper.StoreData
import java.io.IOException
import java.time.LocalDateTime

class AlarmClockSetter {
    companion object {
        private const val TAG = "AlarmClockSetter"
        /* isActive and isEdited is used to override stored data, necessary because storing is
            done on a different coroutine and thus takes time. Used when a new state is set and the
            AlarmClock needs to be adjusted right after that for example to update UI
         */
        //todo move into AlarmClock

        suspend fun main(context: Context): LocalDateTime? {
            return main(context, null, null)
        }

        suspend fun main(context: Context, isActive: Boolean?): LocalDateTime? {
            return main(context, isActive, null)
        }

        suspend fun main(context: Context, isActive: Boolean?, isEdited: Boolean?): LocalDateTime? {
            val result = main2(context, isActive, isEdited)
            DataPass.passAlarmActive(context, result)
            return result
        }

        //the second function is to make return values easier to pass along with dataPass
        private suspend fun main2(
            context: Context,
            isActive: Boolean?,
            isEdited: Boolean?
        ): LocalDateTime? {
            Log.i(TAG, "Called alarmClockSetter")

            //load data
            val storeData = StoreData(context)

            val id = storeData.loadID()
            val loginData = storeData.loadLoginData()
            val tbs: Int = storeData.loadTBS()
                ?: DEFAULT_TBS_MIN //just setting default value in case stored value is lost
            val alarmActive: Boolean = isActive ?: (storeData.loadAlarmActive() ?: false)
            var (storedAlarmClockDateTime, storedAlarmClockEdited) = storeData.loadAlarmClock()
            if (isEdited != null) {
                storedAlarmClockEdited = isEdited
            }

            if (id == null || loginData[0] == null || loginData[1] == null || loginData[2] == null || loginData[3] == null) {
                if (!context.isScreenOn) {
                    context.sendLoggedOutNotif()
                }
                AlarmScheduler(context).cancel()
                Log.i(TAG, "cancelled because user was logged out")
                return null
            }
            var schoolStart: LocalDateTime? = null

            val untisApiCalls = try {
                UntisApiCalls(
                    loginData[0]!!,
                    loginData[1]!!,
                    loginData[2]!!,
                    loginData[3]!!
                )
            } catch (e: IOException) {
                if (e.message == "An unexpected exception occurred: {\"jsonrpc\":\"2.0\",\"id\":\"ID\",\"error\":{\"message\":\"bad credentials\",\"code\":-8504}}") {
                    if (!context.isScreenOn) {
                        context.sendLoggedOutNotif()
                    }
                    AlarmScheduler(context).cancel()
                    Log.i(TAG, "cancelled because user was logged out")
                    return null
                } else null
            }

            try {
                schoolStart = untisApiCalls?.getSchoolStartForDay(id, context)
            } catch (_: IOException) {
            }


            if (!alarmActive) {
                //no further action needed since alarm are disabled
                return schoolStart?.minusMinutes(tbs.toLong())
            }

            if (storedAlarmClockEdited) {
                return storedAlarmClockDateTime
            }

            if (schoolStart == null) {
                Log.i(TAG, "no school start was found")
                return null
            }

            //!! since it was just checked
            val newAlarmClockTime = schoolStart.minusMinutes(tbs.toLong())

            if (storedAlarmClockDateTime == null) {
                AlarmClock.set(newAlarmClockTime, context)
                Log.i(TAG, "cancelled because couldn't load stored AlarmClockDateTime")
                return newAlarmClockTime
            }

            if (isAlarmClockSetProperly(newAlarmClockTime, storedAlarmClockDateTime)) {
                Log.i(TAG, "alarm clock is set properly")
                return newAlarmClockTime
            }

            AlarmClock.set(newAlarmClockTime, context)


            storeData.storeAlarmClock(newAlarmClockTime)
            Log.i(TAG, "updating alarm clock")
            return newAlarmClockTime //todo should also be shown after closing reminderActivity
        }

        private fun isAlarmClockSetProperly(
            alarmClockTime: LocalDateTime?,
            storedAlarmClockDateTime: LocalDateTime?
        ) =
            if (alarmClockTime == null || storedAlarmClockDateTime == null)
                false
            else alarmClockTime.isEqual(
                storedAlarmClockDateTime
            )

    }
}