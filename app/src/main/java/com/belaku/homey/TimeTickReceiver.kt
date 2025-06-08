package com.belaku.homey

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

// AirplaneModeChangeReceiver class extending BroadcastReceiver class
class TimeTickReceiver : BroadcastReceiver() {

    // this function will be executed when the user changes his
    // airplane mode
    override fun onReceive(context: Context?, intent: Intent?) {
        MainActivity.notifyW()
    }
}