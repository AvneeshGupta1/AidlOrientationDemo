package com.tesseract.aidllib.service

import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.os.RemoteCallbackList
import android.os.RemoteException
import android.util.Log
import com.tesseract.aidllib.OrientationCallback
import com.tesseract.aidllib.OrientationInterface


class BoundService : Service(), SensorEventListener {
    private var mSensorManager: SensorManager? = null
    private var mRotationSensor: Sensor? = null
    private val SENSOR_DELAY = 8000  //8ms
    private val listeners: RemoteCallbackList<OrientationCallback> = RemoteCallbackList()

    override fun onCreate() {
        super.onCreate()
        try {
            mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
            mRotationSensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            mSensorManager!!.registerListener(this, mRotationSensor, SENSOR_DELAY)
        } catch (e: Exception) {
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor == mRotationSensor) {
            if (event.values.size > 4) {
                val truncatedRotationVector = FloatArray(4)
                System.arraycopy(event.values, 0, truncatedRotationVector, 0, 4)
                update(truncatedRotationVector)
            } else {
                update(event.values)
            }
        }
    }

    private fun update(vectors: FloatArray) {
        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, vectors)
        val worldAxisX = SensorManager.AXIS_X
        val worldAxisZ = SensorManager.AXIS_Z
        val adjustedRotationMatrix = FloatArray(9)
        SensorManager.remapCoordinateSystem(
            rotationMatrix,
            worldAxisX,
            worldAxisZ,
            adjustedRotationMatrix
        )
        val orientation = FloatArray(3)
        SensorManager.getOrientation(adjustedRotationMatrix, orientation)
        val azimut = orientation[0]
        val pitch = orientation[1]
        val roll = orientation[2]
        try {
            listeners.beginBroadcast()
            sendMessageToAllClients(azimut, pitch, roll)
            listeners.finishBroadcast()

        } catch (e: RemoteException) {
            Log.d("", "")
        }
    }

    private fun sendMessageToAllClients(azimut: Float, pitch: Float, roll: Float) {
        for (i in 0 until listeners.registeredCallbackCount) {
            try {
                listeners.getBroadcastItem(i).handleOrintation(azimut, pitch, roll)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    private val mBinder: IBinder = object : OrientationInterface.Stub() {
        override fun registerListener(callback: OrientationCallback?) {
            callback?.let {
                listeners.register(it)
            }
        }

        override fun unregisterListener(callback: OrientationCallback?) {
            callback?.let {
                listeners.unregister(it)
            }
        }

    }
}