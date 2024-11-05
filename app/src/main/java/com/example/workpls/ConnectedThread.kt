import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class ConnectedThread(private val socket: BluetoothSocket) : Thread() {
    private val inputStream: InputStream? = socket.inputStream
    private val outputStream: OutputStream? = socket.outputStream
    var valueRead: String? = null

    override fun run() {
        val buffer = ByteArray(1024)
        var bytes: Int

        while (true) {
            try {
                bytes = inputStream?.read(buffer) ?: 0
                valueRead = String(buffer, 0, bytes)
            } catch (e: IOException) {
                Log.e("ConnectedThread", "Input stream was disconnected", e)
                break
            }
        }
    }

    fun cancel() {
        try {
            socket.close()
        } catch (e: IOException) {
            Log.e("ConnectedThread", "Could not close the socket", e)
        }
    }
}