package com.example.myapplication.util

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.data.*

private const val TAG = "BLEUtil"

val bleDeviceScanning = MutableLiveData<Boolean>()
var bluetoothAdapter: BluetoothAdapter? = null
val bleDeviceList = MutableLiveData<List<BLEDevice>>()
private var bleDeviceDataList = mutableListOf<BLEDevice>()
var bleDeviceDetail: BLEDeviceDetail? = null
var bluetoothGatt: BluetoothGatt? = null
var selectedDevice: BluetoothDevice? = null

fun scanLeDevice(enable: Boolean) {
    if (enable) {
        startScanning()
    } else {
        stopScanning()
    }
}

// Device scan callback.
private var mLeScanCallback: ScanCallback =
    object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.let { scanResult ->
                addBLEDeviceList(scanResult)
            }
        }

        override fun onBatchScanResults(results: List<ScanResult?>?) {
            super.onBatchScanResults(results)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e(TAG, "Error: $errorCode")
            bleDeviceList.value = mutableListOf()
        }
    }

private fun startScanning() {
    bleDeviceScanning.value = true
    bluetoothAdapter!!.bluetoothLeScanner.startScan(mLeScanCallback)
    Log.i(TAG, "Start scan")
}

private fun stopScanning() {
    bleDeviceScanning.value = false
    bluetoothAdapter!!.bluetoothLeScanner.stopScan(mLeScanCallback)
    Log.i(TAG, "Stop scan")
}

private fun addBLEDeviceList(scanResult: ScanResult) {

    if (scanResult.device.name.isNullOrEmpty())
        return

    val device = bleDeviceDataList.find { it.macAddress == scanResult.device.address }

    device?.let { bleDevice ->
        Log.i(TAG, "Same device: $device")
        bleDevice.timestamp = scanResult.timestampNanos.toString()
        return
    }

    val bleDevice = BLEDevice(
        bleDevice = scanResult.device,
        order = bleDeviceDataList.count() + 1,
        deviceName = scanResult.device.name,
        macAddress = scanResult.device.address,
        timestamp = scanResult.timestampNanos.toString(),
        rssi = scanResult.rssi.toString()
    )
    Log.i(TAG, "Scan result: $scanResult")
    Log.i(TAG, "Device: $bleDevice")

    bleDeviceDataList.add(bleDevice)
    bleDeviceList.value = bleDeviceDataList
}

fun connectBLEDevice(
    context: Context,
    callback: (ConnectionStatus) -> Unit
) {
    selectedDevice?.connectGatt(
        context,
        false,
        object : BluetoothGattCallback() {
            override fun onConnectionStateChange(
                gatt: BluetoothGatt?,
                status: Int,
                newState: Int
            ) {
                super.onConnectionStateChange(gatt, status, newState)
                bluetoothGatt = gatt
                var connectionStatus: ConnectionStatus? = null
                when (newState) {
                    (BluetoothProfile.STATE_CONNECTED) -> {
                        connectionStatus = ConnectionStatus.CONNECTED
                        Log.w(
                            "BluetoothGattCallback",
                            "Successfully connected to ${selectedDevice?.address}"
                        )
                        gatt!!.discoverServices()
                    }
                    (BluetoothProfile.STATE_DISCONNECTED) -> {
                        connectionStatus = ConnectionStatus.DISCONNECTED
                        Log.w(
                            "BluetoothGattCallback",
                            "Successfully disconnected from ${selectedDevice?.address}"
                        )
                        gatt?.disconnect()
                        gatt?.close()
                    }
                    (BluetoothProfile.STATE_CONNECTING) -> {
                        connectionStatus = ConnectionStatus.CONNECTING
                        Log.w(
                            "BluetoothGattCallback",
                            "Error $status encountered for ${selectedDevice?.address}! Disconnecting..."
                        )
                        gatt?.close()
                    }
                    (BluetoothProfile.STATE_DISCONNECTING) -> {
                        connectionStatus = ConnectionStatus.DISCONNECTING
                    }
                    else -> {
                        connectionStatus = ConnectionStatus.FAILURE
                    }
                }
                Log.i(TAG, connectionStatus.toString())
                callback.invoke(connectionStatus)
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                super.onServicesDiscovered(gatt, status)
                getDeviceDetail(gatt!!.services) { gatServiceList ->

                    bleDeviceDetail = BLEDeviceDetail(
                        deviceName = selectedDevice?.name,
                        macAddress = selectedDevice?.address,
                        gattServices = gatServiceList
                    )
                    callback.invoke(ConnectionStatus.SERVICE_DISCOVERED)
                }
            }
        })
}

