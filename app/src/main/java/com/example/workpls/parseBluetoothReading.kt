package com.example.workpls

import android.content.ContentValues.TAG
import android.util.Log

fun parseBluetoothReading(data: String): BluetoothReading {
    val parts = data.split(";") // Use the appropriate delimiter
    return if (parts.size == 5) {
        BluetoothReading(
            index = parts[0].trim(),
            senderName = parts[1].trim(),
            humidity = parts[2].trim(),
            temperature = parts[3].trim(),
            light = parts[4].trim()
        )
    } else {
        Log.e(TAG, "Invalid data format: $data")
        BluetoothReading() // Return an empty reading if format is invalid
    }
}