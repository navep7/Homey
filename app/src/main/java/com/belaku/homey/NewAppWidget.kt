package com.belaku.homey


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import android.widget.RemoteViews


/*


import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.RemoteViews
import android.widget.Toast
import java.util.Calendar



class NewAppWidget : AppWidgetProvider() {
    private lateinit var views: RemoteViews

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Update the widget for each widget ID
        for (appWidgetId in appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    @SuppressLint("Range")
    override fun onReceive(context: Context, intent: Intent) {

        views = RemoteViews(context.packageName, R.layout.new_app_widget)

        // Handle the button click intent
        Toast.makeText(context, "onReceive : " + intent.getIntExtra("calls", 0), Toast.LENGTH_SHORT).show();

     //   if (intent.getIntExtra("calls", 0) != 0)

        //    if (this::views.isInitialized)
          //  views.setTextViewText(R.id.tx_calls, intent.getIntExtra("calls", 0).toString())

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
        */
/*
        "Morning - \uD83C\uDF3B"
        "Afternoon - ☀\uFE0F"
        "Evening - \uD83C\uDF41"
        "Night - \uD83D\uDCA4"
        *//*





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


        views.setTextViewText(R.id.time_text_view, timeOfDay)

        super.onReceive(context, intent)
    }


}*/


class NewAppWidget : AppWidgetProvider() {
    private lateinit var remoteViews: RemoteViews

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
        private const val SYNC_CLICKED = "automaticWidgetSyncButtonClick"
    }
}