fun disconnectBLEDevice() {
    bluetoothGatt?.let { gatt ->
        Log.i(TAG, ConnectionStatus.DISCONNECTED.toString())
        gatt.disconnect()
        gatt.close()
    }
}

private fun getDeviceDetail(
    services: List<BluetoothGattService>,
    callback: (List<GattService>?) -> Unit
) {

    var gattServiceDescriptorList: MutableList<DetailDescriptor>?
    var gattServiceProperties: DetailProperties?
    val gattServiceCharacteristicList: MutableList<DetailCharacteristic>? =
        mutableListOf()
    val gattServiceList: MutableList<GattService>? = mutableListOf()

    services.forEach { service ->

        val characteristics = service.characteristics

        characteristics.forEach { characteristic ->

            var read = "Read not supported"
            var write = "Write not supported"
            var notify = "Notify not supported"
            var indicate = "Indicate not supported"

            if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_READ == 1) {
                read = "Read supported"
                Log.i(TAG, "Read supported")
            } else {
                Log.i(TAG, "Read not supported")
            }

            if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE == 1) {
                write = "Write supported"
                Log.i(TAG, "Write supported")
            } else {
                Log.i(TAG, "Write not supported")
            }

            if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY == 1) {
                notify = "Notify supported"
                Log.i(TAG, "Notify supported")
            } else {
                Log.i(TAG, "Notify not supported")
            }

            if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE == 1) {
                indicate = "Indicate supported"
                Log.i(TAG, "Indicate supported")
            } else {
                Log.i(TAG, "Indicate not supported")
            }

            gattServiceProperties =
                DetailProperties(read, write, notify, indicate)

            gattServiceDescriptorList = mutableListOf()
            val descriptors = characteristic.descriptors
            descriptors.forEach { descriptor ->
                gattServiceDescriptorList?.add(
                    DetailDescriptor(
                        descriptorUUID = descriptor.uuid.toString()
                    )
                )
            }

            if (descriptors.isNullOrEmpty()) {
                // if no descriptor is advertised
                gattServiceDescriptorList?.add(
                    DetailDescriptor(
                        message = "No characteristics is advertised"
                    )
                )
            }

            gattServiceCharacteristicList?.add(
                DetailCharacteristic(
                    characteristicUUID = characteristic.uuid.toString(),
                    characteristicProperties = gattServiceProperties,
                    characteristicDescriptor = gattServiceDescriptorList,
                    message = null
                )
            )
        }

        if (characteristics.isNullOrEmpty()) {
            // if no characteristic is advertised
            DetailCharacteristic(
                message = "No characteristics is advertised"
            )
        }

        gattServiceList?.add(
            GattService(
                gattServiceUUID = service.uuid.toString(),
                gattServiceCharacteristic = gattServiceCharacteristicList
            )
        )
    }

    if (services.isNullOrEmpty()) {
        // if no services is advertised
        gattServiceList?.add(
            GattService(
                message = "No services is advertised"
            )
        )
    }

    Log.i(TAG, gattServiceList.toString())
    callback.invoke(gattServiceList)
}

enum class
ConnectionStatus {
    CONNECTED,
    CONNECTING,
    DISCONNECTED,
    DISCONNECTING,
    SERVICE_DISCOVERED,
    FAILURE
}

