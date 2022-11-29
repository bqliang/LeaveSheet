package com.bqliang.leavesheet.main

import android.content.Context
import android.webkit.JavascriptInterface

class WebAppInterface(private val context: Context) {

    @JavascriptInterface
    fun onBack() = with(context) {
        packageManager.getLaunchIntentForPackage("com.tencent.wework")?.let { startActivity(it) }
    }
}