package com.bqliang.leavesheet.main

import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import timber.log.Timber

class MyWebChromeClient : WebChromeClient() {

    companion object {

        private val _progress = MutableLiveData<Int>()

        @JvmStatic
        val progress: LiveData<Int> = _progress
    }

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        _progress.value = newProgress
        Timber.i("onProgressChanged: $newProgress")
    }
}
