package com.belaku.homey

import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.Context
import android.icu.util.Calendar
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.NonNull
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.belaku.homey.MainActivity.Companion.makeToast
import com.belaku.homey.MainActivity.Companion.updateInterval
import java.io.IOException
import java.net.URL
import kotlin.random.Random


class SetWallWorker(context: Context?, workerParams: WorkerParameters?) :
    Worker(context!!, workerParams!!) {
    private var delayTime: Long = 0

    @NonNull
    override fun doWork(): Result {

        if (updateInterval == "min")
            delayTime = 60000
        else if (updateInterval == "hour")
            delayTime = 3600000
        else if (updateInterval == "day")
            delayTime = 86400000
        for (i in 1..900){
            try {
                Log.d("ThreadSleeping...", "Let me sleep a moment... $delayTime")
                Thread.sleep((delayTime)) //1 minutes cycle
            //    doTheActualProcessingWork()
                setWall()
            } catch (e: InterruptedException) {
                Log.d("ThreadSleeping... EXP - ", "Thread sleep failed...")
                e.printStackTrace()
            }
        }

        return Result.success()
    }

    private fun setWall() {

        try {

            val wm = WallpaperManager.getInstance(applicationContext)
            val urls = ArrayList(MainActivity.sharedPreferences.getStringSet("walls", null)!!)
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
            // Handle exceptions (e.g., network errors, file access issues)
        }
    }
}


