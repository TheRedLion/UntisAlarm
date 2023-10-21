package com.carlkarenfort.test.vibration

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationEffect
import android.os.VibratorManager
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.S)
class CommonVibration(
    private val vibratorManager: VibratorManager,
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
        context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager,
        attributesBuilder
    )

    override fun launch(pattern: LongArray, repeatCount: Int) {
        vibratorManager.defaultVibrator.vibrate(
            VibrationEffect.createWaveform(pattern, repeatCount),
            attributesBuilder
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()
        )
    }


    override fun cancel() {
        vibratorManager.cancel()
    }
}