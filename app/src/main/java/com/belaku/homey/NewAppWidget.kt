package com.belaku.homey

import android.Manifest
import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.WallpaperManager
import android.app.admin.DevicePolicyManager
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
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.belaku.homey.MainActivity.Companion.makeToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL
import java.util.Collections
import java.util.Date
import java.util.LinkedList
import java.util.Locale
import java.util.Random
import kotlin.properties.Delegates


class NewAppWidget : AppWidgetProvider() {


    private var walls: HashSet<String> = HashSet()
    private var wallDescs: HashSet<String> = HashSet()

    private lateinit var mp: MediaPlayer

    private lateinit var newAppWidget: ComponentName
    private lateinit var appWM: AppWidgetManager
    private lateinit var wallType: String
    var wallTypes: List<String> = mutableListOf(
        "Beautiful",
        "Trending",
        "festival",
        "Sunset",
        "Beach",
        "Rain",
        "Diwali",
        "Street",
        "Cityscapes"
    )

    private var currentHour by Delegates.notNull<Int>()
    private var currentMin by Delegates.notNull<Int>()
    val choosenApps: ArrayList<App> = ArrayList()
    var favContacts: ArrayList<Contact> = ArrayList()
    lateinit var gpName: String


    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {


        appWM = appWidgetManager

        for (appWidgId in appWidgetIds) {
            remoteViews = RemoteViews(context.packageName, R.layout.new_app_widget)
            newAppWidget = ComponentName(context, NewAppWidget::class.java)

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

            remoteViews.setOnClickPendingIntent(
                R.id.imgv_add5,
                getPendingSelfIntent(context, APP5_CLICKED)
            )

            remoteViews.setOnClickPendingIntent(
                R.id.imgv_contact1,
                getPendingSelfIntent(context, C1_CLICKED)
            )

            remoteViews.setOnClickPendingIntent(
                R.id.imgv_contact2,
                getPendingSelfIntent(context, C2_CLICKED)
            )

            remoteViews.setOnClickPendingIntent(
                R.id.imgv_contact3,
                getPendingSelfIntent(context, C3_CLICKED)
            )

            remoteViews.setOnClickPendingIntent(
                R.id.imgv_contact4,
                getPendingSelfIntent(context, C4_CLICKED)
            )



            appWidgetManager.updateAppWidget(appWidgId, remoteViews)
        }

        appWidgetManager.updateAppWidget(newAppWidget, remoteViews)

    }


    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onReceive(context: Context, intent: Intent) {
        // TODO Auto-generated method stub

        super.onReceive(context, intent)

        sharedPreferences = context.getSharedPreferences("UserPreferences", MODE_PRIVATE)
        sharedPreferencesEditor = sharedPreferences.edit()

        walls = sharedPreferences.getStringSet("walls", walls) as HashSet<String>
        wallDescs = sharedPreferences.getStringSet("wallDescs", wallDescs) as HashSet<String>

        appIndex = 0
        conIndex = 0

        remoteViews = RemoteViews(context.packageName, R.layout.new_app_widget)

        currentHour = Calendar.getInstance()[Calendar.HOUR_OF_DAY]
        currentMin = Calendar.getInstance()[Calendar.MINUTE]

        remoteViews.setTextViewText(R.id.tx_timwstamp, "" + currentHour + ":" + currentMin)

        var timeOfDay = if (currentHour >= 6 && currentHour < 12) {
            "Morning"
        } else if (currentHour >= 12 && currentHour < 17) {
            "Afternoon"
        } else if (currentHour >= 17 && currentHour < 21) {
            "Evening"
        } else {
            "Night"
        }

        makeToast("onReceive")
        appWM = AppWidgetManager.getInstance(context)

        todaysDate(context)
        appUsageStats(context, timeOfDay)


        makeToast("CHKI " + intent.action)

        if (intent.action!!.contains("."))
            setWalls(context)

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            greeting(context, remoteViews, timeOfDay)
            getFavoriteContacts(context)
        }


        /* if (WALL_CHANGE == intent.action)
             setWalls(context)

         if (LOCK_PHONE == intent.action) {

             val deviceManger =
                 context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
             val compName = ComponentName(context, DeviceAdmin::class.java)
             val active: Boolean = deviceManger.isAdminActive(compName)

             if (active)
                 deviceManger.lockNow()
         }*/


