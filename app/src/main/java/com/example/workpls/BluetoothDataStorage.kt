package com.example.workpls

data class BluetoothDataStorage(
    val indices: MutableList<String> = mutableListOf(),
    val senderNames: MutableList<String> = mutableListOf(),
    val humidities: MutableList<String> = mutableListOf(),
    val temperatures: MutableList<String> = mutableListOf(),
    val lights: MutableList<String> = mutableListOf()
) {
    // Funkcja do dodania obiektu BluetoothReading do odpowiednich list
    fun addReading(reading: BluetoothReading) {
        indices.add(reading.index)
        senderNames.add(reading.senderName)
        humidities.add(reading.humidity)
        temperatures.add(reading.temperature)
        lights.add(reading.light)
    }

    // Funkcja do wyczyszczenia wszystkich list
    fun clearAll() {
        indices.clear()
        senderNames.clear()
        humidities.clear()
        temperatures.clear()
        lights.clear()
    }
}
