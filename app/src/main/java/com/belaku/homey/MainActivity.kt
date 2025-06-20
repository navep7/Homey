package com.belaku.homey

import android.Manifest
import android.app.AlertDialog
import android.app.AppOpsManager
import android.app.AppOpsManager.MODE_ALLOWED
import android.app.AppOpsManager.OPSTR_GET_USAGE_STATS
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.belaku.homey.databinding.ActivityMainBinding
import com.google.android.material.color.DynamicColors
import com.google.android.material.snackbar.Snackbar
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URL
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

        DynamicColors.applyToActivitiesIfAvailable(application)

        BRo()

        checkP()


        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()
            wallBitmaps = fetchWallpaper()
        }

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
                arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE),
                MY_PERMISSIONS_REQUEST_READ_CONTACTS
            )


        } else  {
            cGranted = true
            UsageStatsPermissionDialog()
        }
    }

    private fun UsageStatsPermissionDialog() {
        val alertDialog: AlertDialog = AlertDialog.Builder(this@MainActivity).create()
        alertDialog.setTitle("Permission Request")
        alertDialog.setMessage("App needs permission to get Usage stats to suggest you apps to use.. Permit ?")
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK"
        ) { dialog, which ->
            val intent1 = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            applicationContext.startActivity(intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            dialog.dismiss()
        }

        if(!getGrantStatus()) {
            alertDialog.show()
        }

    }

    private fun getGrantStatus(): Boolean {
        val appOps = applicationContext.getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            applicationContext.packageName
        )
        return if (mode == AppOpsManager.MODE_DEFAULT) {
            applicationContext.checkCallingOrSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED
        } else {
            mode == MODE_ALLOWED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        cGranted = true
        UsageStatsPermissionDialog()
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
        var wallBitmaps: List<Bitmap> = ArrayList<Bitmap>().toMutableList()
        var cGranted: Boolean = false
        lateinit var appContx: Context

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

        fun makeToast(s: String) {
          //  Toast.makeText(appContx, s, Toast.LENGTH_SHORT).show()
            Log.d("makeToastinG", s)
        }

        fun fetchWallpaper(): List<Bitmap> {
            makeToast("fetchWallpaper")
            val url = "https://api.pexels.com/v1/curated/?page="+1.toString()+"&per_page=80";
            val request: StringRequest = object : StringRequest(

                com.android.volley.Request.Method.GET, url,
                object : Response.Listener<String?> {
                    private lateinit var imageBitmap: Bitmap

                    override fun onResponse(response: String?) {


                        try {
                            val jsonObject = JSONObject(response)

                            val jsonArray = jsonObject.getJSONArray("photos")

                            val length = jsonArray.length()

                            makeToast("Wlength7 - " + length)
                            for (i in 0 until length) {
                                val `object` = jsonArray.getJSONObject(i)

                                val id = `object`.getInt("id")

                                val objectImages = `object`.getJSONObject("src")

                                val orignalUrl = objectImages.getString("original")

                                makeToast(orignalUrl)


                                try {
                                    val url = URL(orignalUrl)
                                    val thread = Thread {
                                        try {
                                            // Your code goes here
                                            imageBitmap = BitmapFactory.decodeStream(
                                                url.openConnection().getInputStream())
                                            wallBitmaps.toMutableList().add(imageBitmap)

                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            makeToast("EXCE1 - " + e.toString())

                                        }
                                    }

                                    thread.start()


                                } catch (e: IOException) {
                                    println(e)
                                    makeToast("EXCE2 - " + e.toString())
                                }
                            }

                     //       makeToast("Wlength7 - allLoaded" + length)

                        } catch (e: JSONException) {
                        }
                    }
                }, object : Response.ErrorListener {
                    override fun onErrorResponse(error: VolleyError?) {

                    }
                }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params["Authorization"] = "563492ad6f9170000100000123804538e2a24b5c9381b7c388de9f80"

                    return params
                }
            }

            val requestQueue = Volley.newRequestQueue(appContx)
            requestQueue.add(request)

            return wallBitmaps
        }
    }
}