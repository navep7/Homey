package com.belaku.homey


import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import java.util.Collections
import java.util.Date
import java.util.LinkedList
import java.util.Locale


class NewAppWidget : AppWidgetProvider() {


    val choosenApps: ArrayList<App> = ArrayList()
    lateinit var gpName: String
    lateinit var gpPh: String

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
        sharedPreferences = context.getSharedPreferences("UserPreferences", MODE_PRIVATE)
        sharedPreferencesEditor = sharedPreferences.edit()

        super.onReceive(context, intent)

        appContx = context

        //   Toast.makeText(context, "onR", Toast.LENGTH_SHORT).show()

        val currentHour = Calendar.getInstance()[Calendar.HOUR_OF_DAY]
        var timeOfDay = if (currentHour >= 6 && currentHour < 12) {
            "Morning"
        } else if (currentHour >= 12 && currentHour < 17) {
            "Afternoon"
        } else if (currentHour >= 17 && currentHour < 21) {
            "Evening"
        } else {
            "Night"
        }
        todaysDate()
        greeting(context, remoteViews, timeOfDay)
        appUsageStats(timeOfDay)

        if (SYNC_CLICKED == intent.action) {
            showAppsDialog(context)
        }

        val appWidgetManager = AppWidgetManager.getInstance(context)
        val watchWidget = ComponentName(context, NewAppWidget::class.java)

        appWidgetManager.updateAppWidget(watchWidget, remoteViews)


        if (APP1_CLICKED == intent.action) {
            Log.d("APP1_CLICKED", readApps()[0])
            launchApp(readApps()[0])
        }

        if (APP2_CLICKED == intent.action) {
            Log.d("APP2_CLICKED", readApps()[1])
            launchApp(readApps()[1])
        }

        if (APP3_CLICKED == intent.action) {
            Log.d("APP3_CLICKED", readApps()[2])
            launchApp(readApps()[2])
        }

