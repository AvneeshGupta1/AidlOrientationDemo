package com.tesseract.demo

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.tesseract.aidllib.OrientationCallback
import com.tesseract.aidllib.OrientationInterface


class MainActivity : AppCompatActivity() {
    private var serviceCallBack: ServiceCallBack = ServiceCallBack()
    private var myServiceConnection: MyServiceConnection? = null
    private var orientationService: OrientationInterface? = null
    var rotationValueText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rotationValueText = findViewById(R.id.rotation_value_text)
        bindService()
    }


    override fun onDestroy() {
        super.onDestroy()
        unbindService()
    }

    private fun bindService() {
        myServiceConnection = MyServiceConnection()
        serviceCallBack = ServiceCallBack()
        val intent = Intent()
        intent.component = ComponentName(
            "com.tesseract.demo",
            "com.tesseract.aidllib.service.BoundService"
        )
        startService(intent)
        myServiceConnection?.let {
            bindService(intent, it, BIND_AUTO_CREATE)
        }
    }

    private fun unbindService() {
        myServiceConnection?.let { connection ->
            try {
                orientationService?.unregisterListener(serviceCallBack)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            unbindService(connection)
        }
    }

    inner class MyServiceConnection : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            orientationService = OrientationInterface.Stub.asInterface(iBinder)
            try {
                orientationService?.registerListener(serviceCallBack)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Log.d("", "")
        }
    }

    inner class ServiceCallBack : OrientationCallback.Stub() {
        @Throws(RemoteException::class)
        override fun handleOrintation(azimut: Float, pitch: Float, roll: Float) {
            rotationValueText?.text = "azimut: $azimut pitch: $pitch roll: $roll"
        }
    }
}