package com.wellysis.spatchcardio.ex.sdkexample

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.orhanobut.hawk.Hawk
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class SPatchExApp : Application() {

    companion object {
        private var instance: SPatchExApp? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onLowMemory() {
        super.onLowMemory()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
    }

    override fun onCreate() {
        super.onCreate()

        // Timber
        Timber.plant(Timber.DebugTree())

        // Hawk
        Hawk.init(this).build()
    }

    override fun onTerminate() {
        super.onTerminate()
    }
}