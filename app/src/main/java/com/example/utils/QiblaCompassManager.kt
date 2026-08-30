package com.example.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class QiblaCompassManager(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)

    private val _azimuth = MutableStateFlow(0f)
    val azimuth: StateFlow<Float> = _azimuth.asStateFlow()

    private val _accuracy = MutableStateFlow(SensorManager.SENSOR_STATUS_ACCURACY_HIGH)
    val accuracy: StateFlow<Int> = _accuracy.asStateFlow()

    private val _isSensorAvailable = MutableStateFlow(true)
    val isSensorAvailable: StateFlow<Boolean> = _isSensorAvailable.asStateFlow()

    private var isListening = false

    companion object {
        // Kaaba, Mecca coordinates
        const val MECCA_LATITUDE = 21.422487
        const val MECCA_LONGITUDE = 39.826206

        /**
         * Calculates Qibla Bearing (in degrees from true north 0..360) from current GPS coordinates.
         */
        fun calculateQiblaBearing(userLat: Double, userLng: Double): Float {
            val phiK = Math.toRadians(MECCA_LATITUDE)
            val lambdaK = Math.toRadians(MECCA_LONGITUDE)
            val phi = Math.toRadians(userLat)
            val lambda = Math.toRadians(userLng)

            val deltaL = lambdaK - lambda
            val y = sin(deltaL) * cos(phiK)
            val x = cos(phi) * sin(phiK) - sin(phi) * cos(phiK) * cos(deltaL)

            val bearingRad = atan2(y, x)
            var bearingDeg = Math.toDegrees(bearingRad).toFloat()
            if (bearingDeg < 0) {
                bearingDeg += 360f
            }
            return bearingDeg
        }

        /**
         * Calculates distance in kilometers between user and Kaaba using Haversine formula.
         */
        fun calculateDistanceToKaabaKm(userLat: Double, userLng: Double): Double {
            val r = 6371.0 // Radius of Earth in km
            val dLat = Math.toRadians(MECCA_LATITUDE - userLat)
            val dLng = Math.toRadians(MECCA_LONGITUDE - userLng)
            val a = sin(dLat / 2) * sin(dLat / 2) +
                    cos(Math.toRadians(userLat)) * cos(Math.toRadians(MECCA_LATITUDE)) *
                    sin(dLng / 2) * sin(dLng / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return r * c
        }
    }

    fun startListening() {
        if (isListening) return
        val hasAccel = accelerometer != null
        val hasMag = magnetometer != null
        val hasRot = rotationSensor != null

        if (!hasRot && (!hasAccel || !hasMag)) {
            _isSensorAvailable.value = false
            return
        }

        if (hasRot) {
            sensorManager?.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            sensorManager?.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
        }
        isListening = true
    }

    fun stopListening() {
        if (!isListening) return
        sensorManager?.unregisterListener(this)
        isListening = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientation)
            var azimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
            if (azimuthDeg < 0) azimuthDeg += 360f
            _azimuth.value = smoothAzimuth(_azimuth.value, azimuthDeg)
            return
        }

        val alpha = 0.8f
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]
        }
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic[0] = alpha * geomagnetic[0] + (1 - alpha) * event.values[0]
            geomagnetic[1] = alpha * geomagnetic[1] + (1 - alpha) * event.values[1]
            geomagnetic[2] = alpha * geomagnetic[2] + (1 - alpha) * event.values[2]
        }

        val success = SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)
        if (success) {
            SensorManager.getOrientation(rotationMatrix, orientation)
            var azimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
            if (azimuthDeg < 0) azimuthDeg += 360f
            _azimuth.value = smoothAzimuth(_azimuth.value, azimuthDeg)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor?.type == Sensor.TYPE_MAGNETIC_FIELD || sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            _accuracy.value = accuracy
        }
    }

    private fun smoothAzimuth(oldAzimuth: Float, newAzimuth: Float): Float {
        var diff = newAzimuth - oldAzimuth
        while (diff < -180) diff += 360
        while (diff > 180) diff -= 360
        return (oldAzimuth + 0.2f * diff + 360) % 360
    }
}
