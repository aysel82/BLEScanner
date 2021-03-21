package com.example.myapplication.data

data class BLEDeviceDetail(
    var deviceName: String?,
    var macAddress: String?,
    var gattServices: List<GattService>? = null,
)

data class GattService(
    var gattServiceUUID: String? = null,
    var gattServiceCharacteristic: List<DetailCharacteristic>? = null,
    var message: String? = null
)

data class DetailCharacteristic(
    var characteristicUUID: String? = null,
    var characteristicProperties: DetailProperties? = null,
    var characteristicDescriptor: List<DetailDescriptor>? = null,
    var message: String? = null
)

data class DetailProperties(
    var propertyRead: String?,
    var propertyWrite: String?,
    var propertyNotify: String?,
    var propertyIndicate: String?
)

data class DetailDescriptor(
    var descriptorUUID: String? = null,
    var message: String? = null
)