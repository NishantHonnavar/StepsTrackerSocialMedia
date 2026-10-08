package com.example

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.sqrt

class StepSensorManager(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private var stepCounterSensor: Sensor? = null
    private var stepDetectorSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null

    private var previousStepCounterValue: Float = -1f
    private var isListening = false
    private var hasReceivedHardwareSteps = false

    // Accelerometer peak detection variables for responsive fallback background counting
    private var lastAccelMagnitude: Float = 9.8f
    private var lastStepTimestamp: Long = 0L
    private val stepThresholdHigh = 11.2f // Tuned for higher sensitivity to normal walking
    private val stepThresholdLow = 9.4f
    private var isPeakWaitingForValley = false

    init {
        stepCounterSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepDetectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        accelerometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val hasHardwareSensor = stepCounterSensor != null || stepDetectorSensor != null
        val sensorName = stepCounterSensor?.name
            ?: stepDetectorSensor?.name
            ?: accelerometerSensor?.name
            ?: "Scroll Tax Sensor Engine"

        StepLockRepository.setStepSensorInfo(hasHardwareSensor || accelerometerSensor != null, sensorName)
        Log.d("StepSensorManager", "Initialized with sensor: $sensorName (HW: $hasHardwareSensor)")
    }

    fun startListening() {
        if (isListening || sensorManager == null) return

        var registered = false
        // Primary: Hardware Step Counter
        stepCounterSensor?.let { sensor ->
            val success = sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_UI
            )
            if (success) registered = true
        }

        // Secondary: Hardware Step Detector
        stepDetectorSensor?.let { sensor ->
            val success = sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_UI
            )
            if (success) registered = true
        }

        // Tertiary fallback: Accelerometer for guaranteed background & emulator step counting
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

        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val totalStepsSinceBoot = event.values[0]
                if (previousStepCounterValue < 0f) {
                    previousStepCounterValue = totalStepsSinceBoot
                } else {
                    val delta = (totalStepsSinceBoot - previousStepCounterValue).toInt()
                    if (delta > 0 && delta < 5000) { // sanity check
                        hasReceivedHardwareSteps = true
                        StepLockRepository.addSteps(delta)
                        previousStepCounterValue = totalStepsSinceBoot
                    }
                }
            }

            Sensor.TYPE_STEP_DETECTOR -> {
                if (event.values.isNotEmpty() && event.values[0] == 1.0f) {
                    hasReceivedHardwareSteps = true
                    StepLockRepository.addSteps(1)
                }
            }

            Sensor.TYPE_ACCELEROMETER -> {
                // If a dedicated hardware step counter/detector is actively providing events,
                // prioritize it to avoid duplicate counting. If not (e.g. emulators or devices without HW step FIFO),
                // the accelerometer peak-valley detector immediately processes steps!
                if (hasReceivedHardwareSteps) return

                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

                val now = System.currentTimeMillis()
                // Cadence check: normal human walking is ~1.5 to 3 steps per second (min 240ms interval)
                if (magnitude > stepThresholdHigh && !isPeakWaitingForValley && (now - lastStepTimestamp > 240)) {
                    isPeakWaitingForValley = true
                } else if (magnitude < stepThresholdLow && isPeakWaitingForValley) {
                    isPeakWaitingForValley = false
                    lastStepTimestamp = now
                    StepLockRepository.addSteps(1)
                }
                lastAccelMagnitude = magnitude
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
