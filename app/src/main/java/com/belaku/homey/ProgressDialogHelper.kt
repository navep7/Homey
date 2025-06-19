package com.belaku.homey

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog


object ProgressDialogHelper {
    fun createProgressDialog(context: Context?, message: String?): Dialog {
        val builder = AlertDialog.Builder(
            context!!
        )
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(
            R.layout.progress_dialog_layout,
            null
        ) // Create a layout with ProgressBar and TextView
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val messageTextView = view.findViewById<TextView>(R.id.messageTextView)
        messageTextView.text = message
        builder.setView(view)
        builder.setCancelable(false) // Prevent dismissing on outside touch
        val dialog = builder.create()
        return dialog
    }
}
