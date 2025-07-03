package com.belaku.homey

import android.app.Service
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.IBinder
import android.util.Log
import java.io.IOException
import java.util.Timer
import java.util.TimerTask
import com.belaku.homey.MainActivity.Companion.makeToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL
import java.util.Random
import com.belaku.homey.NewAppWidget.Companion.remoteViews

/*

import android.app.Service
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.annotation.Nullable
import com.belaku.homey.MainActivity.Companion.makeToast
import com.belaku.homey.NewAppWidget.Companion.remoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL
import java.util.Random
import java.util.Timer
import java.util.TimerTask


class WallService : Service() {
    private lateinit var newAppWidget: ComponentName
    private lateinit var wallURLs: ArrayList<String>
    private lateinit var wallDESCs: ArrayList<String>
    private var timer: Timer? = null
    private var currentImageIndex = 0


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service Started")
        newAppWidget = ComponentName(applicationContext, NewAppWidget::class.java)
        wallURLs = intent.getStringArrayListExtra("URLs")!!
        wallDESCs = intent.getStringArrayListExtra("DESCs")!!
        startWallpaperTimer()
        return START_STICKY
    }

    private fun startWallpaperTimer() {
        timer = Timer()
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                changeWallpaper()
            }
        }, 0, (5 * 60 * 1000).toLong()) // 5 minutes in milliseconds
    }

    private fun changeWallpaper() {
        val wallpaperManager = WallpaperManager.getInstance(this)
        Log.d("changeWallpaper", "CHG")

        var rn = Random().nextInt(wallURLs.size)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputStream = URL(wallURLs.get(rn)).openStream()
                remoteViews.setTextViewText(R.id.tx_desc, wallDESCs.get(rn))
                wallpaperManager.setStream(inputStream)
            } catch (ex: Exception) {
                makeToast(ex.message.toString() + " - EX!")
                //    remoteViews.setTextViewText(R.id.tx_desc, ex.message.toString())
            }
        }

        AppWidgetManager.getInstance(applicationContext).updateAppWidget(newAppWidget, remoteViews)
    }


    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service Destroyed")
        if (timer != null) {
            timer!!.cancel()
        }
    }

    companion object {
        private const val TAG = "WallpaperService"
    }
}*/



class WallService : Service() {
    private lateinit var newAppWidget: ComponentName
    private val TAG: String = "WallService"
    private lateinit var wallURLs: ArrayList<String>
    private lateinit var wallDESCs: ArrayList<String>
    private var timer: Timer? = null
    private val wallpaperResources = intArrayOf(
        R.drawable.calls,
        R.drawable.msgs,
        R.drawable.reload
    ) // Replace with your wallpaper resource IDs
    private var currentWallpaperIndex = 0
    private var wallpaperManager: WallpaperManager? = null

    override fun onCreate() {
        super.onCreate()
        wallpaperManager = WallpaperManager.getInstance(this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        Log.d(TAG, "onStartCommand")
        newAppWidget = ComponentName(applicationContext, NewAppWidget::class.java)

        wallURLs = intent.getStringArrayListExtra("URLs")!!
        wallDESCs = intent.getStringArrayListExtra("DESCs")!!
        timer = Timer()
        timer!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                changeWallpaper()
            }
        }, 0, (1 * 15 * 1000).toLong()) // Change every hour (1000ms * 60s * 60m)
        return START_STICKY
    }

    private fun changeWallpaper() {
        Log.d(TAG, "GHC")
        var rn = Random().nextInt(wallURLs.size)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputStream = URL(wallURLs.get(rn)).openStream()
                WallpaperManager.getInstance(applicationContext).setStream(inputStream)
            } catch (ex: Exception) {
                Log.d(TAG, ex.message.toString() + " - EX!")
                //    remoteViews.setTextViewText(R.id.tx_desc, ex.message.toString())
            }
        }
        remoteViews?.setTextViewText(R.id.tx_desc, wallDESCs.get(rn))
        AppWidgetManager.getInstance(applicationContext).updateAppWidget(newAppWidget, remoteViews)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        if (timer != null) {
            timer!!.cancel()
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
