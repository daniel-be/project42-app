package com.example.android.marvincontrolpanel

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.IntentFilter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    companion object {
        private const val LOG_TAG = "MainActivity"

        const val REQUEST_ENABLE_BT = 1
    }

    private val broadcastReceiver = BtBroadcastReceiver(this)
    private var btPowerState = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intentFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        this.registerReceiver(broadcastReceiver, intentFilter)

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Log.e(LOG_TAG, "Device does not support bluetooth!")
        }

        if (!bluetoothAdapter.isEnabled) {
            SetBluetoothPowerState(BluetoothAdapter.STATE_OFF)
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else {
            SetBluetoothPowerState(BluetoothAdapter.STATE_ON)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.panel_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.itemId == R.id.menu_settings) {
            val intentSettingsActivity = Intent(this, SettingsActivity::class.java)
            startActivity(intentSettingsActivity)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode ==  Activity.RESULT_OK) {
                Log.d(LOG_TAG, "Bluetooth enabled.")
            } else {
                Log.e(LOG_TAG, "Enabling bluetooth failed!")
            }
        }
    }

    fun SetBluetoothPowerState(state: Int) {
        btPowerState = state
        val btInfo = findViewById<TextView>(R.id.bluetooth_power_state)
        btInfo.text = when (state) {
            BluetoothAdapter.ERROR ->  getString(R.string.bt_power_err)
            BluetoothAdapter.STATE_TURNING_OFF -> getString(R.string.bt_power_turning_off)
            BluetoothAdapter.STATE_TURNING_ON -> getString(R.string.bt_power_turning_on)
            BluetoothAdapter.STATE_OFF -> getString(R.string.bt_power_off)
            BluetoothAdapter.STATE_ON -> getString(R.string.bt_power_on)
            else -> getString(R.string.bt_power_undefined)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(broadcastReceiver)
    }
}
