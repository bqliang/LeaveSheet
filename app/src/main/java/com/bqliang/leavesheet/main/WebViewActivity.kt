package com.bqliang.leavesheet.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebViewClient.*
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import androidx.core.view.*
import androidx.databinding.DataBindingUtil
import com.bqliang.leavesheet.*
import com.bqliang.leavesheet.data.database.LeaveSheetDatabase
import com.bqliang.leavesheet.data.datastore.LeaveSheet
import com.bqliang.leavesheet.data.datastore.SettingsDataStore
import com.bqliang.leavesheet.data.datastore.leaveSheetDataStore
import com.bqliang.leavesheet.databinding.ActivityWebviewBinding
import com.bqliang.leavesheet.settings.SettingsActivity
import com.bqliang.leavesheet.utils.launchActivity
import kotlinx.coroutines.flow.combine
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.toptas.fancyshowcase.FancyShowCaseView


abstract class WebViewActivity : BaseActivity() {

    protected abstract val url: String
    @get:ColorRes
    protected abstract val toolbarBackgroundColorResId: Int
    protected abstract val navigationContentDesc: String
    @get:DrawableRes
    protected abstract val navigationIconResId: Int
    @get:ColorInt
    protected abstract val toolbarIconTint: Int
    protected abstract val toolbarTitle: String
    @get:StyleRes
    protected abstract val toolbarTitleTextAppearanceStyleResId: Int
    @get:DrawableRes
    protected abstract val settingsMenuIconResId: Int

    protected abstract fun setUpStatusBar()
    protected abstract fun onPageLoadSuccess(
        leaveSheetJsonStr: String,
        annexFileNameListJsonStr: String,
        facultyAuditVisible: Boolean
    )

    protected abstract fun onNavigationOnClick()

    protected lateinit var binding: ActivityWebviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpStatusBar()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_webview)
        binding.lifecycleOwner = this
        initView()
    }


    private fun initView() {
        setUpToolbar()
        subscribeFlow()
        setUpWebView()

        // 设置下拉刷新的颜色
        binding.swipeRefreshLayout.setColorSchemeResources(R.color.wework_blue)

        // 下拉刷新 webview
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            binding.webView.loadUrl(url)
        }

        // 重试按钮刷新 webview
        binding.retryButton.setOnClickListener { binding.webView.loadUrl(url) }
    }


    private fun setUpToolbar() = binding.toolbar.apply {
        val settingsMenu = menu.findItem(R.id.menu_settings)
        setBackgroundResource(toolbarBackgroundColorResId)
        navigationContentDescription = navigationContentDesc
        setNavigationIcon(navigationIconResId)
        setNavigationIconTint(toolbarIconTint)
        title = toolbarTitle
        setTitleTextAppearance(context, toolbarTitleTextAppearanceStyleResId)
        settingsMenu.setIcon(settingsMenuIconResId)
        settingsMenu.icon?.setTint(toolbarIconTint)

        setNavigationOnClickListener {
            onNavigationOnClick()
        }

        setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_settings -> {
                    launchActivity<SettingsActivity>()
                    true
                }
                else -> false
            }
        }
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun setUpWebView() = with(binding.webView) {
        webViewClient = MyWebViewClient()
        webChromeClient = MyWebChromeClient()
        settings.javaScriptEnabled = true
//        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        loadUrl(this@WebViewActivity.url)
        addJavascriptInterface(WebAppInterface(this@WebViewActivity), "Android")
    }


    private fun subscribeFlow() = combine(
        MyWebViewClient.status,
        MyApp.context.leaveSheetDataStore.data,
        LeaveSheetDatabase.getDatabase().annexDao().loadAllAnnexDesc(),
        SettingsDataStore.facultyAuditVisible
    ) { status, leaveSheet, annexList, facultyAuditVisible ->
        when (status) {
            MyWebViewClient.Status.Loading -> {
                binding.webView.visibility = View.INVISIBLE
                binding.errorGroup.visibility = View.GONE
            }

            MyWebViewClient.Status.Success -> {
                binding.webView.visibility = View.VISIBLE
                binding.errorGroup.visibility = View.GONE
                val leaveSheetJsonStr = Json.encodeToString(LeaveSheet.serializer(), leaveSheet)
                val annexFileNameList = annexList.map { it.fileName }
                val annexFileNameListJsonStr = Json.encodeToString(annexFileNameList)
                onPageLoadSuccess(leaveSheetJsonStr, annexFileNameListJsonStr, facultyAuditVisible)
            }

            is MyWebViewClient.Status.Error -> {
                binding.errorText.text = when (status.error.errorCode) {
                    ERROR_TIMEOUT -> "请求超时"
                    ERROR_CONNECT -> "您的网络连接不可用"
                    ERROR_HOST_LOOKUP -> "无法连接到服务器"
                    else -> "${status.error.description} (${status.error.errorCode})"
                }

                binding.errorIcon.setImageResource(
                    when (status.error.errorCode) {
                        ERROR_CONNECT -> R.drawable.ic_connection_error
                        ERROR_HOST_LOOKUP -> R.drawable.ic_connection_error
                        else -> R.drawable.ic_round_error_outline_24
                    }
                )

                binding.webView.visibility = View.INVISIBLE
                binding.errorGroup.visibility = View.VISIBLE
            }

            null -> {}
        }
    }.collectLifecycle(this)


    private fun guide() = FancyShowCaseView.Builder(this)
//        .focusOn(findViewById(R.id.action_overflow_btn))
        .focusOn(findViewById(R.id.menu_settings))
        .showOnce("guide_settings")
        .title("点击右上角查看设置")
        .build()
        .show()
}