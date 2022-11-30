package com.bqliang.leavesheet.main

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.bqliang.leavesheet.BuildConfig
import com.bqliang.leavesheet.R
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

class LeaveSheetActivity() : WebViewActivity(), DistributeListener {

    override val url = "http://bqliangdev.gitee.io/leave-sheet-web"
    override val toolbarBackgroundColorResId = R.color.wework_blue
    override val navigationContentDesc = "打开通行证"
    override val navigationIconResId = R.drawable.ic_outline_close_24
    override val toolbarIconTint = Color.WHITE
    override val toolbarTitle = "本科生请假"
    override val toolbarTitleTextAppearanceStyleResId = R.style.TextAppearance_LeaveSheet_ToolBar_Title
    override val settingsMenuIconResId = R.drawable.ic_round_more_vert_24


    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivityIfAvailable(this)

        // set up AppCenter
        Distribute.setListener(this)
        AppCenter.start(
            application, BuildConfig.APP_CENTER_SECRET,
            Analytics::class.java, Crashes::class.java, Distribute::class.java
        )
    }


    override fun setUpStatusBar() {
        window.statusBarColor = getColor(toolbarBackgroundColorResId)
        setDarkStatusBar()
    }


    override fun onNavigationOnClick() {
        launchActivity<PassPortActivity>()
    }


    override fun onPageLoadSuccess(
        leaveSheetJsonStr: String,
        annexFileNameListJsonStr: String,
        facultyAuditVisible: Boolean
    ) {
        binding.webView.evaluateJavascript(
            "javascript:edit('${leaveSheetJsonStr}', ${facultyAuditVisible});",
            null
        )
        binding.webView.evaluateJavascript(
            "javascript:setAnnex('${annexFileNameListJsonStr}');",
            null
        )
    }


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