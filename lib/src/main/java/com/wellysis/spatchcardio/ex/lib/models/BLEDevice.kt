package com.wellysis.spatchcardio.ex.lib.models

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice

class BLEDevice(var device: BluetoothDevice, var rssi: Int) {
    val name: String
        @SuppressLint("MissingPermission")
        get() = device.name ?: ""

    val macAddress: String get() = device.address

    val mRssi: Int get() = rssi

}