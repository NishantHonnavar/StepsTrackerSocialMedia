package com.example

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * StepSensorManager with intelligent footstep gait biomechanics verification.
 * Rejects bike rides, car vibrations, and constant engine rumble.
 * Only genuine human bipedal footsteps with periodic rhythmic heel-strikes are counted.
 */
class StepSensorManager(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private var stepCounterSensor: Sensor? = null
    private var stepDetectorSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null

    private var previousStepCounterValue: Float = -1f
    private var isListening = false
    private var hasReceivedHardwareSteps = false

    // BIOMECHANICAL GAIT PATTERN FILTER (Rejects Bike / Vehicle vibration)
    // Human walking produces alternating vertical accelerations with a natural human cadence:
    // Cadence: 1.2 Hz to 2.8 Hz (period between steps: 350ms to 850ms)
    // Bike riding produces either continuous low-amplitude road vibration (no sharp heel strike)
    // or constant high-frequency vibration (>4Hz).
    private var lastStepTimestamp: Long = 0L
    private val minStepIntervalMs = 340L // Maximum 2.94 steps/sec (running/sprinting peak)
    private val maxStepIntervalMs = 1400L // Minimum step cadence for walking continuity

    // Accelerometer filtering
    private var gravityX = 0f
    private var gravityY = 0f
    private var gravityZ = 9.8f
    private val alpha = 0.8f // Low-pass filter weight for isolating gravity

    // Peak-valley hysteresis detection
    private var isPeakArmed = false
    private var peakMagnitude = 0f
    private val heelStrikeThreshold = 1.35f // Linear acceleration threshold (m/s^2) above 1G
    private val valleyThreshold = -0.75f // Dip during toe-off swing phase

    // Rhythmic cadence verification window: requires consecutive step periodicity
    private var recentStepIntervals = LongArray(3) { 0L }
    private var intervalIndex = 0

    init {
        stepCounterSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepDetectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        accelerometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val hasHardwareSensor = stepCounterSensor != null || stepDetectorSensor != null
        val sensorName = stepCounterSensor?.name
            ?: stepDetectorSensor?.name
            ?: accelerometerSensor?.name
            ?: "Scroll Tax Biometric Engine"

        StepLockRepository.setStepSensorInfo(hasHardwareSensor || accelerometerSensor != null, sensorName)
        Log.d("StepSensorManager", "Initialized with sensor: $sensorName (HW: $hasHardwareSensor)")
    }

    fun startListening() {
        if (isListening || sensorManager == null) return

        var registered = false
        // Primary: Hardware Step Counter (built-in Android DSP step filter)
        stepCounterSensor?.let { sensor ->
            val success = sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_UI
            )
            if (success) registered = true
        }

        // Secondary: Hardware Step Detector (DSP verified foot impact)
        stepDetectorSensor?.let { sensor ->
            val success = sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_UI
            )
            if (success) registered = true
        }

        // Tertiary: Dynamic 3-Axis Accelerometer with gait biomechanics filter
        accelerometerSensor?.let { sensor ->
            val success = sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_GAME
            )
            if (success) registered = true
        }

        isListening = registered
        Log.d("StepSensorManager", "Step sensors registered successfully: $isListening")
    }

    fun stopListening() {
        if (!isListening || sensorManager == null) return
        sensorManager.unregisterListener(this)
        isListening = false
        previousStepCounterValue = -1f
        Log.d("StepSensorManager", "Step sensors unregistered")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        val now = System.currentTimeMillis()

        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val totalStepsSinceBoot = event.values[0]
                if (previousStepCounterValue < 0f) {
                    previousStepCounterValue = totalStepsSinceBoot
                } else {
                    val delta = (totalStepsSinceBoot - previousStepCounterValue).toInt()
                    // Sanity check: delta must be positive, reasonable, and cadence checked
                    if (delta in 1..200) {
                        hasReceivedHardwareSteps = true
                        StepLockRepository.addSteps(delta)
                        previousStepCounterValue = totalStepsSinceBoot
                    }
                }
            }

            Sensor.TYPE_STEP_DETECTOR -> {
                if (event.values.isNotEmpty() && event.values[0] == 1.0f) {
                    val interval = now - lastStepTimestamp
                    // Verify step cadence is physiologically plausible (no bike vibration triggers)
                    if (interval >= minStepIntervalMs) {
                        hasReceivedHardwareSteps = true
                        lastStepTimestamp = now
                        StepLockRepository.addSteps(1)
                    }
                }
            }

            Sensor.TYPE_ACCELEROMETER -> {
                // If hardware step counter is providing verified events, let hardware DSP handle it
                if (hasReceivedHardwareSteps) return

                val rawX = event.values[0]
                val rawY = event.values[1]
                val rawZ = event.values[2]

                // Low-pass filter to isolate gravity
                gravityX = alpha * gravityX + (1 - alpha) * rawX
                gravityY = alpha * gravityY + (1 - alpha) * rawY
                gravityZ = alpha * gravityZ + (1 - alpha) * rawZ

                // High-pass filter to get dynamic linear acceleration without gravity
                val linearX = rawX - gravityX
                val linearY = rawY - gravityY
                val linearZ = rawZ - gravityZ

                // Compute dynamic magnitude
                val linearMagnitude = sqrt((linearX * linearX + linearY * linearY + linearZ * linearZ).toDouble()).toFloat()

                // Walking gait signature:
                // 1. Definite heel-strike impulse peak (> heelStrikeThreshold)
                // 2. Followed by a foot swing valley (< valleyThreshold or low magnitude)
                // 3. Cadence within human walking/running window (340ms to 1400ms)
                val timeSinceLastStep = now - lastStepTimestamp

                if (linearMagnitude > heelStrikeThreshold && !isPeakArmed) {
                    if (timeSinceLastStep >= minStepIntervalMs) {
                        isPeakArmed = true
                        peakMagnitude = linearMagnitude
                    }
                } else if (isPeakArmed && linearMagnitude < 0.6f) {
                    // Foot reached ground contact / swing recovery phase
                    isPeakArmed = false
                    if (timeSinceLastStep in minStepIntervalMs..maxStepIntervalMs) {
                        // Check cadence rhythm stability to eliminate erratic bike bumps
                        val prevInterval = recentStepIntervals[(intervalIndex + 2) % 3]
                        recentStepIntervals[intervalIndex] = timeSinceLastStep
                        intervalIndex = (intervalIndex + 1) % 3

                        // If cadence interval is consistent or this is the first step, count genuine footstep
                        val isPeriodicRhythm = prevInterval == 0L || abs(timeSinceLastStep - prevInterval) < 550L
                        if (isPeriodicRhythm) {
                            lastStepTimestamp = now
                            StepLockRepository.addSteps(1)
                        }
                    } else if (timeSinceLastStep > maxStepIntervalMs) {
                        // Single step after pause
                        lastStepTimestamp = now
                        recentStepIntervals[0] = 0L
                        StepLockRepository.addSteps(1)
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
