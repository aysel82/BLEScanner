package com.example.myapplication.data

import android.bluetooth.BluetoothDevice

data class BLEDevice(
    val bleDevice: BluetoothDevice?,
    val order: Int,
    val deviceName: String?,
    val macAddress: String,
    var timestamp: String,
    val rssi: String,
    var isConnected: Boolean? = false
)