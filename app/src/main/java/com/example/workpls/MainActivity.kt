package com.example.workpls

import ConnectedThread
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import android.os.Looper
import android.widget.Button
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val TAG = "FrugalLogs"

    // Define your permissions
    private val BLUETOOTH_PERMISSIONS = arrayOf(
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val REQUEST_ENABLE_BT = 1
    private var arduinoBTModule: BluetoothDevice? = null
    private var arduinoUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private lateinit var bluetoothPermissionLaunchers: List<ActivityResultLauncher<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the list of permission launchers
        bluetoothPermissionLaunchers = BLUETOOTH_PERMISSIONS.map { permission ->
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    Log.d(TAG, "$permission granted")
                } else {
                    Log.d(TAG, "$permission denied")
                }
            }
        }

        // Check and request all permissions
        checkPermissions()
    }

    private fun checkPermissions() {
        var allPermissionsGranted = true

        for (permission in BLUETOOTH_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false
                // Launch the permission request
                bluetoothPermissionLaunchers[BLUETOOTH_PERMISSIONS.indexOf(permission)].launch(permission)
            }
        }

        if (allPermissionsGranted) {
            // Proceed with Bluetooth operations
            Log.d(TAG, "All Bluetooth permissions are granted")
            initBluetooth() // Initialize Bluetooth operations
        }
    }

    private fun initBluetooth() {
        setContent {
            BluetoothApp()
        }
    }

    @SuppressLint("MissingPermission")
    @Composable
    fun BluetoothApp() {
        var btReadings by remember { mutableStateOf(BluetoothReading()) } // Stan dla odczytów Bluetooth
        var btDevices by remember { mutableStateOf(listOf<BluetoothDevice>()) } // Lista urządzeń Bluetooth

        val bluetoothAdapter = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        val context = LocalContext.current // Kontekst dla Toastów

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // Pierwszy Card - Wykryte i Sparowane urządzenia
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(3f)
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    // Rząd dla Wykrytych i Sparowanych urządzeń
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        // LazyColumn dla Wykrytych urządzeń
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp), // Dodaj trochę przestrzeni w pionie
                                    contentAlignment = Alignment.Center // Wyśrodkowanie zawartości
                                ) {
                                    Text(
                                        text = "Scanned Devices",
                                        fontSize = 20.sp, // Ustawienia rozmiaru czcionki
                                        fontWeight = FontWeight.Bold // Ustawienia pogrubienia
                                    )
                                }
                            }
                            items(btDevices) { device ->
                                Text(
                                    text = "${device.name} || ${device.address}",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            // Obsługa kliknięcia urządzenia
                                            arduinoBTModule = device
                                            connectToDevice { value ->
                                                btReadings = value
                                            }
                                        }
                                        .padding(8.dp)
                                )
                            }
                        }

