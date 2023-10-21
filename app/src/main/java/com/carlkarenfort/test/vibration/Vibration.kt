package com.carlkarenfort.test.vibration


interface Vibration {
    fun launch(pattern: LongArray, repeatCount: Int)
    fun cancel()
}