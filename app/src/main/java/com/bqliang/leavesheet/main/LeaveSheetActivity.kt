package com.bqliang.leavesheet.main

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebViewClient.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.*
import androidx.databinding.DataBindingUtil
import com.bqliang.leavesheet.*
import com.bqliang.leavesheet.data.database.LeaveSheetDatabase
import com.bqliang.leavesheet.data.datastore.LeaveSheet
import com.bqliang.leavesheet.data.datastore.SettingsDataStore
import com.bqliang.leavesheet.data.datastore.leaveSheetDataStore
import com.bqliang.leavesheet.databinding.ActivityLeavesheetBinding
import com.bqliang.leavesheet.settings.SettingsActivity
import com.bqliang.leavesheet.utils.launchActivity
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.microsoft.appcenter.distribute.Distribute
import com.microsoft.appcenter.distribute.DistributeListener
import com.microsoft.appcenter.distribute.ReleaseDetails
import com.microsoft.appcenter.distribute.UpdateAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.toptas.fancyshowcase.FancyShowCaseView


class LeaveSheetActivity : BaseActivity(), DistributeListener {

    companion object {
        private const val URL = "http://bqliangdev.gitee.io/leave-sheet-web"
    }

    private lateinit var binding: ActivityLeavesheetBinding
    private val leaveSheetJsonStr: Flow<String> by lazy {
        MyApp.context.leaveSheetDataStore.data.map { leaveSheet ->
            Json.encodeToString(LeaveSheet.serializer(), leaveSheet)
        }
    }
    private val annexFileNameListJson: Flow<String> by lazy {
        LeaveSheetDatabase.getDatabase().annexDao().loadAllAnnexDesc().map { annexList ->
            val annexFileNameList = annexList.map { annnex -> annnex.fileName }
            Json.encodeToString(annexFileNameList)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        // 设置 Material 3 动态颜色
        DynamicColors.applyToActivityIfAvailable(this)
        // 设置状态栏颜色为蓝色
        window.statusBarColor = getColor(R.color.wework_blue)
        // 设置深色状态栏(图标字体颜色为白色)
        setDarkStatusBar()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_leavesheet)
        binding.lifecycleOwner = this

        initView()

        // 设置 AppCenter
        Distribute.setListener(this)
        AppCenter.start(
            application, BuildConfig.APP_CENTER_SECRET,
            Analytics::class.java, Crashes::class.java, Distribute::class.java
        )
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun initView() {
        // 设置 webview
        with(binding.webView) {
            webViewClient = MyWebViewClient()
            webChromeClient = MyWebChromeClient()
            settings.javaScriptEnabled = true
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            loadUrl(URL)
            addJavascriptInterface(WebAppInterface(this@LeaveSheetActivity), "Android")
            doOnPreDraw { guide() }
        }


        collectLifecycleFlow(MyWebViewClient.status) { status ->
            if (status == null) return@collectLifecycleFlow
            when (status) {
                MyWebViewClient.Status.Loading -> {
                    binding.webView.visibility = View.INVISIBLE
                    binding.errorGroup.visibility = View.GONE
                }

                // 错误时隐藏 webview, 显示 LottieAnimationView
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

                // 加载成功
                MyWebViewClient.Status.Success -> {
                    binding.webView.visibility = View.VISIBLE
                    binding.errorGroup.visibility = View.GONE
                }
            }
        }

        combine(
            MyWebViewClient.status,
            leaveSheetJsonStr,
            annexFileNameListJson,
            SettingsDataStore.facultyAuditVisible
        ) { status, leaveSheetJsonStr, annexFileNameListJsonStr, facultyAuditVisible ->
            if (status is MyWebViewClient.Status.Success) {
                binding.webView.evaluateJavascript(
                    "javascript:edit('${leaveSheetJsonStr}', ${facultyAuditVisible});",
                    null
                )
                binding.webView.evaluateJavascript(
                    "javascript:setAnnex('${annexFileNameListJsonStr}');",
                    null
                )
            }
        }.collectLifecycle(this)

        // 设置 toolbar
        with(binding.toolbar)
        {
            overflowIcon?.setTint(Color.WHITE)

            setNavigationOnClickListener {
                val intent = packageManager.getLaunchIntentForPackage("com.tencent.wework")
                if (intent != null)
                    startActivity(intent)
                else
                    onBackPressedDispatcher.onBackPressed()
            }

            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_more -> launchActivity<SettingsActivity>(this@LeaveSheetActivity)
                }
                true
            }
        }

        // 设置下拉刷新的颜色
        binding.swipeRefreshLayout.setColorSchemeResources(R.color.wework_blue)

        // 下拉刷新 webview
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.webView.reload()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        // 重试按钮刷新 webview
        binding.retryButton.setOnClickListener {
            binding.webView.reload()
        }
    }


    private fun guide() = FancyShowCaseView.Builder(this)
//        .focusOn(findViewById(R.id.action_overflow_btn))
        .focusOn(findViewById(R.id.menu_more))
        .showOnce("guide_more")
        .title("点击右上角查看设置")
        .build()
        .show()


    override fun onReleaseAvailable(activity: Activity, releaseDetails: ReleaseDetails): Boolean {
        val versionName = releaseDetails.shortVersion
        val versionCode = releaseDetails.version
        val releaseNotes = releaseDetails.releaseNotes
        val releaseNotesUrl = releaseDetails.releaseNotesUrl

        val dialogBuilder = MaterialAlertDialogBuilder(activity)
            .setTitle("新版本: $versionName")
            .setMessage(releaseNotes)
            .setPositiveButton("更新") { _, _ ->
                Distribute.notifyUpdateAction(UpdateAction.UPDATE)
            }

        if (!releaseDetails.isMandatoryUpdate) {
            dialogBuilder.setNegativeButton("延迟") { _, _ ->
                Distribute.notifyUpdateAction(UpdateAction.POSTPONE)
            }
        }
        dialogBuilder.setCancelable(false)
        dialogBuilder.show()

        // Return true if you're using your own dialog, false otherwise
        return true
    }

    override fun onNoReleaseAvailable(activity: Activity?) {}
}