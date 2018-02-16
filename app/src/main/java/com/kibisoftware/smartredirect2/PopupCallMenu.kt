package com.kibisoftware.smartredirect2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button

/**
 * Created by kibi on 13/02/18.
 */
class PopupCallMenu : AppCompatActivity() {

    private val TAG = "PopupCallMenu"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("Kibi", "Hit the onCreate")
        setContentView(R.layout.popupmenu)

        val info = intent
        val phoneNumber = info.getStringExtra(Intent.EXTRA_PHONE_NUMBER)

        val callByDefault = findViewById(R.id.CallByDefaultButton) as Button
        callByDefault.setOnClickListener {
            //invoke the phone and kill this view
            SettingsActivity.callNumber = phoneNumber
            val permission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CALL_PHONE)

            if (permission != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission to call denied")
            } else {
                startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:" + SettingsActivity.callNumber)))
            }

            finish()
        }

        val callByRedirect = findViewById(R.id.CallByRedirectButton) as Button

       	callByRedirect.setOnClickListener {
               //start the main dialer window with some info (phone number)
               CallInterceptor.callUsingRedirect(this, phoneNumber)
               finish()
           }


    }
}