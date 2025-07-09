package com.belaku.homey

import android.Manifest

import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.app.usage.UsageStats
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.belaku.homey.MainActivity.Companion.appContx
import com.belaku.homey.MainActivity.Companion.favContacts
import com.belaku.homey.MainActivity.Companion.makeToast
import com.belaku.homey.MainActivity.Companion.sharedPreferences
import com.belaku.homey.MainActivity.Companion.sharedPreferencesEditor
import java.util.Collections
import java.util.Date
import java.util.LinkedList
import java.util.Locale
import kotlin.properties.Delegates


class NewAppWidget : AppWidgetProvider() {


    private val TAG: String = "NewAppWidget LOG7"
    private lateinit var wD: String
    private lateinit var qT: String
    private lateinit var uT: String
    private lateinit var dU: String

    private lateinit var mp: MediaPlayer


    private var currentHour by Delegates.notNull<Int>()
    private var currentMin by Delegates.notNull<Int>()
    val choosenApps: ArrayList<App> = ArrayList()
    lateinit var gpName: String


    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        appContx = context!!
        onEn = true
        Log.d("onEnabled! - ", favContacts.size.toString())

        for (i in favContacts)
        addContactInWidget(context, i.name, i.number, i.image)

    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
        appContx = context!!
        makeToast("onDisabled!")
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {

        Log.d(TAG, "onUpdate")


        for (appWidgetId in appWidgetIds) {
            remoteViews = RemoteViews(context.packageName, R.layout.new_app_widget)
            newAppWidget = ComponentName(context, NewAppWidget::class.java)


            remoteViews?.setOnClickPendingIntent(
                R.id.imgbtn_lock,
                getPendingSelfIntent(context, LOCK_PHONE)
            )

            remoteViews?.setOnClickPendingIntent(
                R.id.imgbtn_set,
                getPendingSelfIntent(context, WALL_CHANGE)
            )

            remoteViews?.setOnClickPendingIntent(
                R.id.imgbtn_note,
                getPendingSelfIntent(context, SET_CLICKED)
            )

            remoteViews?.setOnClickPendingIntent(
                R.id.imgv_add1,
                getPendingSelfIntent(context, APP1_CLICKED)
            )

            remoteViews?.setOnClickPendingIntent(
                R.id.imgv_add2,
                getPendingSelfIntent(context, APP2_CLICKED)
            )

            remoteViews?.setOnClickPendingIntent(
                R.id.imgv_add3,
                getPendingSelfIntent(context, APP3_CLICKED)
            )

            remoteViews?.setOnClickPendingIntent(
                R.id.imgv_add4,
                getPendingSelfIntent(context, APP4_CLICKED)
            )

            remoteViews?.setOnClickPendingIntent(
                R.id.imgv_add5,
                getPendingSelfIntent(context, APP5_CLICKED)
            )

            remoteViews?.setOnClickPendingIntent(
                R.id.imgv_contact1,
                getPendingSelfIntent(context, C1_CLICKED)
            )

            remoteViews?.setOnClickPendingIntent(
                R.id.imgv_contact2,
                getPendingSelfIntent(context, C2_CLICKED)
            )

            remoteViews?.setOnClickPendingIntent(
                R.id.imgv_contact3,
                getPendingSelfIntent(context, C3_CLICKED)
            )

            remoteViews?.setOnClickPendingIntent(
                R.id.imgv_contact4,
                getPendingSelfIntent(context, C4_CLICKED)
            )

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }

        appWidgetManager.updateAppWidget(newAppWidget, remoteViews)

    }


    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onReceive(context: Context, intent: Intent) {
        // TODO Auto-generated method stub

        super.onReceive(context, intent)
        remoteViews = RemoteViews(context.packageName, R.layout.new_app_widget)

        Log.d(TAG, "onReceive ${intent.action}")

        sharedPreferences = context.getSharedPreferences("UserPreferences", MODE_PRIVATE)
        sharedPreferencesEditor = sharedPreferences.edit()

        wD = sharedPreferences.getString("wD", "").toString()
        qT = sharedPreferences.getString("qT", "").toString()
        dU = sharedPreferences.getString("dU", "").toString()
        uT = sharedPreferences.getString("uT", "").toString()
        try {
            remoteViews?.setTextViewText(R.id.tx_desc, wD)
            remoteViews?.setTextViewText(R.id.tx_timestamp, uT)
            remoteViews?.setTextViewText(
                R.id.tx_walltype,
                qT.substring(0, 1).uppercase() + qT.substring(1) + " ~ " + dU
            )
        } catch (ex: Exception) {

        }

        appIndex = 0


        currentHour = Calendar.getInstance()[Calendar.HOUR_OF_DAY]
        currentMin = Calendar.getInstance()[Calendar.MINUTE]


        remoteViews?.setOnClickPendingIntent(
            R.id.imgbtn_lock,
            getPendingSelfIntent(context, LOCK_PHONE)
        )

        remoteViews?.setOnClickPendingIntent(
            R.id.imgbtn_set,
            getPendingSelfIntent(context, WALL_CHANGE)
        )

        remoteViews?.setOnClickPendingIntent(
            R.id.imgbtn_note,
            getPendingSelfIntent(context, SET_CLICKED)
        )

        remoteViews?.setOnClickPendingIntent(
            R.id.imgv_add1,
            getPendingSelfIntent(context, APP1_CLICKED)
        )

        remoteViews?.setOnClickPendingIntent(
            R.id.imgv_add2,
            getPendingSelfIntent(context, APP2_CLICKED)
        )

        remoteViews?.setOnClickPendingIntent(
            R.id.imgv_add3,
            getPendingSelfIntent(context, APP3_CLICKED)
        )

        remoteViews?.setOnClickPendingIntent(
            R.id.imgv_add4,
            getPendingSelfIntent(context, APP4_CLICKED)
        )

        remoteViews?.setOnClickPendingIntent(
            R.id.imgv_add5,
            getPendingSelfIntent(context, APP5_CLICKED)
        )

        remoteViews?.setOnClickPendingIntent(
            R.id.imgv_contact1,
            getPendingSelfIntent(context, C1_CLICKED)
        )

        remoteViews?.setOnClickPendingIntent(
            R.id.imgv_contact2,
            getPendingSelfIntent(context, C2_CLICKED)
        )

        remoteViews?.setOnClickPendingIntent(
            R.id.imgv_contact3,
            getPendingSelfIntent(context, C3_CLICKED)
        )

        remoteViews?.setOnClickPendingIntent(
            R.id.imgv_contact4,
            getPendingSelfIntent(context, C4_CLICKED)
        )

        var timeOfDay = if (currentHour >= 6 && currentHour < 12) {
            "Morning"
        } else if (currentHour >= 12 && currentHour < 17) {
            "Afternoon"
        } else if (currentHour >= 17 && currentHour < 21) {
            "Evening"
        } else {
            "Night"
        }

        //     makeToast("onReceive!")

        todaysDate(context)


        if (choosenApps.size == 0) {
            //   appUsageStats(context, timeOfDay)
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED
            ) {
                greeting(context, remoteViews!!, timeOfDay)
                //   if (favContacts.size == 0)
                //     getFavoriteContacts(context)
            }
        }

