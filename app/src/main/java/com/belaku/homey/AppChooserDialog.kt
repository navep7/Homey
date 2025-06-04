package com.belaku.homey

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.GridView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Collections


class AppChooserDialog : Activity() {
    private var list: java.util.ArrayList<App> = ArrayList()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_chooser_dialog)

        var gridView: GridView = findViewById(R.id.grid_view)

        getApps(applicationContext)

        //   list.add(App("DSA", R.drawable.calls))

        val adapter = GridViewAdapter(this, list)
        gridView.adapter = adapter
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun getApps(applicationContext: Context) {

        val packageManager = applicationContext.packageManager
        val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        var appNames: ArrayList<String> = ArrayList()
        var appIcons: ArrayList<Drawable> = ArrayList()

        for (i in 0 until apps.size) {
            if ((apps.get(i).flags and ApplicationInfo.FLAG_SYSTEM) == 0) {
                var appName = apps.get(i).loadLabel(packageManager).toString()
                var appIcon: Drawable = packageManager.getApplicationIcon(apps.get(i))
                appNames.add(appName)
                appIcons.add(appIcon)
                list.add(App(apps.get(i).loadLabel(packageManager).toString(), appIcon))
                sortApps(list)
            }
        }

     //   sortApps(appNames, appIcons)

    }

    private fun sortApps(list: java.util.ArrayList<App>) {

        Collections.sort<App>(
            list
        ) { p1: App, p2: App ->
            p1.name.compareTo(p2.name)
        }

    }


}