        if (APP4_CLICKED == intent.action) {
            Log.d("APP4_CLICKED", readApps()[3])
            launchApp(readApps()[3])
        }
    }

    private fun todaysDate() {

        val c: Date = Calendar.getInstance().time
        val df: SimpleDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val formattedDate: String = df.format(c)

        remoteViews.setTextViewText(R.id.date_text_view, formattedDate)
    }

    private fun launchApp(pkgName: String) {
        val launchIntent: Intent = appContx.getPackageManager().getLaunchIntentForPackage(pkgName)!!
        appContx.startActivity(launchIntent)
    }


    private fun appUsageStats(timeOfDay: String) {

        var cDate = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

        val usageStatsManager =
            appContx.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager // Context.USAGE_STATS_SERVICE);


        val beginCal = Calendar.getInstance()
        val endCal = Calendar.getInstance()
        if (timeOfDay.equals("Morning")) {
            beginCal.set(2025, 5, cDate - 1, 9, 0)
            endCal.set(2025, 5, cDate - 1, 12, 0)
        } else if (timeOfDay.equals("Afternoon")) {
            beginCal.set(2025, 5, cDate - 5, 12, 0)
            endCal.set(2025, 5, cDate - 1, 17, 0)
        } else if (timeOfDay.equals("Evening")) {
            beginCal.set(2025, 5, cDate - 1, 17, 0)
            endCal.set(2025, 5, cDate - 1, 21, 0)
        } else if (timeOfDay.equals("Night")) {
            beginCal.set(2025, 5, cDate - 1, 21, 0)
            endCal.set(2025, 5, cDate - 1, 23, 57)
        }

        val queryUsageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            beginCal.timeInMillis,
            endCal.timeInMillis
        )
        println("results for " + beginCal.time.toGMTString() + " - " + endCal.time.toGMTString())
        println("QUS - " + queryUsageStats.size)
        sortAndFindFour(queryUsageStats)


        var appNames = HashSet<String>()
        for (i in 0 until queryUsageStats.size) {
            var appName = getAppNameFromPkg(queryUsageStats.get(i).packageName)
            var appIcon = getAppIconFromPkg(queryUsageStats.get(i).packageName)

            if (queryUsageStats.get(i).totalTimeInForeground > 0)
                if (!appName.contains("Launcher"))
                    if (appNames.add(appName))
                        if (choosenApps.size < 5) {
                            choosenApps.add(
                                App(
                                    appName, appIcon
                                )
                            )
                            addAppInWidget(App(queryUsageStats.get(i).packageName, appIcon))
                        }
        }
        saveApps(Apps)

    }

    private fun saveApps(apps: java.util.ArrayList<App>) {

        val set: MutableSet<String> = HashSet()

        for (i in 0 until apps.size)
            set.add(apps.get(i).name)

        sharedPreferencesEditor.putInt("Status_size", set.size)

        for (i in 0 until set.size) {
            sharedPreferencesEditor.remove("Status_$i")
            sharedPreferencesEditor.putString("Status_$i", apps.get(i).name)
        }
        sharedPreferencesEditor.commit()

    }

    private fun readApps(): ArrayList<String> {
        val apps = ArrayList<String>()

        val size: Int = sharedPreferences.getInt("Status_size", 0)
        for (i in 0 until size) {
            apps.add(sharedPreferences.getString("Status_$i", null).toString())
        }

        return apps
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


    private fun showAppsDialog(context: Context) {

        //   appUsageStats(timeOfDay)
        context.startActivity(
            Intent(
                context,
                AppChooserDialog::class.java
            ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )

    }

    @SuppressLint("Range")
    private fun greeting(context: Context, remoteViews: RemoteViews, timeOfDay: String) {

        var timeOfDay = timeOfDay

        val c: Cursor? = context.getContentResolver()
            .query(ContactsContract.Profile.CONTENT_URI, null, null, null, null)
        c?.moveToFirst()
        gpName = c!!.getString(c.getColumnIndex("display_name"))
        c?.close()

        if (timeOfDay.equals("Morning"))
            timeOfDay = "$timeOfDay, ${gpName.split(" ").get(0)}  \uD83C\uDF3B "
        else if (timeOfDay.equals("Afternoon"))
            timeOfDay = "$timeOfDay, ${gpName.split(" ").get(0)}  ☀\uFE0F "
        else if (timeOfDay.equals("Evening"))
            timeOfDay = "$timeOfDay, ${gpName.split(" ").get(0)}  \uD83C\uDF41 "
        else if (timeOfDay.equals("Night"))
            timeOfDay = "$timeOfDay, ${gpName.split(" ").get(0)}  \uD83D\uDCA4 "


        remoteViews.setTextViewText(R.id.time_text_view, timeOfDay)

    }

    @SuppressLint("Range", "Recycle")
    private fun getGoogleProfileInfo(context: Context): String {

        val manager = AccountManager.get(context)
        val accounts = manager.getAccountsByType("com.google")
        val possibleEmails: MutableList<String?> = LinkedList()

        for (account in accounts) {
            // TODO: Check possibleEmail against an email regex or treat
            // account.name as an email address only for certain account.type
            // values.
            possibleEmails.add(account.name)
        }

        if (!possibleEmails.isEmpty() && possibleEmails[0] != null) {
            val email = possibleEmails[0]
            val parts: Array<String?> = email!!.split("@".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            return if (parts.size > 0 && parts[0] != null) parts[1].toString()
            else "null1"
        } else return "null2"
    }

    protected fun getPendingSelfIntent(context: Context?, action: String?): PendingIntent {
        val intent = Intent(context, javaClass)
        intent.setAction(action)
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    companion object {
        private var Apps: ArrayList<App> = ArrayList()
        private lateinit var sharedPreferencesEditor: SharedPreferences.Editor
        private lateinit var sharedPreferences: SharedPreferences

        fun addAppInWidget(app: App) {

            Apps.add(app)

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

        private fun saveApp(app: App) {

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