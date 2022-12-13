package com.bqliang.leavesheet.main

import android.graphics.Color
import com.bqliang.leavesheet.R
import com.bqliang.leavesheet.utils.SoundPoolUtil
import com.bqliang.leavesheet.utils.launchActivity

class PassPortActivity : WebViewActivity() {

    override val url = "http://bqliangdev.gitee.io/nfu-alipay-pass-code/app"
    override val toolbarBackgroundColorResId = android.R.color.white
    override val navigationContentDesc = "打开请假条"
    override val navigationIconResId = R.drawable.round_chevron_left_24
    override val toolbarIconTint = Color.BLACK
    override val toolbarTitle = "我的通行证"
    override val toolbarTitleTextAppearanceStyleResId =
        R.style.TextAppearance_PassCode_ToolBar_Title
    override val settingsMenuIconResId = R.drawable.round_more_horiz_24


    override fun setUpStatusBar() {
        window.statusBarColor = getColor(R.color.grey)
        if (isNightModeActive())  setLightStatusBar() else setDarkStatusBar()
    }

    override fun onPageLoadSuccess(
        leaveSheetJsonStr: String,
        annexFileNameListJsonStr: String,
        facultyAuditVisible: Boolean
    ) {
        binding.webView.evaluateJavascript(
            "javascript:edit('${leaveSheetJsonStr}');",
            null
        )
    }

    override fun onNavigationOnClick() {
        launchActivity<LeaveSheetActivity>()
    }


    override fun onDestroy() {
        super.onDestroy()
        SoundPoolUtil.release()
    }
}