        if (APP1_CLICKED == intent.action) {
            var app = readApps()[0]
            Log.d("APP1_CLICKED", app)
            launchApp(context, app)
        }

        if (APP2_CLICKED == intent.action) {
            var app = readApps()[1]
            Log.d("APP2_CLICKED", app)
            launchApp(context, app)
        }

        if (APP3_CLICKED == intent.action) {
            var app = readApps()[2]
            Log.d("APP3_CLICKED", app)
            launchApp(context, app)
        }

        if (APP4_CLICKED == intent.action) {
            var app = readApps()[3]
            Log.d("APP4_CLICKED", app)
            launchApp(context, app)
        }

        if (APP5_CLICKED == intent.action) {
            var app = readApps()[4]
            Log.d("APP5_CLICKED", app)
            launchApp(context, app)
        }

        if (C1_CLICKED == intent.action) {
            dialPhoneNumber(context, favContacts.get(0).number)
        }
        if (C2_CLICKED == intent.action) {
            dialPhoneNumber(context, favContacts.get(1).number)
        }
        if (C3_CLICKED == intent.action) {
            dialPhoneNumber(context, favContacts.get(2).number)
        }
        if (C4_CLICKED == intent.action) {
            dialPhoneNumber(context, favContacts.get(3).number)
        }