        var apps = readApps()


        if (LOCK_PHONE == intent.action) {

            var deviceManger =
                context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            var compName = ComponentName(context, DeviceAdmin::class.java)
            val active: Boolean = deviceManger.isAdminActive(compName)

            if (active)
                deviceManger.lockNow()
        }

        if (SET_CLICKED == intent.action) {
            val launchIntent: Intent =
                context.packageManager.getLaunchIntentForPackage("com.belaku.homey")!!
            context.startActivity(launchIntent)
        }

        if (WALL_CHANGE == intent.action) {

            remoteViews?.setViewVisibility(R.id.progressBar_cyclic, View.VISIBLE)
            remoteViews?.setViewVisibility(R.id.imgbtn_set, View.INVISIBLE)
            appContx = context
            Thread {
                SetWallWorker.setWall()
            }.start()


        }

        if (APP1_CLICKED == intent.action) {
            var app = apps[0]
            Log.d("APP1_CLICKED", app)
            launchApp(context, app)
        }

        if (APP2_CLICKED == intent.action) {
            var app = apps[1]
            Log.d("APP2_CLICKED", app)
            launchApp(context, app)
        }

        if (APP3_CLICKED == intent.action) {
            var app = apps[2]
            Log.d("APP3_CLICKED", app)
            launchApp(context, app)
        }

        if (APP4_CLICKED == intent.action) {
            var app = apps[3]
            Log.d("APP4_CLICKED", app)
            launchApp(context, app)
        }

        if (APP5_CLICKED == intent.action) {
            var app = apps[4]
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
        AppWidgetManager.getInstance(context).updateAppWidget(newAppWidget, remoteViews)

    }


    private fun clickSound(context: Context) {

        mp = MediaPlayer.create(context, R.raw.click)
        mp.start()
        Handler(Looper.getMainLooper()).postDelayed(Runnable { mp.release() }, 3000)

    }


    fun dialPhoneNumber(context: Context, phoneNumber: String) {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:" + phoneNumber)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent)

    }


