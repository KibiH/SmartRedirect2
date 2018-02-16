package com.kibisoftware.smartredirect2

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.util.Log

class CallInterceptor : BroadcastReceiver() {

    val LOG_TAG = "CallInterceptor"

    override fun onReceive(context: Context, intent: Intent) {
        Log.i("Kibi", "Hit the onReceive")
        if (intent.action != "android.intent.action.NEW_OUTGOING_CALL") {
            return
        }
        val phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
        //short numbers just ignore and leave to the phone
        if (null == phoneNumber || phoneNumber.length < 10) {
            return
        }

        val hasOurNumber = SettingsActivity.callNumber?.contains(phoneNumber) ?: false

        if (null != SettingsActivity.callNumber && hasOurNumber) {
            Log.d(LOG_TAG, "number has been intercepted once - let it though")
            //we just dialed this number
            SettingsActivity.callNumber = null //only block once
            return
        }

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val showPrompt = prefs.getBoolean("promptalways", false)
        if (showPrompt) {
            Log.d(LOG_TAG, "Bring up the prompt dialog")
            resultData = null //this kills the call
            //now we bring up the popup dialog activity
            val i = Intent(context, com.kibisoftware.smartredirect2.PopupCallMenu::class.java)
            i.addFlags(Intent.FLAG_FROM_BACKGROUND)
            i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            i.putExtra(Intent.EXTRA_PHONE_NUMBER, phoneNumber)
            context.startActivity(i)
        } else
        //check the interception parameters
        {
            Log.d(LOG_TAG, "Number we are checking is " + phoneNumber)
            val interception = prefs.getBoolean("interception", false)
            if (interception) {
                //redirect if international
                //check if international
                if (phoneNumber.startsWith("00") || phoneNumber.startsWith("+")
                        || phoneNumber.startsWith("011")) {
                    //this is an international call...except in SA 011 is to Jo'burg or something
                    val hdc = prefs.getString("hdc", "1")
                    if (hdc == "27" && phoneNumber.startsWith("011"))
                    //SA code
                    {
                        //do nothing it will dial through
                    } else
                    //all other cases call via redirect
                    {
                        Log.d(LOG_TAG, "About to replace code in number")
                        resultData = null //this kills the call
                        callUsingRedirect(context, phoneNumber)
                    }
                } else {
                    Log.d(LOG_TAG, "No prefix found to replace")
                }
            } else {
                Log.d(LOG_TAG, "Not intercepting calls now")
            }
        }
    }

    companion object {
        fun callUsingRedirect(context: Context, phoneNumber: String) {
            //reset the number replacing '+' with our replacement
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val replacement = prefs.getString("replacement", "")
            var offset = 0
            if (phoneNumber.startsWith("011")) {
                offset = 3
            } else if (phoneNumber.startsWith("00")) {
                offset = 2
            } else if (phoneNumber.startsWith("+")) {
                offset = 1
            }
            val ignore = prefs.getString("ignore", "xxx")
            val testString = phoneNumber.substring(offset)
            if (testString.startsWith(ignore!!)) {
                //leave number as is
                Log.d("CallInterceptor", "This is an ignored number - pass through as is")
                SettingsActivity.callNumber = phoneNumber
            } else {
                SettingsActivity.callNumber = replacement!! + phoneNumber.substring(offset)
                Log.d("CallInterceptor", "Replacement number is " + SettingsActivity.callNumber)
            }
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:" + SettingsActivity.callNumber)
            callIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            val permission = ContextCompat.checkSelfPermission(context,
                    Manifest.permission.CALL_PHONE)

            if (permission != PackageManager.PERMISSION_GRANTED) {
                Log.i("CallInterceptor", "Permission to call denied")
                return;
            }
            context.startActivity(callIntent)
        }

    }
}
