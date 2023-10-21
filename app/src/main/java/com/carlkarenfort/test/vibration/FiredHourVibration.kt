package com.carlkarenfort.test.vibration

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build

@SuppressLint("NewApi")
class FiredHourVibration(
    private val vibrators: Map<String, Vibration>
) : Vibration {

    constructor(context: Context) : this(
        mapOf(
            "obsolete" to ObsoleteVibration(context),
            "common" to CommonVibration(context)
        )

    )

    override fun launch(pattern: LongArray, repeatCount: Int) {
        val key = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            "common"
        } else {
            "obsolete"
        }

        vibrators.getValue(key).launch(pattern, repeatCount)

    }

    override fun cancel() {
        val key = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            "common"
        } else {
            "obsolete"
        }

        vibrators.getValue(key).cancel()
    }
}