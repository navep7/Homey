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
import android.util.Log
import android.view.View
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity.SENSOR_SERVICE
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.belaku.homey.MainActivity.Companion.appContx
import com.belaku.homey.MainActivity.Companion.makeToast
import com.belaku.homey.MainActivity.Companion.randomNumber
import com.belaku.homey.MainActivity.Companion.sharedPreferences
import com.belaku.homey.MainActivity.Companion.sharedPreferencesEditor
import com.belaku.homey.MainActivity.Companion.updateTime
import com.belaku.homey.NewAppWidget.Companion.newAppWidget
import com.belaku.homey.NewAppWidget.Companion.remoteViews
import java.io.IOException
import java.net.URL
import java.util.concurrent.TimeUnit
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
        setWall()
     //   initSteps()

        return Result.success()
    }


    companion object {

        lateinit var stepCounterSensor: Sensor
        lateinit var sensorManager: SensorManager
        val mSensorEventListener: SensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                Log.d("onSensorChanged",  steps.toString())
                if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
                    steps++;
                }

            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                Log.d("MY_APP", "$sensor - $accuracy")
            }
        }


        var steps by Delegates.notNull<Int>()
        val TAG: String = "SetWallWorker LOG7"
        var wallDesc: String = ""
        var wallDescs: ArrayList<String> = ArrayList()
        var urls: ArrayList<String> = ArrayList()
        lateinit var wm: WallpaperManager



        fun initSteps() {
            steps = 0
            sensorManager = appContx.getSystemService(SENSOR_SERVICE) as SensorManager
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)!!
            if(sensorManager.registerListener(mSensorEventListener, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL))
                Log.d(TAG, "StepS Initd")
        }
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
                    AppWidgetManager.getInstance(appContx)
                        .updateAppWidget(newAppWidget, remoteViews)

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


