package com.belaku.homey

import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.icu.util.Calendar
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.NonNull
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.belaku.homey.MainActivity.Companion
import com.belaku.homey.MainActivity.Companion.appContx
import com.belaku.homey.MainActivity.Companion.imgDescs
import com.belaku.homey.MainActivity.Companion.makeToast
import com.belaku.homey.MainActivity.Companion.queryType
import com.belaku.homey.MainActivity.Companion.randomNumber
import com.belaku.homey.MainActivity.Companion.updateInterval
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
    val TAG: String = "SetWallWorker7"

    @NonNull
    override fun doWork(): Result {

        Log.d(TAG, "doWork!")
        sharedPreferences = applicationContext.getSharedPreferences("UserPreferences", MODE_PRIVATE)
        sharedPreferencesEditor = sharedPreferences.edit()

        urls = ArrayList(sharedPreferences.getStringSet("walls", null)!!)

        urls.sort()

        appContx = applicationContext
        wm = WallpaperManager.getInstance(appContx)

        if (updateInterval != null)
        if (updateInterval == "min")
            delayInterval = 60000
        else if (updateInterval == "hour")
            delayInterval = 300000
        else if (updateInterval == "day")
            delayInterval = 600000

        setWall()

        for (i in 1..100){
            try {
                Log.d(TAG, "Let me sleep a moment... $delayInterval")
                Thread.sleep((delayInterval))
                setWall()
            } catch (e: InterruptedException) {
                Log.d(TAG, "Thread sleep failed...")
                e.printStackTrace()
            }
        }
        return Result.success()
    }



    companion object {
        var urls: ArrayList<String> = ArrayList()
        lateinit var wm: WallpaperManager

        fun setWall() {

            val TAG: String = "SetWallWorker7"

            try {

                urls = ArrayList(sharedPreferences.getStringSet("walls", null)!!)
                urls.sort()

                randomNumber = Random.Default.nextInt(urls.size)

                try {
                    val inputStream = URL(urls[randomNumber].substring(4, urls[randomNumber].length)).openStream()
                    wm.setStream(inputStream)
                    Log.d(TAG, "Set successfully")
                    remoteViews?.setViewVisibility(R.id.progressBar_cyclic, View.INVISIBLE)
                    remoteViews?.setViewVisibility(R.id.imgbtn_set, View.VISIBLE)
                    /*var c = Calendar.getInstance()
                    remoteViews?.setTextViewText(R.id.tx_walltype, MainActivity.queryType + " : " + updateInterval + " - " + c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND))
                    newAppWidget = ComponentName(appContx, NewAppWidget::class.java)
                    AppWidgetManager.getInstance(appContx).updateAppWidget(newAppWidget, remoteViews)*/
                } catch (ex: Exception) {
                    Log.d(TAG, "$ex - EX!")
                    //    remoteViews.setTextViewText(R.id.tx_desc, ex.message.toString())
                }

            } catch (e: IOException) {
                Log.d(TAG,  e.toString())
                /*remoteViews?.setTextViewText(R.id.tx_timwstamp, "EXPfu")
                AppWidgetManager.getInstance(appContx).updateAppWidget(newAppWidget, remoteViews)*/
                // Handle exceptions (e.g., network errors, file access issues)
            }
        }
    }
}


