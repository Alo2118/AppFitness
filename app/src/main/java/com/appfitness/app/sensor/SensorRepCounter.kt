package com.appfitness.app.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.appfitness.app.domain.RepDetector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

/**
 * Counts repetitions in real time from the phone's accelerometer (worn on the
 * wrist), delegating the signal processing to [RepDetector]. Also estimates the
 * current cadence (reps/min) so the coaching layer can detect slowing down.
 */
class SensorRepCounter(context: Context) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer: Sensor? =
        sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val detector = RepDetector()
    private val recentRepTimes = ArrayDeque<Long>()

    private val _reps = MutableStateFlow(0)
    val reps: StateFlow<Int> = _reps.asStateFlow()

    private val _cadence = MutableStateFlow(0f)
    val cadence: StateFlow<Float> = _cadence.asStateFlow()

    fun hasSensor(): Boolean = accelerometer != null

    fun start() {
        val sensor = accelerometer ?: return
        sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
    }

    fun reset() {
        detector.reset()
        recentRepTimes.clear()
        _reps.value = 0
        _cadence.value = 0f
    }

    /** Manual fallback (e.g. the sensor missed a rep, or device has none). */
    fun addManualRep() {
        registerRep(System.currentTimeMillis())
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER || event.values.size < 3) return
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val magnitude = sqrt(x * x + y * y + z * z)
        if (detector.onSample(magnitude, System.currentTimeMillis())) {
            registerRep(System.currentTimeMillis())
        }
    }

    private fun registerRep(now: Long) {
        _reps.value += 1
        recentRepTimes.addLast(now)
        while (recentRepTimes.size > CADENCE_WINDOW) recentRepTimes.removeFirst()
        if (recentRepTimes.size >= 2) {
            val span = recentRepTimes.last() - recentRepTimes.first()
            val intervals = recentRepTimes.size - 1
            if (span > 0) _cadence.value = 60_000f * intervals / span
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { /* not used */ }

    companion object {
        private const val CADENCE_WINDOW = 5
    }
}
