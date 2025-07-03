package com.belaku.homey

import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.icu.util.Calendar
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.NonNull
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.belaku.homey.MainActivity.Companion.makeToast
import com.belaku.homey.NewAppWidget.Companion.newAppWidget
import com.belaku.homey.NewAppWidget.Companion.remoteViews
import com.belaku.homey.NewAppWidget.Companion.sharedPreferences
import com.belaku.homey.NewAppWidget.Companion.sharedPreferencesEditor
import java.io.IOException
import java.net.URL
import kotlin.random.Random


class SetWallWorker(context: Context?, workerParams: WorkerParameters?) :
    Worker(context!!, workerParams!!) {
    private var delayInterval: Long = 10000

    @NonNull
    override fun doWork(): Result {

        sharedPreferences = applicationContext.getSharedPreferences("UserPreferences", MODE_PRIVATE)
        sharedPreferencesEditor = sharedPreferences.edit()

        if (MainActivity.updateInterval == "min")
            delayInterval = 60000
        else if (MainActivity.updateInterval == "hour")
            delayInterval = 3600000
        else if (MainActivity.updateInterval == "day")
            delayInterval = 86400000

        for (i in 1..900){
            try {
                Log.d("PWLOG", "Let me sleep a moment... $delayInterval")
                setWall()
                Thread.sleep((delayInterval))
            } catch (e: InterruptedException) {
                Log.d("PWLOG", "Thread sleep failed...")
                e.printStackTrace()
            }
        }
        return Result.success()
    }

    private fun setWall() {

        try {

            val wm = WallpaperManager.getInstance(applicationContext)
            val urls = ArrayList(sharedPreferences.getStringSet("walls", null)!!)
            MainActivity.randomNumber = Random.Default.nextInt(urls.size)

            try {
                val inputStream = URL(urls[MainActivity.randomNumber]).openStream()
                wm.setStream(inputStream)
            } catch (ex: Exception) {
                makeToast(ex.message.toString() + " - EX!")
                //    remoteViews.setTextViewText(R.id.tx_desc, ex.message.toString())
            }

        } catch (e: IOException) {
            makeToast("doWork ExP - " + e.toString())
            remoteViews?.setTextViewText(R.id.tx_timwstamp, "EXPfu")
            AppWidgetManager.getInstance(applicationContext).updateAppWidget(newAppWidget, remoteViews)
            // Handle exceptions (e.g., network errors, file access issues)
        }
    }
}


