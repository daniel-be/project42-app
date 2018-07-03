package com.example.android.marvincontrolpanel

import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException

class BtConnectThread(private val btConnectTask: BtConnectTask, private val btService: BtService) : Thread() {
    companion object {
        private const val LOG_TAG = "BtConnectThread"
    }

    private var bluetoothSocket: BluetoothSocket?

    init {
        try {
            bluetoothSocket = btConnectTask.bluetoothDevice.createRfcommSocketToServiceRecord(btConnectTask.uuid)
        } catch (e: IOException) {
            bluetoothSocket = null
            Log.e(LOG_TAG, "Socket's create() method failed", e)
        }
    }

    override fun run() {
        btConnectTask.bluetoothAdapter.cancelDiscovery()

        if (bluetoothSocket == null) {
            btService.connectionFailed()
            return
        }

        try {
            bluetoothSocket?.connect()
        } catch (connectException: IOException) {
            Log.e(LOG_TAG, "Connecting bluetooth to server failed", connectException)
            btService.connectionFailed()

            try{
                bluetoothSocket?.close()
            } catch (closeException: IOException) {
                Log.e(LOG_TAG, "Could not close the client socket", closeException)
            }
            return
        }

        btService.connected(bluetoothSocket!!)
    }

    fun cancel() {
        try {
            bluetoothSocket?.close()
        } catch (e: IOException) {
            Log.e(LOG_TAG, "Could not close the client socket", e)
        }
    }
}