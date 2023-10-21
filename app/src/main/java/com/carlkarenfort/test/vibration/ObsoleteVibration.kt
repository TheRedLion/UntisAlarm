package com.carlkarenfort.test.vibration

import android.content.Context
import android.media.AudioAttributes
import android.os.Vibrator

class ObsoleteVibration(
    private val vibrator: Vibrator,
    private val attributesBuilder: AudioAttributes.Builder
) : Vibration {

    constructor(
        context: Context
    ) : this(
        context,
        AudioAttributes.Builder()
    )

    constructor(
        context: Context,
        attributesBuilder: AudioAttributes.Builder
    ) : this(
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator,
        attributesBuilder
    )

    override fun launch(pattern: LongArray, repeatCount: Int) {
        vibrator.vibrate(
            pattern,
            repeatCount,
            attributesBuilder
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
    }

    override fun cancel() {
        vibrator.cancel()
    }
}