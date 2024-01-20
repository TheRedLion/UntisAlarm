/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Code originally from https://github.com/SimpleMobileTools/Simple-Clock
 * but modified.
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 *
 * Description: This activity is shown when an alarm goes off.
 */
package eu.karenfort.main.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.AlarmClock
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.carlkarenfort.test.R
import com.carlkarenfort.test.databinding.ActivityReminderBinding
import eu.karenfort.main.alarmClock.AlarmClockSetter
import eu.karenfort.main.extentions.getAlarmPreviewString
import eu.karenfort.main.extentions.notificationManager
import eu.karenfort.main.extentions.onGlobalLayout
import eu.karenfort.main.extentions.performHapticFeedback
import eu.karenfort.main.extentions.viewBinding
import eu.karenfort.main.helper.ALARM_CLOCK_NOTIFICATION_ID
import eu.karenfort.main.helper.ALARM_SOUND_DEFAULT_URI
import eu.karenfort.main.helper.INCREASE_VOLUME_DELAY
import eu.karenfort.main.helper.MAX_ALARM_DURATION
import eu.karenfort.main.helper.MIN_ALARM_VOLUME_FOR_INCREASING_ALARMS
import eu.karenfort.main.helper.StoreData
import eu.karenfort.main.helper.isOreoMr1Plus
import eu.karenfort.main.helper.isSnowConePlus
import eu.karenfort.main.helper.isUpsideDownCakePlus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime


class ReminderActivity : AppCompatActivity() {
    private val increaseVolumeHandler = Handler(Looper.getMainLooper())
    private val maxReminderDurationHandler = Handler(Looper.getMainLooper())
    private val swipeGuideFadeHandler = Handler(Looper.getMainLooper())
    private val vibrationHandler = Handler(Looper.getMainLooper())
    private var didVibrate = false
    private var wasAlarmSnoozed = false
    private var audioManager: AudioManager? = null
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var initialAlarmVolume: Int? = null
    private var dragDownX = 0f
    private val binding: ActivityReminderBinding by viewBinding(ActivityReminderBinding::inflate)
    private var finished = false

    companion object {
        const val TAG = "ReminderActivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        showOverLockscreen()

        binding.reminderTitle.text = getString(R.string.app_name)
        binding.reminderText.text = getAlarmPreviewString(LocalDateTime.now())

        maxReminderDurationHandler.postDelayed({
            finishActivity()
        }, MAX_ALARM_DURATION * 1000L)

        setupAlarmButtons()
        setupEffects()
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun setupAlarmButtons() {
        binding.reminderStop.visibility = View.GONE
        binding.reminderDraggableBackground.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pulsing_animation))

        var minDragX = 0f
        var maxDragX = 0f
        var initialDraggableX = 0f

        binding.reminderDismiss.onGlobalLayout {
            minDragX = binding.reminderSnooze.left.toFloat()
            maxDragX = binding.reminderDismiss.left.toFloat()
            initialDraggableX = binding.reminderDraggable.left.toFloat()
        }