// LazyColumn dla Sparowanych urządzeń
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                        ) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp), // Dodaj trochę przestrzeni w pionie
                                    contentAlignment = Alignment.Center // Wyśrodkowanie zawartości
                                ) {
                                    Text(
                                        text = "Paired Devices",
                                        fontSize = 20.sp, // Ustawienia rozmiaru czcionki
                                        fontWeight = FontWeight.Bold // Ustawienia pogrubienia
                                    )
                                }
                            }
                            items(btDevices) { device ->
                                Text(
                                    text = "${device.name} || ${device.address}",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            arduinoBTModule = device
                                            connectToDevice { value ->
                                                btReadings = value
                                            }
                                        }
                                        .padding(8.dp)
                                )
                            }
                        }
                    }

                    // Rząd dla przycisków Scan i Clear
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly // Distribute buttons evenly
                    ) {
                        // Button to clear values
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp), // Add some horizontal padding
                            contentAlignment = Alignment.Center // Center button horizontally
                        ) {
                            Button(onClick = {
                                btReadings = BluetoothReading() // Clear values
                                Toast.makeText(context, "Wartości wyczyszczone", Toast.LENGTH_SHORT).show()
                            }) {
                                Text("Clear")
                            }
                        }

                        // Button to scan for devices
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp), // Add some horizontal padding
                            contentAlignment = Alignment.Center // Center button horizontally
                        ) {
                            Button(onClick = {
                                val devices = searchPairedDevices(bluetoothAdapter)
                                if (devices.isEmpty()) {
                                    Toast.makeText(context, "Brak sparowanych urządzeń", Toast.LENGTH_SHORT).show()
                                } else {
                                    btDevices = devices // Update the list of devices
                                    Toast.makeText(context, "Znaleziono ${devices.size} sparowanych urządzeń", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Text("Scan")
                            }
                        }
                    }
                }
            }

            // Drugi Card - Obszar wykresu lub wizualizacji
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(7f)
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier
                            .padding(16.dp)
                            .size(width = 700.dp, height = 790.dp)
                    ) {Column(
                        modifier = Modifier.padding(16.dp), // Dodaj padding wewnętrzny dla Column
                        verticalArrangement = Arrangement.spacedBy(20.dp) // Odstępy między elementami
                    ) {
                        Text(
                            text = "Index: ${btReadings.index}",
                            fontSize = 40.sp // Zwiększ rozmiar czcionki
                        )
                        Text(
                            text = "Sender Name: ${btReadings.senderName}",
                            fontSize = 40.sp // Zwiększ rozmiar czcionki
                        )
                        Text(
                            text = "Humidity: ${btReadings.humidity}",
                            fontSize = 40.sp // Zwiększ rozmiar czcionki
                        )
                        Text(
                            text = "Temperature: ${btReadings.temperature}",
                            fontSize = 40.sp // Zwiększ rozmiar czcionki
                        )
                        Text(
                            text = "Light: ${btReadings.light}",
                            fontSize = 40.sp // Zwiększ rozmiar czcionki
                        )
                    }
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(onValueReceived: (BluetoothReading) -> Unit) {
        val bluetoothDataStorage = BluetoothDataStorage()// Updated callback type
        CoroutineScope(Dispatchers.IO).launch {
            val handler = Handler(Looper.getMainLooper())

            arduinoBTModule?.let { device ->
                val connectThread = ConnectThread(device, arduinoUUID, handler, this@MainActivity)
                connectThread.start()
                connectThread.join() // Wait for the thread to finish

                val socket = connectThread.getMmSocket() // Access the socket after connectThread has finished
                if (socket?.isConnected == true) {
                    Log.d(TAG, "Socket connected successfully")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Połączono z ${device.name}", Toast.LENGTH_SHORT).show()
                    }
                    val connectedThread = ConnectedThread(socket)
                    connectedThread.start()

                    // Continuously read values
                    while (true) {
                        val valueRead = connectedThread.valueRead // Pobieranie wiadomości z Bluetooth
                        valueRead?.let { message ->
                            // Użycie parsera do przekształcenia wiadomości na BluetoothReading
                            val reading = parseBluetoothReading(message)

                            // Dodanie odczytanych wartości do odpowiednich list w bluetoothDataStorage
                            bluetoothDataStorage.addReading(reading)

                            withContext(Dispatchers.Main) {
                                // Wywołanie callbacku, aby przesłać odczyt do interfejsu
                                onValueReceived(reading)
                            }
                        }
                        delay(10000) // Odczyt co 10 sekund
                    }
                } else {
                    Log.e(TAG, "Socket not connected")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Nie udało się połączyć z ${device.name}", Toast.LENGTH_SHORT).show()
                    }
                }
                connectThread.cancel()
            } ?: run {
                Log.e(TAG, "Bluetooth device is not available")
            }
        }
    }

    private fun searchPairedDevices(bluetoothAdapter: BluetoothAdapter): List<BluetoothDevice> {
        val devicesList = mutableListOf<BluetoothDevice>()

        if (bluetoothAdapter == null) {
            Log.d(TAG, "Device doesn't support Bluetooth")
            return devicesList // Return an empty list
        }

        // Check if Bluetooth is enabled
        if (!bluetoothAdapter.isEnabled) {
            // Request to enable Bluetooth
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            return devicesList // Return an empty list
        }

        // Check permissions before accessing paired devices
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Bluetooth connect permission is not granted")
            // Optionally, show a message to the user that permission is required
            return devicesList // Return an empty list
        }

        // Get paired devices
        val pairedDevices = bluetoothAdapter.bondedDevices
        pairedDevices.forEach { device ->
            Log.d(TAG, "Found device: ${device.name} with address ${device.address}")
            devicesList.add(device) // Add each device to the list
        }

        return devicesList // Return the list of Bluetooth devices
    }
}




