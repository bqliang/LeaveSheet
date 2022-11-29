package com.bqliang.leavesheet.main

import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

class MyWebViewClient : WebViewClient() {

    sealed class Status {
        object Loading : Status()
        object Success : Status()
        data class Error(val error: WebResourceError) : Status()
    }

    companion object {
        private val _status = MutableStateFlow<Status?>(null)

        @JvmStatic
        val status: StateFlow<Status?>
            get() = _status
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        Timber.i("onPageStarted: $url")
        super.onPageStarted(view, url, favicon)
        _status.value = Status.Loading
    }


    override fun onReceivedError(
        view: WebView, request: WebResourceRequest, error: WebResourceError
    ) {
        Timber.e("onReceivedError: [request: ${request.method} ${request.url}, isForMainFrame: ${request.isForMainFrame}, error code: ${error.errorCode}, description: ${error.description}]")
        super.onReceivedError(view, request, error)
        if (request.isForMainFrame) _status.value = Status.Error(error)
    }


    override fun onPageFinished(view: WebView?, url: String?) {
        Timber.i("onPageFinished: $url")
        super.onPageFinished(view, url)
        if (_status.value is Status.Loading) _status.value = Status.Success
    }
}