package com.example.android.marvincontrolpanel

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BtBroadcastReceiver(private val activity: MainActivity) : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        activity.setBluetoothPowerState(p1!!.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR))
    }
}