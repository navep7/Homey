package com.belaku.homey



import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.icu.util.Calendar
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.RemoteViews


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

            Log.d("ADDA", "S")
            showAppsDialog(context)

            appWidgetManager.updateAppWidget(watchWidget, remoteViews)
        }
    }

    private fun showAppsDialog(context: Context) {

        context.startActivity(Intent(context, AppChooserDialog::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))

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
    }
}