    private fun todaysDate(context: Context) {

        val c: Date = Calendar.getInstance().time
        val df = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val formattedDate: String = df.format(c)
        remoteViews?.setTextViewText(R.id.tx_date, formattedDate)
        remoteViews?.setTextViewText(
            R.id.tx_day,
            SimpleDateFormat("EEEE", Locale.getDefault()).format(c)
        )
    }

    private fun launchApp(context: Context, pkgName: String) {
        val launchIntent: Intent = context.packageManager.getLaunchIntentForPackage(pkgName)!!
        context.startActivity(launchIntent)
    }


    private fun readApps(): ArrayList<String> {
        val apps = ArrayList<String>()

        val size: Int = sharedPreferences.getInt("Status_size", 0)
        for (i in 0 until size) {
            apps.add(sharedPreferences.getString("Status_$i", null).toString())
        }

        return apps
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
        var onEn: Boolean = false
        var remoteViews: RemoteViews? = null
        var Apps: ArrayList<App> = ArrayList()


        fun addContactInWidget(context: Context, strN: String, strNu: String, cDrawable: String) {

            makeToast("addContactInWidget!")
            newAppWidget = ComponentName(context, NewAppWidget::class.java)
            remoteViews = RemoteViews(context.packageName, R.layout.new_app_widget)

            val input =
                ContactsContract.Contacts.openContactPhotoInputStream(
                    context.contentResolver,
                    Uri.parse(cDrawable)
                )
            val bm = BitmapFactory.decodeStream(input)
            val d: Drawable = BitmapDrawable(bm)


            if (conIndex == 0) {
                remoteViews!!.setImageViewBitmap(R.id.imgv_contact1, drawableToBitmap(context, d).getCircledBitmap())
                remoteViews!!.setTextViewText(R.id.tx_c1, strN)
                conIndex = 1
            } else if (conIndex == 1) {
                remoteViews!!.setImageViewBitmap(R.id.imgv_contact2, drawableToBitmap(context, d).getCircledBitmap())
                remoteViews!!.setTextViewText(R.id.tx_c2, strN)
                conIndex = 2
            } else if (conIndex == 2) {
                remoteViews!!.setImageViewBitmap(R.id.imgv_contact3, drawableToBitmap(context, d).getCircledBitmap())
                remoteViews!!.setTextViewText(R.id.tx_c3, strN)
                conIndex = 3
            } else if (conIndex == 3) {
                remoteViews!!.setImageViewBitmap(R.id.imgv_contact4, drawableToBitmap(context, d).getCircledBitmap())
                remoteViews!!.setTextViewText(R.id.tx_c4, strN)
                conIndex = 4
            }


            AppWidgetManager.getInstance(context).updateAppWidget(newAppWidget, remoteViews)
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

            Apps.add(app)

            if (appIndex == 0) {
                remoteViews?.setImageViewBitmap(
                    R.id.imgv_add1,
                    app.image?.let { drawableToBitmap(context, it) })
                appIndex = 1
                remoteViews?.setViewVisibility(R.id.imgv_add1, View.VISIBLE)
            } else if (appIndex == 1) {
                remoteViews?.setImageViewBitmap(
                    R.id.imgv_add2,
                    app.image?.let { drawableToBitmap(context, it) })
                appIndex = 2
                remoteViews?.setViewVisibility(R.id.imgv_add2, View.VISIBLE)
            } else if (appIndex == 2) {
                remoteViews?.setImageViewBitmap(
                    R.id.imgv_add3,
                    app.image?.let { drawableToBitmap(context, it) })
                appIndex = 3
                remoteViews?.setViewVisibility(R.id.imgv_add3, View.VISIBLE)
            } else if (appIndex == 3) {
                remoteViews?.setImageViewBitmap(
                    R.id.imgv_add4,
                    app.image?.let { drawableToBitmap(context, it) })
                appIndex = 4
                remoteViews?.setViewVisibility(R.id.imgv_add4, View.VISIBLE)
            } else if (appIndex == 4) {
                remoteViews?.setImageViewBitmap(
                    R.id.imgv_add5,
                    app.image?.let { drawableToBitmap(context, it) })
                appIndex = 4
                remoteViews?.setViewVisibility(R.id.imgv_add5, View.VISIBLE)
            }


        }


        private fun drawableToBitmap(context: Context, drawable: Drawable): Bitmap {

            if (drawable is BitmapDrawable) {
                if (drawable.bitmap != null) {
                    return drawable.bitmap
                } else return drawableToBitmap(
                    context,
                    AppCompatResources.getDrawable(context, R.drawable.face_holder)!!
                )
            }

            val bitmap: Bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
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

        lateinit var newAppWidget: ComponentName

        private const val LOCK_PHONE = "lockPhone"
        private const val WALL_CHANGE = "wallChange"
        private const val SET_CLICKED = "setButtonClick"
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