package com.belaku.homey

import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.icu.util.Calendar
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity.SENSOR_SERVICE
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.belaku.homey.MainActivity.Companion
import com.belaku.homey.MainActivity.Companion.appContx
import com.belaku.homey.MainActivity.Companion.makeToast
import com.belaku.homey.MainActivity.Companion.queryType
import com.belaku.homey.MainActivity.Companion.randomNumber
import com.belaku.homey.MainActivity.Companion.sharedPreferences
import com.belaku.homey.MainActivity.Companion.sharedPreferencesEditor
import com.belaku.homey.MainActivity.Companion.updateTime
import com.belaku.homey.NewAppWidget.Companion.newAppWidget
import com.belaku.homey.NewAppWidget.Companion.remoteViews
import com.belaku.homey.NewAppWidget.Companion.screenHeight
import com.belaku.homey.NewAppWidget.Companion.screenWidth
import java.io.IOException
import java.net.URL
import kotlin.properties.Delegates
import kotlin.random.Random


class SetWallWorker(context: Context?, workerParams: WorkerParameters?) :
    Worker(context!!, workerParams!!), SensorEventListener {


    @NonNull
    override fun doWork(): Result {

        Log.d(TAG, "doWork!")
        sharedPreferences = applicationContext.getSharedPreferences("UserPreferences", MODE_PRIVATE)
        sharedPreferencesEditor = sharedPreferences.edit()

        urls = ArrayList(sharedPreferences.getStringSet("walls", null)!!)
        urls.sort()

        appContx = applicationContext
        wm = WallpaperManager.getInstance(appContx)


        var c = Calendar.getInstance()


        setWall()



            val sensorManager = applicationContext.getSystemService(SENSOR_SERVICE) as SensorManager
            val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);

        /*var mp = MediaPlayer.create(applicationContext, R.raw.click)
        mp.start()
        Handler(Looper.getMainLooper()).postDelayed(Runnable { mp.release() }, 3000)
*/
        Log.d(
            "6J25", "" + c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(
                Calendar.SECOND
            )
        )


        return Result.success()
    }


    companion object {
        var steps: Int = 0
        val TAG: String = "SetWallWorker LOG7"
        var wallDesc: String = ""
        var wallDescs: ArrayList<String> = ArrayList()
        var urls: ArrayList<String> = ArrayList()
        lateinit var wm: WallpaperManager

        fun setWall() {

            wm = WallpaperManager.getInstance(appContx)

            try {

                urls = ArrayList(sharedPreferences.getStringSet("walls", null)!!)
                urls.sort()
                wallDescs = ArrayList(sharedPreferences.getStringSet("wallDescs", null)!!)
                wallDescs.sort()


                randomNumber = Random.Default.nextInt(urls.size)
                wallDesc = wallDescs.get(randomNumber)


                try {
                    val inputStream =
                        URL(urls[randomNumber].substring(4, urls[randomNumber].length)).openStream()
                    wm.setStream(inputStream)
                    var c = Calendar.getInstance()
                    updateTime =
                        "" + c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(
                            Calendar.SECOND
                        )
                    sharedPreferencesEditor.putString("wD", wallDesc.split("+")[1]).apply()
                    sharedPreferencesEditor.putString("uT", updateTime).apply()
                    Log.d(TAG, "Set successfully")
                    remoteViews?.setViewVisibility(R.id.progressBar_cyclic, View.INVISIBLE)
                    remoteViews?.setViewVisibility(R.id.imgbtn_set, View.VISIBLE)
                    newAppWidget = ComponentName(appContx, NewAppWidget::class.java)
                    AppWidgetManager.getInstance(appContx).updateAppWidget(newAppWidget, remoteViews)

                    val intent = Intent(appContx, NewAppWidget::class.java)
                    intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, newAppWidget)
                    appContx.sendBroadcast(intent)

                } catch (ex: Exception) {
                    Log.d(TAG, "setWallEx1 - $ex")
                }

            } catch (e: IOException) {
                Log.d(TAG, "setWallEx2 - $e")
            }
        }
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        steps++
        Log.d("onSensorChanged", steps.toString())
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        Log.d("onAccuracyChanged", "h3r3")
    }


}


