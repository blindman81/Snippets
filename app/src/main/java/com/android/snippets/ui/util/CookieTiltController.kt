package com.android.snippets.ui.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.PowerManager
import android.view.Surface
import android.view.WindowManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import android.content.ContextWrapper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlin.math.abs


/**
 * Battery-optimized controller that supplies subtle tilt rotation for primary-colored cookie buttons.
 *
 * Optimizations for Android 7+ (API 24+):
 * 1. Single shared SensorEventListener across all active cookie buttons (reference-counted).
 * 2. Uses low-power TYPE_GRAVITY (fallback to TYPE_ACCELEROMETER) instead of power-hungry TYPE_GYROSCOPE.
 * 3. Delivers at SENSOR_DELAY_UI (~15 Hz) rather than high-rate game delays.
 * 4. Deadband filtering (0.6° epsilon): drops noisy micro-fluctuations when stationary so CPU/GPU sleep.
 * 5. Strict lifecycle awareness: automatically unregisters onPause/onStop to prevent background drain on Android 7-8.
 * 6. Disables entirely when PowerManager.isPowerSaveMode is active.
 */
object CookieTiltController : SensorEventListener, DefaultLifecycleObserver {
    private var sensorManager: SensorManager? = null
    private var sensor: Sensor? = null
    private var powerManager: PowerManager? = null
    private var appContext: Context? = null

    private val _rawTiltAngle = mutableFloatStateOf(0f)
    val rawTiltAngle: State<Float> = _rawTiltAngle

    private val _isGloballyEnabled = mutableStateOf(true)
    var isGloballyEnabled: Boolean
        get() = _isGloballyEnabled.value
        set(value) {
            if (_isGloballyEnabled.value != value) {
                _isGloballyEnabled.value = value
                updateListeningState()
            }
        }

    private var subscriberCount = 0
    private var isResumed = false
    private var isListening = false
    private var lastReportedAngle = 0f

    private const val MAX_TILT_DEGREES = 28f
    private const val TILT_SENSITIVITY_DIVISOR = 5.0f
    private const val DEADBAND_THRESHOLD = 1.2f

