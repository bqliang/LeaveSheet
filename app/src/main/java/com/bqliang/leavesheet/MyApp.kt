package com.bqliang.leavesheet

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.google.android.material.color.DynamicColors
import com.tencent.mmkv.MMKV
import timber.log.Timber

class MyApp : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext

        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())

        DynamicColors.applyToActivitiesIfAvailable(this)

        MMKV.initialize(this)
    }
}