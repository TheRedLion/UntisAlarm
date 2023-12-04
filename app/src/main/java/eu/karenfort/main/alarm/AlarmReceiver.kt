package eu.karenfort.main.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log


class AlarmReceiver: BroadcastReceiver() {
    private val TAG = "AlarmReceiver"
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "called onReceive with intent: $intent")

        /* //todo add intent checks
            if (!intent.equals("Intent { flg=0x10 cmp=com.carlkarenfort.test/eu.karenfort.main.alarm.AlarmReceiver }") && !intent.equals(Intent.ACTION_BOOT_COMPLETED)) {
                Log.i(TAG, "intent $intent is not allowed")
                return
            }*/

        AlarmManager.main(context)
    }
}