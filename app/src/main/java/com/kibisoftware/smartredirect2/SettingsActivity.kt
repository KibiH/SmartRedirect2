package com.kibisoftware.smartredirect2

import android.Manifest
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText

class SettingsActivity : AppCompatActivity() {

    companion object NumberInfo{
        var callNumber: String? = null
    }
    var myPreference: SharedPreferences? = null
    private var replacementText: EditText? = null
    private var ignoreText:EditText? = null
    private var replacement: String? = null
    private var ignore:String? = null

    private val REQUEST_CALL_PERMISSION = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        myPreference = PreferenceManager.getDefaultSharedPreferences(this)

		val interceptBox = findViewById(R.id.checkBoxIntercept) as CheckBox
	    val intercept = myPreference?.getBoolean("interception", false)
        interceptBox.isChecked = intercept ?: false
		interceptBox.setOnCheckedChangeListener { buttonView, isChecked ->
            val editor = myPreference?.edit()
            editor?.putBoolean("interception", isChecked)
            editor?.commit()
        }

        val promptBox = findViewById(R.id.checkBoxPopup) as CheckBox
		val prompt = myPreference?.getBoolean("promptalways", false)
        promptBox.isChecked = prompt ?: false
		promptBox.setOnCheckedChangeListener(object: CompoundButton.OnCheckedChangeListener {

            override fun onCheckedChanged(buttonView: CompoundButton, isChecked:Boolean) {
                val editor = myPreference?.edit()
                editor?.putBoolean("promptalways", isChecked)
                editor?.commit()
            }
        })

		replacementText = findViewById(R.id.editReplacement) as EditText
		replacement = myPreference?.getString("replacement", "")
		replacementText?.setText(replacement)

		ignoreText = findViewById(R.id.editIgnore) as EditText
		ignore = myPreference?.getString("ignore", "")
		ignoreText?.setText(ignore)

		val okButton = findViewById(R.id.buttonOK) as Button
		okButton.setOnClickListener(object: View.OnClickListener {

            override fun onClick(v: View) {
                replacement = replacementText?.getText().toString()
                ignore = ignoreText?.getText().toString()
                val editor = myPreference?.edit()
                editor?.putString("replacement", replacement)
                ignore?.let{
                    if (it.length > 0) {
                        editor?.putString("ignore", ignore)
                    }
                }

                 editor?.commit()

                 this@SettingsActivity.finish()
                Log.i("Kibi", "Hit the ok")

            }
        })

        val permissionCall = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE)

        if (permissionCall != PackageManager.PERMISSION_GRANTED) {
            // we need to ask permission
            askPermission()
        } else {
            val permissionProcess = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.PROCESS_OUTGOING_CALLS)
            if (permissionProcess != PackageManager.PERMISSION_GRANTED) {
                // we need to ask permission
                askPermission()
            }
        }

    }

    private fun askPermission() {
        val prePermissionAlert = AlertDialog.Builder(this@SettingsActivity, R.style.DialogTheme)
        with (prePermissionAlert) {
            setTitle(getString(R.string.pre_permission_title))
            setMessage(getString(R.string.pre_permission_text))
            setCancelable(false)

            setNegativeButton(getString(R.string.cancel)) {
                dialog: DialogInterface, which: Int ->
                dialog.dismiss()
                noPermissionGranted()
            }
            setPositiveButton(getString(R.string.ok)) {
                dialog: DialogInterface, which: Int ->
                dialog.dismiss()
                ActivityCompat.requestPermissions(this@SettingsActivity,
                        arrayOf(Manifest.permission.CALL_PHONE, Manifest.permission.PROCESS_OUTGOING_CALLS),
                        REQUEST_CALL_PERMISSION)
            }
        }
        val dialog = prePermissionAlert.create()
        dialog.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CALL_PERMISSION -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    //denied
                    noPermissionGranted()
                } else {
                    //allowed
                    val yesPermissionAlert = AlertDialog.Builder(this@SettingsActivity, R.style.DialogTheme).create()
                    yesPermissionAlert.setTitle(getString(R.string.yes_permission_title))
                    yesPermissionAlert.setMessage(getString(R.string.yes_permission_text))
                    yesPermissionAlert.setCancelable(false)


                    yesPermissionAlert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok)) {
                        dialog: DialogInterface?, which: Int ->
                        this@SettingsActivity.finish()
                    }

                    yesPermissionAlert.show()
                }
            }
        }
    }

    private fun noPermissionGranted() {
        val noPermissionAlert = AlertDialog.Builder(this@SettingsActivity,R.style.DialogTheme).create()
        noPermissionAlert.setTitle(getString(R.string.no_permission_title))
        noPermissionAlert.setMessage(getString(R.string.no_permission_text))
        noPermissionAlert.setCancelable(false)

        noPermissionAlert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok)) {
            dialog: DialogInterface?, which: Int ->
            this@SettingsActivity.finish()
        }

        noPermissionAlert.show()
    }
}
