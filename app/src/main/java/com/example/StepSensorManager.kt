package com.example

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class StepSensorManager(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private var stepCounterSensor: Sensor? = null
    private var stepDetectorSensor: Sensor? = null

    private var previousStepCounterValue: Float = -1f
    private var isListening = false

    init {
        stepCounterSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepDetectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        val hasSensor = stepCounterSensor != null || stepDetectorSensor != null
        val sensorName = stepCounterSensor?.name ?: stepDetectorSensor?.name ?: "None"
        StepLockRepository.setStepSensorInfo(hasSensor, sensorName)
    }

    fun startListening() {
        if (isListening || sensorManager == null) return

        var registered = false
        stepCounterSensor?.let { sensor ->
            val success = sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_UI
            )
            if (success) registered = true
        }

        stepDetectorSensor?.let { sensor ->
            val success = sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_UI
            )
            if (success) registered = true
        }

        isListening = registered
        Log.d("StepSensorManager", "Step sensor registered: $isListening")
    }

    fun stopListening() {
        if (!isListening || sensorManager == null) return
        sensorManager.unregisterListener(this)
        isListening = false
        previousStepCounterValue = -1f
        Log.d("StepSensorManager", "Step sensor unregistered")
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
                    if (delta > 0) {
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
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
