package com.belaku.homey

import android.Manifest
import android.app.AlertDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.belaku.homey.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import java.util.Date


class MainActivity : AppCompatActivity() {

    private val MY_PERMISSIONS_REQUEST_READ_CONTACTS: Int = 1
    private lateinit var sinceDate: Date
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    lateinit var brTimeTick: TimeTickReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        appContx = applicationContext

        BRo()

        checkP()
        val alertDialog: AlertDialog = AlertDialog.Builder(this@MainActivity).create()
        alertDialog.setTitle("Permission Request")
        alertDialog.setMessage("App needs permission to get Usage stats to suggest you apps to use.. Permit ?")
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK"
        ) { dialog, which ->
            val intent1 = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            applicationContext.startActivity(intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            dialog.dismiss()
        }
        alertDialog.show()


  //      NewAppWidget.views.setTextViewText(R.id.appwidget_text, "Qwerty")
    //    NewAppWidget.updateW("1,2,3...")

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()

       //     notifyW()
         //   NewAppWidget.updateW("1,2,3...")
        }

     //   NewAppWidget.
    }

    private fun BRo() {


        brTimeTick = TimeTickReceiver()
        IntentFilter(Intent.ACTION_TIME_TICK).also {
            // registering the receiver
            // it parameter which is passed in  registerReceiver() function
            // is the intent filter that we have just created
            registerReceiver(brTimeTick, it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(brTimeTick)
    }

    private fun checkP() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED) {


            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS),
                MY_PERMISSIONS_REQUEST_READ_CONTACTS
            )


        }
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    companion object {
        private lateinit var appContx: Context

        fun notifyW() {

            try {
                val intent = Intent(
                    appContx,
                    NewAppWidget::class.java)
                intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                val ids: IntArray = AppWidgetManager.getInstance(appContx)
                    .getAppWidgetIds(ComponentName(appContx, NewAppWidget::class.java))
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                appContx.sendBroadcast(intent);
            } catch (e: Exception) {
                // TODO: handle exception
            }

        }
    }
}