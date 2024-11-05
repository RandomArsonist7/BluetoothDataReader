package com.example.workpls

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.util.Log
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.util.UUID

// Class that will open the BT Socket to the Arduino BT Module
// Given a BT device, the UUID, and a Handler to set the results
class ConnectThread(
    device: BluetoothDevice,
    private val myUUID: UUID,
    private val handler: Handler,
    private val context: Context
) : Thread() {

    private val TAG = "FrugalLogs"
    private var mmSocket: BluetoothSocket? = createSocket(device)

    private fun createSocket(device: BluetoothDevice): BluetoothSocket? {
        return try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e(TAG, "Bluetooth permission not granted.")
                null
            } else {
                device.createRfcommSocketToServiceRecord(myUUID)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Socket's create() method failed", e)
            null
        }
    }

    override fun run() {
        // Check for Bluetooth permission
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Bluetooth permission not granted.")
            handler.obtainMessage(ERROR_READ, "Bluetooth permission not granted").sendToTarget()
            return // Exit the method if permission is not granted
        }

        try {
            // Attempt to connect to the Bluetooth device
            mmSocket?.connect()
        } catch (connectException: IOException) {
            Log.e(TAG, "Unable to connect to the Bluetooth device", connectException)
            handler.obtainMessage(ERROR_READ, "Unable to connect to the BT device").sendToTarget()
            try {
                mmSocket?.close()
            } catch (closeException: IOException) {
                Log.e(TAG, "Could not close the client socket", closeException)
            }
            return // Exit if connection fails
        }

        // Connection successful - Notify or proceed with connected socket handling
        Log.d(TAG, "Bluetooth device connected successfully")
        handler.obtainMessage(SUCCESS_CONNECT).sendToTarget()
    }

    fun cancel() {
        try {
            mmSocket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Could not close the client socket", e)
        }
    }

    fun getMmSocket(): BluetoothSocket? {
        return mmSocket
    }

    companion object {
        private const val ERROR_READ = 0
        private const val SUCCESS_CONNECT = 1
    }
}
