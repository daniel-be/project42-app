package com.example.android.marvincontrolpanel

import android.os.Handler
import android.os.Message

class BtServiceHandler(private val mainActivity: MainActivity) : Handler() {
    override fun handleMessage(msg: Message?) {
        mainActivity.handleMessage(msg)
    }
}