package com.example.android.marvincontrolpanel

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import java.util.*

data class BtConnectTask(val bluetoothDevice: BluetoothDevice, val uuid: UUID, val bluetoothAdapter: BluetoothAdapter)