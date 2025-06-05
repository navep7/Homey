package com.belaku.homey


import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.icu.util.Calendar
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import com.belaku.homey.AppChooserDialog.Companion.choosenApps
import java.security.AccessController.getContext
import java.util.Collections
import java.util.Date
import java.util.Locale


class NewAppWidget : AppWidgetProvider() {


    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {


        //  val remoteViews = RemoteViews(context.packageName, R.layout.new_app_widget)
        val watchWidget = ComponentName(context, NewAppWidget::class.java)

        for (appWidgetId in appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }

        remoteViews.setOnClickPendingIntent(
            R.id.imgv_add,
            getPendingSelfIntent(context, SYNC_CLICKED)
        )

        remoteViews.setOnClickPendingIntent(
            R.id.imgv_add1,
            getPendingSelfIntent(context, APP1_CLICKED)
        )

        remoteViews.setOnClickPendingIntent(
            R.id.imgv_add2,
            getPendingSelfIntent(context, APP2_CLICKED)
        )

        remoteViews.setOnClickPendingIntent(
            R.id.imgv_add3,
            getPendingSelfIntent(context, APP3_CLICKED)
        )

        remoteViews.setOnClickPendingIntent(
            R.id.imgv_add4,
            getPendingSelfIntent(context, APP4_CLICKED)
        )

        appWidgetManager.updateAppWidget(watchWidget, remoteViews)
    }

    override fun onReceive(context: Context, intent: Intent) {
        // TODO Auto-generated method stub
        remoteViews = RemoteViews(context.packageName, R.layout.new_app_widget)
        super.onReceive(context, intent)

        appContx = context

        //   Toast.makeText(context, "onR", Toast.LENGTH_SHORT).show()

        greeting(context, remoteViews)

        if (SYNC_CLICKED == intent.action) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val watchWidget = ComponentName(context, NewAppWidget::class.java)

                 val intent1: Intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
               appContx.startActivity(intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))

            Log.d("ADDA", "S")
            //    showAppsDialog(context)
            appUsageStats()