        binding.reminderDraggable.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dragDownX = event.x
                    binding.reminderDraggableBackground.animate().alpha(0f)
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    dragDownX = 0f
                    if (!didVibrate) {
                        binding.reminderDraggable.animate().x(initialDraggableX).withEndAction {
                            binding.reminderDraggableBackground.animate().alpha(0.2f)
                        }

                        binding.reminderGuide.animate().alpha(1f).start()
                        swipeGuideFadeHandler.removeCallbacksAndMessages(null)
                        swipeGuideFadeHandler.postDelayed({
                            binding.reminderGuide.animate().alpha(0f).start()
                        }, 2000L)
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    binding.reminderDraggable.x =
                        maxDragX.coerceAtMost(minDragX.coerceAtLeast(event.rawX - dragDownX))
                    if (binding.reminderDraggable.x >= maxDragX - 50f) {
                        if (!didVibrate) {
                            binding.reminderDraggable.performHapticFeedback()
                            didVibrate = true
                            finishActivity()
                            CoroutineScope(Dispatchers.Default).launch{
                                AlarmClockSetter.main(this@ReminderActivity)
                            }
                        }

                        notificationManager.cancelAll()
                    } else if (binding.reminderDraggable.x <= minDragX + 50f) {
                        if (!didVibrate) {
                            binding.reminderDraggable.performHapticFeedback()
                            didVibrate = true
                            snoozeAlarmClock()
                        }

                        notificationManager.cancelAll()
                    }
                }
            }
            true
        }
    }
    private fun setupEffects() {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        initialAlarmVolume = audioManager?.getStreamVolume(AudioManager.STREAM_ALARM) ?: 7

        var doVibrate = true
        var soundUri: Uri = ALARM_SOUND_DEFAULT_URI
        runBlocking {
            doVibrate = StoreData(this@ReminderActivity).loadVibrate()?: return@runBlocking
            val newSoundUri = StoreData(this@ReminderActivity).loadSound() ?: return@runBlocking
            soundUri = newSoundUri
        }

        if (doVibrate) {
            val pattern = LongArray(2) { 500 }
            vibrationHandler.postDelayed({
                if (isSnowConePlus()) {
                    val vibratorManager =
                        getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    vibrator = @Suppress("DEPRECATION") getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            }, 500)
        }

        try {
            mediaPlayer = MediaPlayer()
            mediaPlayer!!.setAudioAttributes(AudioAttributes.Builder()
                .setLegacyStreamType(AudioManager.STREAM_ALARM)
                .build())

            mediaPlayer = mediaPlayer!!.apply {
                setDataSource(this@ReminderActivity, soundUri)
                isLooping = true
                prepare()
                start()
            }
            var increaseVolumeGradually = false
            runBlocking {
                increaseVolumeGradually = StoreData(this@ReminderActivity).loadIncreaseVolumeGradually() ?: return@runBlocking
            }

            if (increaseVolumeGradually) {
                scheduleVolumeIncrease(MIN_ALARM_VOLUME_FOR_INCREASING_ALARMS.toFloat(), initialAlarmVolume!!.toFloat(), 0)
            }
        } catch (e: Exception) {
            Log.i(TAG, "Failed to play Alarm Clock Sounds!")
        }
    }
    private fun scheduleVolumeIncrease(lastVolume: Float, maxVolume: Float, delay: Long) {
        increaseVolumeHandler.postDelayed({
            val newLastVolume = (lastVolume + 0.1f).coerceAtMost(maxVolume)
            audioManager?.setStreamVolume(AudioManager.STREAM_ALARM, newLastVolume.toInt(), 0)
            scheduleVolumeIncrease(newLastVolume, maxVolume, INCREASE_VOLUME_DELAY)
        }, delay)
    }
    private fun resetVolumeToInitialValue() {
        initialAlarmVolume?.apply {
            audioManager?.setStreamVolume(AudioManager.STREAM_ALARM, this, 0)
        }
    }
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action == AlarmClock.ACTION_SNOOZE_ALARM) {
            snoozeAlarmClock()
        } else {
            finishActivity()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        increaseVolumeHandler.removeCallbacksAndMessages(null)
        maxReminderDurationHandler.removeCallbacksAndMessages(null)
        swipeGuideFadeHandler.removeCallbacksAndMessages(null)
        vibrationHandler.removeCallbacksAndMessages(null)
        if (!finished) {
            finishActivity()
            notificationManager.cancel(ALARM_CLOCK_NOTIFICATION_ID)
        } else {
            destroyEffects()
        }
    }

    private fun destroyEffects() {
        resetVolumeToInitialValue()

        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        vibrator?.cancel()
        vibrator = null
    }
    private fun snoozeAlarmClock() {
        var snoozeTime = 5
        runBlocking {
            snoozeTime = StoreData(this@ReminderActivity).loadSnoozeTime() ?: return@runBlocking
        }
        eu.karenfort.main.alarmClock.AlarmClock.snooze(snoozeTime, this)
        wasAlarmSnoozed = true
        finishActivity(true)
    }

    private fun finishActivity() {
        finishActivity(false)
    }
    private fun finishActivity(snoozed: Boolean) {
        finished = true
        destroyEffects()
        finish()
        CoroutineScope(Dispatchers.Default).launch{
            if (snoozed) {
                AlarmClockSetter.main(this@ReminderActivity, null, null)
            } else {
                AlarmClockSetter.main(this@ReminderActivity, null, false)
            }
        }
        if (isUpsideDownCakePlus()) {
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE,0,0)
        } else {
            @Suppress("DEPRECATION") overridePendingTransition(0, 0)
        }
    }
    private fun showOverLockscreen() {
        if (isOreoMr1Plus()) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            return
        }
        window.addFlags(
            @Suppress("DEPRECATION") WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    @Suppress("DEPRECATION") WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        ) //to support older api versions
    }
}
