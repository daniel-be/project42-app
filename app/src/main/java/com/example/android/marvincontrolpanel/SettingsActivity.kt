package com.example.android.marvincontrolpanel

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsFragment())
                .addToBackStack(null)
                .commit()
    }
}