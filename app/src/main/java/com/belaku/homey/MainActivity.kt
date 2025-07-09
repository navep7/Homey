package com.belaku.homey

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.AppOpsManager
import android.app.AppOpsManager.MODE_ALLOWED
import android.app.AppOpsManager.OPSTR_GET_USAGE_STATS
import android.app.Dialog
import android.app.ProgressDialog
import android.app.WallpaperManager
import android.app.admin.DevicePolicyManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.database.Cursor
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.provider.ContactsContract
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RemoteViews
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.belaku.homey.AppChooserDialog.Companion.choosenApps
import com.belaku.homey.NewAppWidget.Companion.Apps
import com.belaku.homey.NewAppWidget.Companion.addAppInWidget
import com.belaku.homey.NewAppWidget.Companion.newAppWidget
import com.belaku.homey.NewAppWidget.Companion.remoteViews
import com.belaku.homey.databinding.ActivityMainBinding
import com.bumptech.glide.Glide
import com.google.android.material.color.DynamicColors
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.net.URL
import java.util.Collections
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {


    private val TAG: String = "WallWorkRequest"
    private lateinit var pD: ProgressDialog
    private lateinit var frameMin: FrameLayout
    private lateinit var frameHour: FrameLayout
    private lateinit var frameDay: FrameLayout
    private lateinit var fabMain: FloatingActionButton
    private lateinit var fabMin: FloatingActionButton
    private lateinit var fabHour: FloatingActionButton
    private lateinit var fabDay: FloatingActionButton
    private lateinit var rvAdapter: RvAdapter
    private lateinit var rv: RecyclerView
    private lateinit var editTextPrompt: EditText
    private var pexelUrl: String =
        "https://api.pexels.com/v1/search?query=$queryType&per_page=10"

    private val RESULT_ENABLE: Int = 1
    private val MY_PERMISSIONS_REQUEST_READ_CONTACTS: Int = 1
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        appContx = applicationContext
        sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        sharedPreferencesEditor = sharedPreferences.edit()

        mAct = this@MainActivity

        parentLayout = findViewById(android.R.id.content);

        pD = ProgressDialog(this@MainActivity)
        pD.setMessage("fetching Walls...")


        DynamicColors.applyToActivitiesIfAvailable(application)

        queryType = sharedPreferences.getString("walltype", "nature").toString()

        sharedPreferences.getStringSet("walls", null)?.let { imgUrls.addAll(it) }
        sharedPreferences.getStringSet("wallDescs", null)?.let { imgDescs.addAll(it) }


        findViewByIds()
        setRV(imgUrls, imgDescs)
        listeners()
        fetchWallpaper(applicationContext)
        GetDisplayDimens()
        checkP()
        appUsageStats(applicationContext)

        sharedPreferencesEditor.putString("qT", queryType).apply()

        fabMain.setOnClickListener { view ->

            if (fabDay.visibility == View.GONE) {
                Snackbar.make(view, "Auto Update Wallpaper, every ?", Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .setAnchorView(R.id.fab_main).show()
                fabDay.visibility = View.VISIBLE
                frameMin.visibility = View.VISIBLE
                frameHour.visibility = View.VISIBLE
                frameDay.visibility = View.VISIBLE
                // Add animation here to expand the menu
            } else {
                fabDay.visibility = View.GONE
                frameMin.visibility = View.GONE
                frameHour.visibility = View.GONE
                frameDay.visibility = View.GONE
                // Add animation here to collapse the menu
            }

            //   setWalls(applicationContext)

            /* val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
             var compName = ComponentName(this, DeviceAdmin::class.java)
             intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
             intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "You should enable the app!")
             startActivityForResult(intent, RESULT_ENABLE)*/
        }

    }

    private fun appUsageStats(applicationContext: Context?) {

        choosenApps.clear()

        val currentHour = Calendar.getInstance()[Calendar.HOUR_OF_DAY]

        val timeOfDay = if (currentHour >= 6 && currentHour < 12) {
            "Morning"
        } else if (currentHour >= 12 && currentHour < 17) {
            "Afternoon"
        } else if (currentHour >= 17 && currentHour < 21) {
            "Evening"
        } else {
            "Night"
        }

        var cDate = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

        val usageStatsManager =
            applicationContext?.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager // Context.USAGE_STATS_SERVICE);


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

            var appName = getAppNameFromPkg(applicationContext, queryUsageStats.get(i).packageName)
            var appIcon = getAppIconFromPkg(applicationContext, queryUsageStats.get(i).packageName)

            Log.d(
                "queryUsageStats",
                "$appName ... - $i : " + queryUsageStats.get(i).totalTimeInForeground
            )

            //    if (queryUsageStats.get(i).totalTimeInForeground > 0)
            if (!appName.contains("Launcher"))
                if (applicationContext.packageManager.getLaunchIntentForPackage(queryUsageStats[i].packageName) != null)
                    if (appNames.add(appName))
                        if (choosenApps.size < 5) {
                            choosenApps.add(
                                App(
                                    appName, appIcon
                                )
                            )
                            Log.d("cLogSetAppIcon", appIcon.toString())
                            addAppInWidget(
                                applicationContext,
                                App(queryUsageStats.get(i).packageName, appIcon)
                            )
                        }
        }
        saveApps(Apps)

    }


    @SuppressLint("Range", "UseCompatLoadingForDrawables")
    fun getFavoriteContacts(context: Context) {

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
            }

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



        }

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

    private fun sortApps(queryUsageStats: List<UsageStats>) {

        Collections.sort<UsageStats>(
            queryUsageStats
        ) { p1: UsageStats, p2: UsageStats ->
            p2.totalTimeInForeground.compareTo(p1.totalTimeInForeground)
            //   p1.name.compareTo(p2.name)
        }

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

    private fun setWalls(delay: Long) {
        //    val oneTimeWorkRequest = OneTimeWorkRequest.Builder(SetWallWorker::class.java).build()

        delayUnit = delay.toString()
        sharedPreferencesEditor.putString("dU", delayUnit).apply()


        val periodicWorkRequest =
            PeriodicWorkRequest.Builder(SetWallWorker::class.java, delay, TimeUnit.MINUTES)
                .setConstraints(Constraints.NONE)
                .build()

        val workManager = WorkManager.getInstance(applicationContext)

        workManager.enqueueUniquePeriodicWork(
            TAG,
            ExistingPeriodicWorkPolicy.REPLACE,
            periodicWorkRequest
        )
    }


    private fun listeners() {

        editTextPrompt.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if ((event != null && (event.keyCode == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                //do what you want on the press of 'done'

                imgUrls.clear()
                imgDescs.clear()

                queryType = editTextPrompt.text.toString()
                sharedPreferencesEditor.putString("qT", queryType).apply()
                pD.show()
                sN.dismiss()
                fetchWallpaper(applicationContext)
            }
            false
        })

        fabMin.setOnClickListener {
            updateInterval = "min"
            makeToast("Wallpaper updates every 15 Mins!")
            setWalls(15)
            sharedPreferencesEditor.putStringSet("walls", HashSet(imgUrls)).apply()
            sharedPreferencesEditor.putStringSet("wallDescs", HashSet(imgDescs)).apply()
        }

        fabHour.setOnClickListener {
            updateInterval = "hour"
            makeToast("Wallpaper updates every 30 Mins!")
            setWalls(30)
            sharedPreferencesEditor.putStringSet("walls", HashSet(imgUrls)).apply()
            sharedPreferencesEditor.putStringSet("wallDescs", HashSet(imgDescs)).apply()
        }

        fabDay.setOnClickListener {
            updateInterval = "day"
            makeToast("Wallpaper updates every 60 Mins!")
            setWalls(60)
            sharedPreferencesEditor.putStringSet("walls", HashSet(imgUrls)).apply()
            sharedPreferencesEditor.putStringSet("wallDescs", HashSet(imgDescs)).apply()
        }

    }

    private fun findViewByIds() {

        editTextPrompt = findViewById(R.id.edtx_prompt)
        fabMain = findViewById(R.id.fab_main)
        frameMin = findViewById(R.id.frame_fab1)
        frameHour = findViewById(R.id.frame_fab2)
        frameDay = findViewById(R.id.frame_fab3)
        fabMin = findViewById(R.id.fab_option_1)
        fabHour = findViewById(R.id.fab_option_2)
        fabDay = findViewById(R.id.fab_option_3)
    }

    private fun GetDisplayDimens() {
        var displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        sharedPreferencesEditor.putInt("sWidth", displayMetrics.widthPixels).apply()
        sharedPreferencesEditor.putInt("sHeight", displayMetrics.heightPixels).apply()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RESULT_ENABLE -> {
                if (resultCode == RESULT_OK) {

                } else {
                    Toast.makeText(
                        applicationContext, "Failed!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
    }

    private fun checkP() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            )
            != PackageManager.PERMISSION_GRANTED
        ) {


            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE),
                MY_PERMISSIONS_REQUEST_READ_CONTACTS
            )


        } else {
            cGranted = true
            // UsageStatsPermissionDialog()
            if (favContacts.size == 0)
                getFavoriteContacts(applicationContext)
        }

        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        var compName = ComponentName(this, DeviceAdmin::class.java)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "You should enable the app!")
        startActivityForResult(intent, RESULT_ENABLE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)

        if (requestCode == MY_PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.size > 0) {
                getFavoriteContacts(applicationContext)
            }
        }
    }

    private fun UsageStatsPermissionDialog() {
        val alertDialog: AlertDialog = AlertDialog.Builder(this@MainActivity).create()
        alertDialog.setTitle("Permission Request")
        alertDialog.setMessage("App needs permission to get Usage stats to suggest you apps to use.. Permit ?")
        alertDialog.setButton(
            AlertDialog.BUTTON_NEUTRAL, "OK"
        ) { dialog, which ->
            val intent1 = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            applicationContext.startActivity(intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            dialog.dismiss()
        }

        if (!getGrantStatus()) {
            alertDialog.show()
        }

    }

    fun fetchWallpaper(context: Context) {


        imgUrls.clear()
        imgDescs.clear()


        imgUrls.sort()
        imgDescs.sort()


        if (imgUrls.size == 0) {

            if (queryType.length != 0) {
                makeSnack("Showing $queryType wallpapers \n Search using above Bar if you seek something else..")
                pexelUrl = "https://api.pexels.com/v1/search?query=$queryType&per_page=35"
                val request: StringRequest = @SuppressLint("NotifyDataSetChanged")
                object : StringRequest(

                    com.android.volley.Request.Method.GET, pexelUrl,
                    Response.Listener<String?> { response ->
                        try {
                            val jsonObject = JSONObject(response)

                            val jsonArray = jsonObject.getJSONArray("photos")

                            val length = jsonArray.length()


                            for (i in 0 until length) {
                                val jsonObject = jsonArray.getJSONObject(i)
                                val objectImages = jsonObject.getJSONObject("src")
                                imgUrls.add("$i + ${objectImages.getString("original")}")
                                imgDescs.add("$i + ${jsonObject.getString("alt")})")
                            }

                            rvAdapter.notifyItemRangeChanged(0, length)
                            pD.dismiss()


                        } catch (e: JSONException) {
                            makeToast("EXE7 - " + e.message)
                        }


                    }, object : Response.ErrorListener {
                        override fun onErrorResponse(error: VolleyError?) {
                            makeToast("onErrorResponse - " + error.toString())
                        }
                    }) {
                    @Throws(AuthFailureError::class)
                    override fun getHeaders(): Map<String, String> {
                        val params: MutableMap<String, String> = HashMap()
                        params["Authorization"] =
                            "563492ad6f9170000100000123804538e2a24b5c9381b7c388de9f80"

                        return params
                    }
                }
                val requestQueue = Volley.newRequestQueue(context)
                requestQueue.add(request)
            } else makeToast("Please Search for the Walls using the above search bar..")
        }
    }

    private fun setRV(imgUrls: java.util.ArrayList<String>, imgDescs: ArrayList<String>) {

        rv = findViewById(R.id.rv_images)
        rv.layoutManager = StaggeredGridLayoutManager(2, 1)
        rvAdapter = RvAdapter(applicationContext, imgUrls, imgDescs)
        rv.adapter = rvAdapter
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


    companion object {
        var favContacts: ArrayList<Contact> = ArrayList()
        lateinit var sN: Snackbar
        lateinit var parentLayout: View
        var delayUnit: String = ""
        var queryType: String = "Material Design"
        var updateTime: String = "00:00"
        lateinit var mAct: Activity
        var updateInterval: String? = null
        lateinit var sharedPreferences: SharedPreferences
        lateinit var sharedPreferencesEditor: SharedPreferences.Editor
        var randomNumber: Int = 0
        val imgUrls: ArrayList<String> = ArrayList()
        var imgDescs: ArrayList<String> = ArrayList()
        var cGranted: Boolean = false
        lateinit var appContx: Context


        fun makeToast(s: String) {
            Toast.makeText(appContx, s, Toast.LENGTH_SHORT).show()
            Log.d("makeToastinG", s)
        }

        fun makeSnack(s: String) {
            sN = Snackbar.make(parentLayout, s, Snackbar.LENGTH_INDEFINITE)
            sN.show()
            Log.d("makeToastinG", s)
        }

        fun showSelected(adapterPosition: Int) {

            var url = imgUrls[adapterPosition]
            url = url.split("+ ")[1]

            val dialog = Dialog(mAct)
            dialog.setContentView(R.layout.imgv_dialog_layout)
            dialog.setTitle("Title...")

            var image: ImageView = dialog.findViewById(R.id.imgv_dialog)
            var txt: TextView = dialog.findViewById(R.id.tx_dialog)
            var set: Button = dialog.findViewById(R.id.btn_set_dialog)

            set.setOnClickListener(View.OnClickListener {
                Thread {
                    val inputStream = URL(url).openStream()
                    WallpaperManager.getInstance(appContx).setStream(inputStream)
                }.start()
                Handler(Looper.getMainLooper()).postDelayed(Runnable { makeToast("Set!") }, 1000)

            })

            txt.text = imgDescs[adapterPosition].substring(4, imgDescs[adapterPosition].length)


            Glide.with(appContx)
                .load(url)
                .into(image)

            dialog.show()
        }
    }
}