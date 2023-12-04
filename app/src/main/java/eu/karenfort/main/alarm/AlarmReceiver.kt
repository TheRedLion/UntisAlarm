package eu.karenfort.main.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log


class AlarmReceiver: BroadcastReceiver() {
    private val TAG = "AlarmReceiver"
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "called onReceive with intent: $intent")

        val actualAction = intent.action
        if (actualAction != null && actualAction != Intent.ACTION_BOOT_COMPLETED) {
            if (actualAction != Intent.ACTION_CALL) {
                Log.i(TAG, "no allowed intent")
                return
            }
        }
        AlarmManager.main(context)
    }
}