package com.kibisoftware.smartredirect2

import android.app.Application
import android.content.Context

/**
 * Created by kibi on 13/02/18.
 */
class MainApplication :Application() {

    init {
        instance = this
    }

    companion object {
        private var instance: MainApplication? = null

        fun applicationContext() : Context {
            return instance !!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()

        val context: Context = MainApplication.applicationContext()
    }
}