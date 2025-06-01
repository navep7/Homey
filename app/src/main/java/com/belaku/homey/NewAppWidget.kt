package com.belaku.homey

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.RemoteViews
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class NewAppWidget : AppWidgetProvider() {
    private lateinit var views: RemoteViews

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Update the widget for each widget ID
        for (appWidgetId in appWidgetIds) {
         //   views = RemoteViews(context.packageName, R.layout.new_app_widget)

            // Set the text on the TextView
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val currentDate = Date()
            val timeString = sdf.format(currentDate)
         //   views.setTextViewText(R.id.time_text_view, timeString)

            // Set up a PendingIntent for the button click
            val intent = Intent(
                context,
                NewAppWidget::class.java
            )
            intent.setAction(ACTION_BUTTON_CLICKED)
            intent.putExtra(EXTRA_TIME_VALUE, timeString)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
           // views.setOnClickPendingIntent(R.id.button, pendingIntent)

            // Update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    @SuppressLint("Range")
    override fun onReceive(context: Context, intent: Intent) {

        // Handle the button click intent
        Toast.makeText(context, "onReceive : ", Toast.LENGTH_SHORT).show();

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
            if (timeOfDay.equals("Night"))
            timeOfDay = "$timeOfDay, ${name.split(" ").get(0)}  \uD83D\uDCA4 "
        }

        views = RemoteViews(context.packageName, R.layout.new_app_widget)

        views.setTextViewText(R.id.time_text_view, timeOfDay)

        super.onReceive(context, intent)
    }

    companion object {
        private const val ACTION_BUTTON_CLICKED = "com.example.mywidget.BUTTON_CLICKED"
        const val EXTRA_TIME_VALUE: String = "com.example.mywidget.EXTRA_TIME_VALUE"
    }
}