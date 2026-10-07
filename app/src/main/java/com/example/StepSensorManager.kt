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

    // Accelerometer peak detection variables for fallback background counting
    private var lastAccelMagnitude: Float = 9.8f
    private var lastStepTimestamp: Long = 0L
    private val stepThresholdHigh = 11.8f
    private val stepThresholdLow = 9.2f
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
    }

    fun startListening() {
        if (isListening || sensorManager == null) return

        var registered = false
        // Primary: Hardware Step Counter
        stepCounterSensor?.let { sensor ->
            val success = sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            if (success) registered = true
        }

        // Secondary: Hardware Step Detector
        stepDetectorSensor?.let { sensor ->
            val success = sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            if (success) registered = true
        }

        // Tertiary fallback: Accelerometer for guaranteed background step counting
        accelerometerSensor?.let { sensor ->
            val success = sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            if (success) registered = true
        }

        isListening = registered
        Log.d("StepSensorManager", "Step sensors registered successfully (Background active): $isListening")
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
                        StepLockRepository.addSteps(delta)
                        previousStepCounterValue = totalStepsSinceBoot
                    }
                }
            }

            Sensor.TYPE_STEP_DETECTOR -> {
                if (event.values.isNotEmpty() && event.values[0] == 1.0f) {
                    StepLockRepository.addSteps(1)
                }
            }

            Sensor.TYPE_ACCELEROMETER -> {
                // If hardware step counter is actively delivering data, avoid double-counting
                if (stepCounterSensor != null || stepDetectorSensor != null) return

                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

                val now = System.currentTimeMillis()
                if (magnitude > stepThresholdHigh && !isPeakWaitingForValley && (now - lastStepTimestamp > 280)) {
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
