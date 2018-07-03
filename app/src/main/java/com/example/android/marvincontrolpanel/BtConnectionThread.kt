package com.example.android.marvincontrolpanel

import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class BtConnectionThread(private val btConnectionTask: BtConnectionTask, private val btService: BtService) : Thread() {
    companion object {
        private const val LOG_TAG = "BtConnectionThread"
    }

    private var inStream: InputStream?
    private var outStream: OutputStream?

    init {
        try {
            inStream = btConnectionTask.bluetoothSocket.inputStream
        } catch (e: IOException) {
            inStream = null
            Log.e(LOG_TAG, "Error occurred when creating input stream", e)
        }
        try {
            outStream = btConnectionTask.bluetoothSocket.outputStream
        } catch (e: IOException) {
            outStream = null
            Log.e(LOG_TAG, "Error occurred when creating output stream", e)
        }
    }

    override fun run() {
        val buffer = ByteArray(1204)
        var bytes: Int?

        while (true) {
            try {
                bytes = inStream?.read(buffer)
                btService.addMessage(bytes, buffer)
            } catch (e: IOException) {
                Log.d(LOG_TAG, "Input stream was disconnected")
                btService.connectionLost()
                break
            }
        }
    }

    fun write(bytes: ByteArray) {
        try {
            outStream?.write(bytes)
        } catch (e: IOException) {
            Log.e(LOG_TAG, "Error occurred when sending data", e)
        }
    }

    fun cancel() {
        try {
            btConnectionTask.bluetoothSocket.close()
        } catch (e: IOException) {
            Log.e(LOG_TAG, "Could not close the client socket", e)
        }
    }
}