    fun initialize(context: Context) {
        if (appContext != null) return
        val app = context.applicationContext
        appContext = app
        sensorManager = app.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        powerManager = app.getSystemService(Context.POWER_SERVICE) as? PowerManager
        sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
            ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    private var activeTiltModalCount by mutableIntStateOf(0)
    val hasTiltModalFocus: Boolean
        get() = activeTiltModalCount > 0

    fun beginTiltModalFocus() {
        activeTiltModalCount++
    }

    fun endTiltModalFocus() {
        activeTiltModalCount = (activeTiltModalCount - 1).coerceAtLeast(0)
    }

    fun subscribe(lifecycleOwner: LifecycleOwner?) {
        subscriberCount++
        lifecycleOwner?.lifecycle?.addObserver(this)
        if (lifecycleOwner != null && lifecycleOwner.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) {
            isResumed = true
        }
        updateListeningState()
    }

    fun unsubscribe(lifecycleOwner: LifecycleOwner?) {
        subscriberCount = maxOf(0, subscriberCount - 1)
        lifecycleOwner?.lifecycle?.removeObserver(this)
        updateListeningState()
    }

    override fun onResume(owner: LifecycleOwner) {
        isResumed = true
        updateListeningState()
    }

    override fun onPause(owner: LifecycleOwner) {
        isResumed = false
        updateListeningState()
    }

    private fun updateListeningState() {
        val pm = powerManager
        val isPowerSave = pm?.isPowerSaveMode == true
        val shouldListen = isGloballyEnabled && subscriberCount > 0 && isResumed && !isPowerSave && sensor != null

        if (shouldListen && !isListening) {
            sensor?.let {
                val registered = sensorManager?.registerListener(
                    this,
                    it,
                    SensorManager.SENSOR_DELAY_UI
                ) ?: false
                isListening = registered
            }
        } else if (!shouldListen && isListening) {
            sensorManager?.unregisterListener(this)
            isListening = false
            _rawTiltAngle.floatValue = 0f
            lastReportedAngle = 0f
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (!isListening) return

        if (powerManager?.isPowerSaveMode == true) {
            updateListeningState()
            return
        }

        val displayRotation = getDisplayRotation()
        val horizontalTilt = getHorizontalTilt(event, displayRotation)
        val verticalTilt = event.values[1]

        // Gravity normalization: reaching full rotation at comfortable hand tilt (~30°)
        val tiltFraction = (horizontalTilt / TILT_SENSITIVITY_DIVISOR).coerceIn(-1f, 1f)

        // Reverse if held upside-down
        val orientationSign = if (verticalTilt < -2f) -1f else 1f
        val calculatedAngle = orientationSign * tiltFraction * MAX_TILT_DEGREES

        // Deadband filter: drop micro-jitter when phone is resting
        if (abs(calculatedAngle - lastReportedAngle) >= DEADBAND_THRESHOLD) {
            lastReportedAngle = calculatedAngle
            _rawTiltAngle.floatValue = calculatedAngle
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun getHorizontalTilt(event: SensorEvent, displayRotation: Int): Float {
        val x = event.values[0]
        val y = event.values[1]
        return when (displayRotation) {
            Surface.ROTATION_90 -> -y
            Surface.ROTATION_270 -> y
            Surface.ROTATION_180 -> -x
            else -> x
        }
    }

    private fun getDisplayRotation(): Int {
        val ctx = appContext ?: return Surface.ROTATION_0
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                ctx.display?.rotation ?: Surface.ROTATION_0
            } catch (_: Exception) {
                Surface.ROTATION_0
            }
        } else {
            @Suppress("DEPRECATION")
            (ctx.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)?.defaultDisplay?.rotation ?: Surface.ROTATION_0
        }
    }
}

/**
 * CompositionLocal indicating whether the current screen is active and not obscured by a modal/dialog.
 * When false, buttons must NOT listen to tilt sensors or spin in the background.
 */
val LocalTiltActive = compositionLocalOf { true }

/** True only inside a modal whose controls are allowed to keep tilt while it is open. */
val LocalTiltModalFocus = compositionLocalOf { false }

private tailrec fun Context.findLifecycleOwner(): LifecycleOwner? {
    return when (this) {
        is LifecycleOwner -> this
        is ContextWrapper -> baseContext.findLifecycleOwner()
        else -> null
    }
}

/**
 * Composable hook returning an animated subtle tilt angle (in degrees) for primary-colored buttons.
 *
 * Guarantees zero background/recents battery drain:
 * 1. Checks [enabled] and global kill-switch.
 * 2. Checks [LocalWindowInfo.isWindowFocused] (stops immediately in Android Recents / App Switcher).
 * 3. Checks [LocalTiltActive] (stops immediately when obscured by a dialog, sheet, or inactive backstack screen).
 *
 * When any condition is false, unregisters from SensorManager immediately and settles at 0°.
 */
@Composable
fun rememberCookieTilt(enabled: Boolean): State<Float> {
    val context = LocalContext.current
    val windowInfo = LocalWindowInfo.current
    val isTiltActive = LocalTiltActive.current
    val isModalTiltFocus = LocalTiltModalFocus.current
    val lifecycleOwner = remember(context) { context.findLifecycleOwner() }

    val effectiveEnabled = enabled &&
        CookieTiltController.isGloballyEnabled &&
        (!CookieTiltController.hasTiltModalFocus || isModalTiltFocus) &&
        windowInfo.isWindowFocused &&
        isTiltActive

    DisposableEffect(effectiveEnabled, lifecycleOwner) {
        if (effectiveEnabled) {
            CookieTiltController.initialize(context)
            CookieTiltController.subscribe(lifecycleOwner)
        }
        onDispose {
            if (effectiveEnabled) {
                CookieTiltController.unsubscribe(lifecycleOwner)
            }
        }
    }

    val targetTilt = if (effectiveEnabled) CookieTiltController.rawTiltAngle.value else 0f

    return animateFloatAsState(
        targetValue = targetTilt,
        animationSpec = spring(
            dampingRatio = 0.58f,
            stiffness = 320f
        ),
        label = "cookie_tilt_rotation"
    )
}
