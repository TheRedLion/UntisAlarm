package eu.karenfort.main.activities

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
import android.provider.AlarmClock
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.carlkarenfort.test.R
import com.carlkarenfort.test.databinding.ActivityReminderBinding
import eu.karenfort.main.StoreData
import eu.karenfort.main.alarm.AlarmManager
import eu.karenfort.main.helper.ALARM_NOTIF_ID
import eu.karenfort.main.helper.ALARM_SOUND_DEFAULT_URI
import eu.karenfort.main.helper.INCREASE_VOLUME_DELAY
import eu.karenfort.main.helper.MAX_ALARM_DURATION
import eu.karenfort.main.helper.MIN_ALARM_VOLUME_FOR_INCREASING_ALARMS
import eu.karenfort.main.helper.getFormattedTime
import eu.karenfort.main.helper.getPassedSeconds
import eu.karenfort.main.helper.isOreoMr1Plus
import eu.karenfort.main.helper.isOreoPlus
import eu.karenfort.main.helper.notificationManager
import eu.karenfort.main.helper.viewBinding
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime

class ReminderActivity : AppCompatActivity() {
    private val TAG = "ReminderActivity"
    private val increaseVolumeHandler = Handler(Looper.getMainLooper())
    private val maxReminderDurationHandler = Handler(Looper.getMainLooper())
    private val swipeGuideFadeHandler = Handler(Looper.getMainLooper())
    private val vibrationHandler = Handler(Looper.getMainLooper())
    private var isAlarmReminder = false
    private var didVibrate = false
    private var wasAlarmSnoozed = false
    //private var alarm: Alarm? = null
    private var audioManager: AudioManager? = null
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var initialAlarmVolume: Int? = null
    private var dragDownX = 0f
    private val binding: ActivityReminderBinding by viewBinding(ActivityReminderBinding::inflate)
    private var finished = false

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("ReminderActivity", "creating ReminderActivity")
        //isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        showOverLockscreen()
        //updateTextColors(binding.root)
        //updateStatusbarColor(getProperBackgroundColor())

        binding.reminderTitle.text = getString(R.string.app_name)
        binding.reminderText.text = if (isAlarmReminder) getFormattedTime(getPassedSeconds(), false, false) else getString(
            R.string.time_expired
        )

        val maxDuration = MAX_ALARM_DURATION
        maxReminderDurationHandler.postDelayed({
            finishActivity()
        }, maxDuration * 1000L)

        setupAlarmButtons()
        setupEffects()
    }
    private fun View.beGone() {
        visibility = View.GONE
    }
    private fun View.onGlobalLayout(callback: () -> Unit) {
        viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (viewTreeObserver != null) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    callback()
                }
            }
        })
    }
    private fun View.performHapticFeedback() = performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)

    @SuppressLint("ClickableViewAccessibility")
    private fun setupAlarmButtons() {
        binding.reminderStop.beGone()
        binding.reminderDraggableBackground.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pulsing_animation))
        //binding.reminderDraggableBackground.applyColorFilter(getProperPrimaryColor())

        //val textColor = getProperTextColor()
        //binding.reminderDismiss.applyColorFilter(textColor)
        //binding.reminderDraggable.applyColorFilter(textColor)
        //binding.reminderSnooze.applyColorFilter(textColor)

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
                    binding.reminderDraggable.x = Math.min(maxDragX, Math.max(minDragX, event.rawX - dragDownX))
                    if (binding.reminderDraggable.x >= maxDragX - 50f) {
                        if (!didVibrate) {
                            binding.reminderDraggable.performHapticFeedback()
                            didVibrate = true
                            finishActivity()
                            AlarmManager.main(this)
                        }

                        if (isOreoPlus()) {
                            notificationManager.cancelAll()
                        }
                    } else if (binding.reminderDraggable.x <= minDragX + 50f) {
                        if (!didVibrate) {
                            binding.reminderDraggable.performHapticFeedback()
                            didVibrate = true
                            snoozeAlarm()
                        }

                        if (isOreoPlus()) {
                            notificationManager.cancelAll()
                        }
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
            doVibrate = StoreData(applicationContext).loadVibrate()?: return@runBlocking
            val (_, newSoundUri) = StoreData(applicationContext).loadSound()
            if (newSoundUri == null) {
                return@runBlocking
            }
            soundUri = newSoundUri
        }

        if (doVibrate && isOreoPlus()) {
            val pattern = LongArray(2) { 500 }
            vibrationHandler.postDelayed({
                vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            }, 500)
        }

        try {
            Log.i("ReminderActivity", "Playing sound with Sound Uri: $soundUri")
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
                increaseVolumeGradually = StoreData(applicationContext).loadIncreaseVolumeGradually() ?: return@runBlocking
            }

            if (increaseVolumeGradually) {
                scheduleVolumeIncrease(MIN_ALARM_VOLUME_FOR_INCREASING_ALARMS.toFloat(), initialAlarmVolume!!.toFloat(), 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play Alarm Clock Sounds!")
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
            snoozeAlarm()
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
            notificationManager.cancel(ALARM_NOTIF_ID)
        } else {
            destroyEffects()
        }
    }

    private fun destroyEffects() {

        var increaseVolumeGradually = false
        runBlocking {
            increaseVolumeGradually = StoreData(applicationContext).loadIncreaseVolumeGradually() ?: return@runBlocking
        }

        if (increaseVolumeGradually) {
            resetVolumeToInitialValue()
        }

        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        vibrator?.cancel()
        vibrator = null
    }

    private fun snoozeAlarm() {
        destroyEffects()
        var snoozeTime = 5
        runBlocking {
            snoozeTime = StoreData(applicationContext).loadSnoozeTime() ?: return@runBlocking
        }
        eu.karenfort.main.alarmClock.AlarmClock.snoozeAlarm(snoozeTime, this)
        wasAlarmSnoozed = true
        finishActivity()
    }

    private fun finishActivity() {
        finished = true
        destroyEffects()
        finish()
        AlarmManager.main(this)
        overridePendingTransition(0, 0)
    }

    private fun showOverLockscreen() {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        if (isOreoMr1Plus()) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
    }
}