            appWidgetManager.updateAppWidget(watchWidget, remoteViews)
        }

        if (APP1_CLICKED == intent.action) {
            //  if (choosenApps.size > 0)
            readApps()
            Log.d("APP1_CLICKED", choosenApps.size.toString())
        }
    }


    private fun appUsageStats() {

        var cDate = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

        val usageStatsManager =
            appContx.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager // Context.USAGE_STATS_SERVICE);
        val beginCal = Calendar.getInstance()
        beginCal.set(2025, 5, cDate - 3)


        val endCal = Calendar.getInstance()
        endCal.set(2025, 5, cDate)

        val queryUsageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            beginCal.timeInMillis,
            endCal.timeInMillis
        )
        println("results for " + beginCal.time.toGMTString() + " - " + endCal.time.toGMTString())
        println("QUS - " + queryUsageStats.size)
        sortAndFindFour(queryUsageStats)


        for (i in 0 until 10) {
            choosenApps.add(
                App(
                    getAppNameFromPkg(queryUsageStats.get(i).packageName),
                    appContx.getDrawable(R.drawable.msgs)
                )
            )
        }

        Log.d("No. ", choosenApps.size.toString())
        for (i in 0 until choosenApps.size) {
            var appName = getAppNameFromPkg(queryUsageStats.get(i).packageName)
            var appIcon = getAppIconFromPkg(queryUsageStats.get(i).packageName)
            Log.d(
                "UsageLog",
                "App $i - $appName : ${
                    queryUsageStats.get(i).totalTimeInForeground
                }"
            )

            if (!appName.toLowerCase(Locale.ROOT).contains("launcher")) {
                addAppInWidget(App(appName, appIcon))
            }
        }

    }

    private fun getAppIconFromPkg(packageName: String?): Drawable? {
        try {
            val icon: Drawable =
                appContx.getPackageManager().getApplicationIcon(packageName.toString())
            return icon
        } catch (e: NameNotFoundException) {
            e.printStackTrace()
            return appContx.getDrawable(R.drawable.calls)
        }
    }

    private fun getAppNameFromPkg(packageName: String?): String {
        val pm: PackageManager = appContx.getPackageManager()
        var ai = try {
            pm.getApplicationInfo(packageName.toString(), 0)
        } catch (e: NameNotFoundException) {
            null
        }
        val applicationName =
            (if (ai != null) pm.getApplicationLabel(ai) else "(unknown)") as String

        return applicationName
    }

    private fun sortAndFindFour(queryUsageStats: List<UsageStats>) {

        Collections.sort<UsageStats>(
            queryUsageStats
        ) { p1: UsageStats, p2: UsageStats ->
            p2.totalTimeInForeground.compareTo(p1.totalTimeInForeground)
            //   p1.name.compareTo(p2.name)
        }

    }

    private fun readApps() {

        val sharedPreferences = appContx.getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val app1 = sharedPreferences.getString("app1", "")
        choosenApps.add(App(app1.toString(), appContx.getDrawable(R.drawable.calls)))

    }

    private fun showAppsDialog(context: Context) {

        appUsageStats()
        context.startActivity(
            Intent(
                context,
                AppChooserDialog::class.java
            ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )

    }

    @SuppressLint("Range")
    private fun greeting(context: Context, remoteViews: RemoteViews) {

        val currentHour = Calendar.getInstance()[Calendar.HOUR_OF_DAY]
        var timeOfDay = if (currentHour >= 0 && currentHour < 12) {
            "Morning"
        } else if (currentHour >= 12 && currentHour < 17) {
            "Afternoon"
        } else if (currentHour >= 17 && currentHour < 21) {
            "Evening"
        } else {
            "Night"
        }


        val projection = arrayOf(ContactsContract.Profile.DISPLAY_NAME)
        var name: String? = null
        val dataUri = Uri.withAppendedPath(
            ContactsContract.Profile.CONTENT_URI,
            ContactsContract.Contacts.Data.CONTENT_DIRECTORY
        )
        val contentResolver: ContentResolver = context.getContentResolver()
        val c = contentResolver.query(dataUri, projection, null, null, null)

        try {
            if (c!!.moveToFirst()) {
                name = c!!.getString(c!!.getColumnIndex(ContactsContract.Profile.DISPLAY_NAME))
            }
        } catch (exc: Exception) {
            name = exc.message
        }
        println(name)

        if (name != null) {
            if (timeOfDay.equals("Morning"))
                timeOfDay = "$timeOfDay, ${name.split(" ").get(0)}  \uD83C\uDF3B "
            else if (timeOfDay.equals("Afternoon"))
                timeOfDay = "$timeOfDay, ${name.split(" ").get(0)}  ☀\uFE0F "
            else if (timeOfDay.equals("Evening"))
                timeOfDay = "$timeOfDay, ${name.split(" ").get(0)}  \uD83C\uDF41 "
            else if (timeOfDay.equals("Night"))
                timeOfDay = "$timeOfDay, ${name.split(" ").get(0)}  \uD83D\uDCA4 "
        }


        remoteViews.setTextViewText(R.id.time_text_view, timeOfDay)

    }

    protected fun getPendingSelfIntent(context: Context?, action: String?): PendingIntent {
        val intent = Intent(context, javaClass)
        intent.setAction(action)
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    companion object {
        fun addAppInWidget(app: App) {

            val appWidgetManager = AppWidgetManager.getInstance(appContx)
            val thisWidget: ComponentName =
                ComponentName(appContx, NewAppWidget::class.java)

            val views = RemoteViews(appContx.packageName, R.layout.new_app_widget)

            if (appIndex == 0) {
                views.setImageViewBitmap(R.id.imgv_add1, app.image?.let { drawableToBitmap(it) })
                appIndex = 1
                views.setViewVisibility(R.id.imgv_add1, View.VISIBLE)
            } else if (appIndex == 1) {
                views.setImageViewBitmap(R.id.imgv_add2, app.image?.let { drawableToBitmap(it) })
                appIndex = 2
                views.setViewVisibility(R.id.imgv_add2, View.VISIBLE)
            } else if (appIndex == 2) {
                views.setImageViewBitmap(R.id.imgv_add3, app.image?.let { drawableToBitmap(it) })
                appIndex = 3
                views.setViewVisibility(R.id.imgv_add3, View.VISIBLE)
            } else if (appIndex == 3) {
                views.setImageViewBitmap(R.id.imgv_add4, app.image?.let { drawableToBitmap(it) })
                appIndex = 4
                views.setViewVisibility(R.id.imgv_add4, View.VISIBLE)
            }

            appWidgetManager.updateAppWidget(thisWidget, views)
        }

        fun drawableToBitmap(drawable: Drawable): Bitmap {
            var bitmap: Bitmap? = null

            if (drawable is BitmapDrawable) {
                val bitmapDrawable = drawable
                if (bitmapDrawable.bitmap != null) {
                    return bitmapDrawable.bitmap
                }
            }

            bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
                Bitmap.createBitmap(
                    1,
                    1,
                    Bitmap.Config.ARGB_8888
                ) // Single color bitmap will be created of 1x1 pixel
            } else {
                Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                )
            }

            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }

        private var appIndex: Int = 0
        private lateinit var appContx: Context
        private lateinit var remoteViews: RemoteViews
        private const val SYNC_CLICKED = "automaticWidgetSyncButtonClick"
        private const val APP1_CLICKED = "App1Clicked"
        private const val APP2_CLICKED = "App2Clicked"
        private const val APP3_CLICKED = "App3Clicked"
        private const val APP4_CLICKED = "App4Clicked"
    }
}