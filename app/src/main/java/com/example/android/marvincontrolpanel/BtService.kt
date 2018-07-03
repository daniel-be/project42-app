package com.example.android.marvincontrolpanel

import android.bluetooth.BluetoothSocket

class BtService(private val btServiceHandler: BtServiceHandler) {
    companion object {
        const val DISCONNECTED = 0
        const val CONNECTING = 1
        const val CONNECTED = 2
        const val RPI_NOT_PAIRED = 3
        const val FAILED = 4
        const val HANDLER_STATE_MSG = 10
        const val HANDLER_MSG_MSG = 11
    }

    private var mBtConnectThread: BtConnectThread? = null
    private var mBtConnectionThread: BtConnectionThread? = null
    private var mState: Int = DISCONNECTED

    fun connect(btConnectTask: BtConnectTask) {
        synchronized(this, {
            mBtConnectThread?.cancel()
            mBtConnectionThread?.cancel()
            mBtConnectThread = BtConnectThread(btConnectTask, this)
            mBtConnectThread!!.start()

            setState(CONNECTING)
        })
    }

    fun connected(bluetoothSocket: BluetoothSocket) {
        synchronized(this, {
            mBtConnectionThread?.cancel()
            mBtConnectionThread = BtConnectionThread(BtConnectionTask(bluetoothSocket), this)
            mBtConnectionThread!!.start()

            setState(CONNECTED)
        })
    }

    fun connectionFailed() {
        synchronized(this, {
            setState(FAILED)
        })
    }

    fun connectionLost() {
        synchronized(this, {
            setState(DISCONNECTED)
        })
    }

    fun stop() {
        synchronized(this, {
            mBtConnectThread?.cancel()
            mBtConnectionThread?.cancel()

            setState(DISCONNECTED)
        })
    }

    fun write(out: ByteArray) {
        var r: BtConnectionThread? = null
        synchronized(this, {
            if (mState != CONNECTED) { return }

            r = mBtConnectionThread
        })

        r?.write(out)
    }

    private fun setState(state: Int) {
        mState = state

        btServiceHandler.obtainMessage(HANDLER_STATE_MSG, state).sendToTarget()
    }

    fun addMessage(bytes: Int?, buffer: ByteArray) {
        btServiceHandler.obtainMessage(HANDLER_MSG_MSG, bytes!!, -1, buffer).sendToTarget()
    }
}