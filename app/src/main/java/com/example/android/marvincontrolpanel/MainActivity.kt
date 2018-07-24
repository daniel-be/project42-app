package com.example.android.marvincontrolpanel

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.preference.PreferenceManager
import android.support.constraint.ConstraintLayout
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import java.nio.ByteBuffer
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object {
        private const val LOG_TAG = "MainActivity"

        const val REQUEST_ENABLE_BT = 1
    }

    private val broadcastReceiver = BtBroadcastReceiver(this)
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private lateinit var sharedPreferences: SharedPreferences
    private var btPowerState = -1
    private var connectionState = -1
    private lateinit var btService: BtService
    private var logMsgListView: ListView? = null
    private var logMsgAdapter: ArrayAdapter<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        val intentFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        this.registerReceiver(broadcastReceiver, intentFilter)

        if (bluetoothAdapter == null) {
            Log.e(LOG_TAG, "Device does not support bluetooth!")
        }

        if (!bluetoothAdapter.isEnabled) {
            setBluetoothPowerState(BluetoothAdapter.STATE_OFF)
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else {
            setBluetoothPowerState(BluetoothAdapter.STATE_ON)
        }

        val handler = BtServiceHandler(this)
        btService = BtService(handler)

        logMsgListView = findViewById(R.id.log_msg_view)
        logMsgAdapter = ArrayAdapter<String>(this, R.layout.message)
        logMsgListView!!.adapter = logMsgAdapter

        setConnectionState(BtService.DISCONNECTED)
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
        } else if(item!!.itemId == R.id.menu_connect) {
            val pairedDevices = bluetoothAdapter.bondedDevices
            if (pairedDevices.size > 0) {
                val rpiDevice = pairedDevices.find { d -> d.address == sharedPreferences.getString(getString(R.string.raspberry_pi_bt_mac), null) }
                if (rpiDevice != null) {
                    val btConnectTask = BtConnectTask(rpiDevice, UUID.fromString(getString(R.string.uuid)), bluetoothAdapter)
                    btService.connect(btConnectTask)
                } else {
                    setConnectionState(BtService.RPI_NOT_PAIRED)
                }
            } else {
                Log.d(LOG_TAG, "No paired devices found.")
            }
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

    fun setBluetoothPowerState(state: Int) {
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

    fun handleMessage(msg: Message?) {
        when (msg!!.what) {
            BtService.HANDLER_STATE_MSG -> setConnectionState(msg!!.obj as Int)
            BtService.HANDLER_MSG_MSG -> addMessageToListView(msg!!.arg1, msg!!.obj as ByteArray)
        }
    }

    private fun setConnectionState(state: Int) {
        connectionState = state

        if (state == BtService.CONNECTED) {
            logMsgAdapter!!.clear()
        }

        findViewById<ConstraintLayout>(R.id.btnContainer).visibility = when (state) {
            BtService.CONNECTED -> View.VISIBLE
            else -> View.INVISIBLE
        }

        findViewById<ConstraintLayout>(R.id.log_window).visibility = when (state) {
            BtService.CONNECTED -> View.VISIBLE
            else -> View.INVISIBLE
        }

        findViewById<ConstraintLayout>(R.id.connect_to_hint).visibility = when (state) {
            BtService.CONNECTED -> View.INVISIBLE
            else -> View.VISIBLE
        }

        val connectionInfo = findViewById<TextView>(R.id.connection_state)
        connectionInfo.text = when (state) {
            BtService.CONNECTED ->  getString(R.string.connection_connected)
            BtService.CONNECTING -> getString(R.string.connection_connecting)
            BtService.DISCONNECTED -> getString(R.string.connection_disconnected)
            BtService.RPI_NOT_PAIRED -> getString(R.string.connection_rpi_not_paired, sharedPreferences.getString(getString(R.string.raspberry_pi_bt_mac), null))
            BtService.FAILED -> getString(R.string.connection_failed)
            else -> getString(R.string.bt_power_undefined)
        }
    }

    private fun addMessageToListView(bytes: Int, buffer: ByteArray) {
        val message = String(buffer, 0, bytes)
        logMsgAdapter!!.add(message)
    }

    fun animalSelected(v: View) {
        logMsgAdapter!!.clear()
        val tag = v.tag.toString()
        val data = ByteArray(15)
        data[0] = sharedPreferences.getString(tag + "_hue_lower", getString(R.string.hsv_zero)).toInt().toByte()
        data[1] = sharedPreferences.getString(tag + "_sat_lower", getString(R.string.hsv_zero)).toInt().toByte()
        data[2] = sharedPreferences.getString(tag + "_val_lower", getString(R.string.hsv_zero)).toInt().toByte()
        data[3] = sharedPreferences.getString(tag + "_hue_upper", getString(R.string.hsv_hue_max)).toInt().toByte()
        data[4] = sharedPreferences.getString(tag + "_sat_upper", getString(R.string.hsv_max)).toInt().toByte()
        data[5] = sharedPreferences.getString(tag + "_val_upper", getString(R.string.hsv_max)).toInt().toByte()
        data[6] = sharedPreferences.getString(tag + "_tolerance_to_middle", getString(R.string.tolerance_to_middle_default)).toInt().toByte()

        val intBytes = ByteBuffer.allocate(4).putInt(sharedPreferences.getString(tag + "_min_cont_size", getString(R.string.hsv_zero)).toInt()).array()
        var i = 7
        for (b in intBytes) {
            data[i] = b
            i++
        }

        val nextIntBytes = ByteBuffer.allocate(4).putInt(sharedPreferences.getString(tag + "_cont_size_tolerance", getString(R.string.hsv_zero)).toInt()).array()
        for (b in nextIntBytes) {
            data[i] = b
            i++
        }

        btService.write(data)
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(broadcastReceiver)
        btService.stop()
    }
}