        newAppWidget = ComponentName(context, NewAppWidget::class.java)
        appWM.updateAppWidget(newAppWidget, remoteViews)

    }

    private fun setWalls(context: Context) {

        var randomNumber = 0
        makeToast("WALL_CHANGE!")
        clickSound(context)
        if (walls.size > 0 && wallDescs.size > 0) {
            randomNumber = Random().nextInt(walls.size)
            makeToast("" + walls.size + " Rrrrd - " + wallDescs.size)
            setWallpaperFromUrl(context, walls.toMutableList().get(randomNumber))
            remoteViews.setTextViewText(R.id.tx_desc, wallDescs.toMutableList().get(randomNumber))
        }


    }


    private fun clickSound(context: Context) {

        makeToast("clickSound!")
        mp = MediaPlayer.create(context, R.raw.click)
        mp.start()
        Handler(Looper.getMainLooper()).postDelayed(Runnable { mp.release() }, 3000)

    }


    @OptIn(DelicateCoroutinesApi::class)
    fun setWallpaperFromUrl(context: Context, imageUrl: String) {
        makeToast("setWallpaperFromUrl!")


        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputStream = URL(imageUrl).openStream()
                WallpaperManager.getInstance(context).setStream(inputStream)
            } catch (ex: Exception) {
                makeToast(ex.message.toString() + " - EX!")
                //    remoteViews.setTextViewText(R.id.tx_desc, ex.message.toString())
            }
        }
    }

    fun dialPhoneNumber(context: Context, phoneNumber: String) {
        makeToast("tel:" + phoneNumber)
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:" + phoneNumber)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent)

    }


    @SuppressLint("Range", "UseCompatLoadingForDrawables")
    fun getFavoriteContacts(context: Context) {

        makeToast("MC - getFavoriteContacts")

        favContacts = ArrayList()

        val queryUri = ContactsContract.Contacts.CONTENT_URI.buildUpon()
            .appendQueryParameter(ContactsContract.Contacts.EXTRA_ADDRESS_BOOK_INDEX, "true")
            .build()

        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.STARRED,
            ContactsContract.Contacts.HAS_PHONE_NUMBER
        )

        val selection = ContactsContract.Contacts.STARRED + "='1'"

        val cursor = context.contentResolver.query(
            queryUri,
            projection, selection, null, null
        )

        while (cursor!!.moveToNext()) {
            val contactID = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
            var phoneNumber: String = "7"

            if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {

                val phones: Cursor? = context.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactID,
                    null,
                    null
                )
                while (phones!!.moveToNext()) {
                    phoneNumber =
                        phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    phoneNumber = phoneNumber.filter { !it.isWhitespace() }
                }


            } else makeToast("N")

            val intent = Intent(Intent.ACTION_VIEW)
            val uri = Uri.withAppendedPath(
                ContactsContract.Contacts.CONTENT_URI, contactID.toString()
            )
            intent.data = uri
            val cPhUri = intent.toUri(0)

            val cNme = cursor.getString(
                cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            )

            var c = Contact(cNme, phoneNumber, cPhUri)

            favContacts.add(c)
        }

        cursor.close()




        for (i in 0 until favContacts.size) {

            Log.d(
                "cLog",
                "cName: ${favContacts.get(i).name}, cPic: ${favContacts.get(i).image}, cNum: ${
                    favContacts.get(i).number
                } "
            )

            val input =
                ContactsContract.Contacts.openContactPhotoInputStream(
                    context.contentResolver,
                    Uri.parse(favContacts.get(i).image)
                )
            val bm = BitmapFactory.decodeStream(input)
            val d: Drawable = BitmapDrawable(bm)

            addContactInWidget(context, favContacts.get(i).name, favContacts.get(i).number, d)
        }

    }


    private fun todaysDate(context: Context) {

        val c: Date = Calendar.getInstance().time
        val df: SimpleDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val formattedDate: String = df.format(c)
        remoteViews.setTextViewText(R.id.date_text_view, formattedDate)
    }

    private fun launchApp(context: Context, pkgName: String) {
        val launchIntent: Intent = context.getPackageManager().getLaunchIntentForPackage(pkgName)!!
        context.startActivity(launchIntent)
    }


    private fun appUsageStats(context: Context, timeOfDay: String) {

        choosenApps.clear()

        var cDate = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager // Context.USAGE_STATS_SERVICE);


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
            UsageStatsManager.INTERVAL_BEST,
            beginCal.timeInMillis,
            endCal.timeInMillis
        )
        println("results for " + beginCal.time.toGMTString() + " - " + endCal.time.toGMTString())
        println("QUS - " + queryUsageStats.size)
        sortApps(queryUsageStats)


        var appNames = HashSet<String>()
        for (i in 0 until queryUsageStats.size) {

            var appName = getAppNameFromPkg(context, queryUsageStats.get(i).packageName)
            var appIcon = getAppIconFromPkg(context, queryUsageStats.get(i).packageName)

            Log.d(
                "queryUsageStats",
                "$appName ... - $i : " + queryUsageStats.get(i).totalTimeInForeground
            )

            //    if (queryUsageStats.get(i).totalTimeInForeground > 0)
            if (!appName.contains("Launcher"))
                if (context.packageManager.getLaunchIntentForPackage(queryUsageStats[i].packageName) != null)
                    if (appNames.add(appName))
                        if (choosenApps.size < 5) {
                            choosenApps.add(
                                App(
                                    appName, appIcon
                                )
                            )
                            Log.d("cLogSetAppIcon", appIcon.toString())
                            addAppInWidget(
                                context,
                                App(queryUsageStats.get(i).packageName, appIcon)
                            )
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


    private fun getAppIconFromPkg(context: Context, packageName: String?): Drawable? {
        try {
            val icon: Drawable =
                context.getPackageManager().getApplicationIcon(packageName.toString())
            return icon
        } catch (e: NameNotFoundException) {
            e.printStackTrace()
            return context.getDrawable(R.drawable.calls)
        }
    }

    private fun getAppNameFromPkg(context: Context, packageName: String?): String {
        val pm: PackageManager = context.getPackageManager()
        var ai = try {
            pm.getApplicationInfo(packageName.toString(), 0)
        } catch (e: NameNotFoundException) {
            null
        }
        val applicationName =
            (if (ai != null) pm.getApplicationLabel(ai) else "(unknown)") as String

        return applicationName
    }

    private fun sortApps(queryUsageStats: List<UsageStats>) {

        Collections.sort<UsageStats>(
            queryUsageStats
        ) { p1: UsageStats, p2: UsageStats ->
            p2.totalTimeInForeground.compareTo(p1.totalTimeInForeground)
            //   p1.name.compareTo(p2.name)
        }

    }


    private fun showAppsDialog(context: Context) {

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
        lateinit var sharedPreferencesEditor: SharedPreferences.Editor
        lateinit var sharedPreferences: SharedPreferences


        fun addContactInWidget(context: Context, strN: String, strNu: String, drawable: Drawable) {

            //     makeToast("mC - " + "addContactInWidget")
            var nullD: Drawable
            if (drawable == null)
                nullD = context.getDrawable(R.drawable.face_holder)!!
            else nullD = drawable
            if (conIndex == 0) {
                remoteViews.setImageViewBitmap(
                    R.id.imgv_contact1,
                    nullD?.let { drawableToBitmap(context, it).getCircledBitmap() })
                remoteViews.setTextViewText(R.id.tx_c1, strN)
                conIndex = 1
            } else if (conIndex == 1) {
                remoteViews.setImageViewBitmap(
                    R.id.imgv_contact2,
                    nullD?.let { drawableToBitmap(context, it).getCircledBitmap() })
                remoteViews.setTextViewText(R.id.tx_c2, strN)
                conIndex = 2
            } else if (conIndex == 2) {
                remoteViews.setImageViewBitmap(
                    R.id.imgv_contact3,
                    nullD?.let { drawableToBitmap(context, it).getCircledBitmap() })
                remoteViews.setTextViewText(R.id.tx_c3, strN)
                conIndex = 3
            } else if (conIndex == 3) {
                remoteViews.setImageViewBitmap(
                    R.id.imgv_contact4,
                    nullD?.let { drawableToBitmap(context, it).getCircledBitmap() })
                remoteViews.setTextViewText(R.id.tx_c4, strN)
                conIndex = 4
            }
        }

        private fun Bitmap.getCircledBitmap(): Bitmap {
            val output = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            val paint = Paint()
            val rect = Rect(0, 0, this.width, this.height)
            paint.isAntiAlias = true
            canvas.drawARGB(0, 0, 0, 0)
            canvas.drawCircle(this.width / 2f, this.height / 2f, this.width / 2f, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(this, rect, rect, paint)
            return output
        }

        fun addAppInWidget(context: Context, app: App) {

            makeToast("addAppInWidget!")
            Apps.add(app)



            if (appIndex == 0) {
                remoteViews.setImageViewBitmap(
                    R.id.imgv_add1,
                    app.image?.let { drawableToBitmap(context, it) })
                appIndex = 1
                remoteViews.setViewVisibility(R.id.imgv_add1, View.VISIBLE)
            } else if (appIndex == 1) {
                remoteViews.setImageViewBitmap(
                    R.id.imgv_add2,
                    app.image?.let { drawableToBitmap(context, it) })
                appIndex = 2
                remoteViews.setViewVisibility(R.id.imgv_add2, View.VISIBLE)
            } else if (appIndex == 2) {
                remoteViews.setImageViewBitmap(
                    R.id.imgv_add3,
                    app.image?.let { drawableToBitmap(context, it) })
                appIndex = 3
                remoteViews.setViewVisibility(R.id.imgv_add3, View.VISIBLE)
            } else if (appIndex == 3) {
                remoteViews.setImageViewBitmap(
                    R.id.imgv_add4,
                    app.image?.let { drawableToBitmap(context, it) })
                appIndex = 4
                remoteViews.setViewVisibility(R.id.imgv_add4, View.VISIBLE)
            } else if (appIndex == 4) {
                remoteViews.setImageViewBitmap(
                    R.id.imgv_add5,
                    app.image?.let { drawableToBitmap(context, it) })
                appIndex = 4
                remoteViews.setViewVisibility(R.id.imgv_add5, View.VISIBLE)
            }


        }


        fun drawableToBitmap(context: Context, drawable: Drawable): Bitmap {
            var bitmap: Bitmap? = null

            if (drawable is BitmapDrawable) {
                val bitmapDrawable = drawable
                if (bitmapDrawable.bitmap != null) {
                    return bitmapDrawable.bitmap
                } else return drawableToBitmap(
                    context,
                    context.getDrawable(R.drawable.face_holder)!!
                )
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
        private var conIndex: Int = 0
        lateinit var remoteViews: RemoteViews

        private const val LOCK_PHONE = "lockPhone"
        private const val WALL_CHANGE = "wallChange"
        private const val WALLTYPE_CLICKED = "wallType"
        private const val SYNC_CLICKED = "automaticWidgetSyncButtonClick"
        private const val APP1_CLICKED = "App1Clicked"
        private const val APP2_CLICKED = "App2Clicked"
        private const val APP3_CLICKED = "App3Clicked"
        private const val APP4_CLICKED = "App4Clicked"
        private const val APP5_CLICKED = "App5Clicked"

        private const val C1_CLICKED = "C1Clicked"
        private const val C2_CLICKED = "C2Clicked"
        private const val C3_CLICKED = "C3Clicked"
        private const val C4_CLICKED = "C4Clicked"
    }


}