package com.carlkarenfort.test.alarm

interface AlarmScheduler {
    fun schedule(item: AlarmItem)
    fun cancal(item: AlarmItem)
}