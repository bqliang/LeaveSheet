package com.bqliang.leavesheet.main

import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.method.LinkMovementMethod
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.bqliang.leavesheet.BuildConfig
import com.bqliang.leavesheet.R
import com.bqliang.leavesheet.adapter.AnnexesAdapter
import com.bqliang.leavesheet.data.datastore.SettingsDataStore
import com.bqliang.leavesheet.databinding.ActivityLeaveSheetBinding
import com.bqliang.leavesheet.databinding.DialogAboutBinding
import com.bqliang.leavesheet.databinding.DialogSettingsBinding
import com.bqliang.leavesheet.edit.EditActivity
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
import kotlinx.coroutines.launch
import me.toptas.fancyshowcase.FancyShowCaseQueue
import me.toptas.fancyshowcase.FancyShowCaseView
import me.toptas.fancyshowcase.FocusShape
import me.toptas.fancyshowcase.listener.OnCompleteListener
import rikka.html.text.toHtml


class LeaveSheetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLeaveSheetBinding
    private val viewModel: LeaveSheetViewModel by viewModels()
    private lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>

    inner class MyDistributeListener : DistributeListener {

        override fun onReleaseAvailable(
            activity: Activity,
            releaseDetails: ReleaseDetails
        ): Boolean {
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

        override fun onNoReleaseAvailable(activity: Activity) {
            Toast.makeText(activity, "已经是最新版本", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivityIfAvailable(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_leave_sheet)

        binding.apply {
            lifecycleOwner = this@LeaveSheetActivity
            viewModel = this@LeaveSheetActivity.viewModel
        }

        setUpView()

        Distribute.setListener(MyDistributeListener())
        AppCenter.start(
            application, BuildConfig.APP_CENTER_SECRET,
            Analytics::class.java, Crashes::class.java, Distribute::class.java
        )
    }


    private fun setUpView() {
        binding.cardAnnexList.recyclerView.adapter = AnnexesAdapter(viewModel)

        with(binding.backButton) {
            setOnClickListener { viewModel.openWeWork() }

            setOnLongClickListener {
                launchActivity<EditActivity>(context)
                true
            }
        }

        with(binding.toolbar) {
            overflowIcon?.setTint(Color.WHITE)
            setNavigationOnClickListener { viewModel.openWeWork() }
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.toolbar_edit -> { launchActivity<EditActivity>(context) }
                    R.id.toolbar_about -> { showAboutDialog() }
                    R.id.toolbar_check_update -> { Distribute.checkForUpdate() }
                    R.id.toolbar_settings -> { showSettingsDialog() }
                    else -> {}
                }
                true
            }
        }

        pickMedia =
            registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(5)) { uris ->
                val fileNames = mutableListOf<String>()
                uris?.forEach { uri ->
                    val cursor = contentResolver.query(uri, null, null, null, null, null)
                    cursor?.moveToFirst()
                    cursor?.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                        ?.let {
                            fileNames.add(it)
                        }
                    cursor?.close()
                }
                viewModel.saveAnnex(fileNames)
            }

        binding.cardAnnexList.headline.setOnLongClickListener {
            val pickVisualMediaRequest =
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            Toast.makeText(this, "请选择附件", Toast.LENGTH_SHORT).show()
            pickMedia.launch(pickVisualMediaRequest)
            true
        }
    }


    private fun guide() {
        binding.scrollView.apply {
            isSmoothScrollingEnabled = true
//            fullScroll(View.FOCUS_DOWN)
//            val location = IntArray(2)
//            binding.cardDetails.startDateTitle.getLocationOnScreen(location)
//            smoothScrollTo(0, location[1] - binding.toolbar.height, 800)
            val arr = IntArray(2)
            binding.scrollView.getLocationOnScreen(arr)
            Toast.makeText(this@LeaveSheetActivity, "${arr[0]}, ${arr[1]}", Toast.LENGTH_SHORT)
                .show()
        }

        val backBtnCaseView = FancyShowCaseView.Builder(this)
            .enableAutoTextPosition()
            .fitSystemWindows(true)
            .focusOn(binding.backButton)
            .focusShape(FocusShape.ROUNDED_RECTANGLE)
            .title("长按返回按钮进入编辑页面")
            .roundRectRadius(40)
            .build()

        val addAnnexCaseView = FancyShowCaseView.Builder(this)
            .enableAutoTextPosition()
            .fitSystemWindows(true)
            .focusOn(binding.cardAnnexList.headline)
            .title("长按附件列表标题添加附件")
            .build()

        val actionOverflowCaseView = FancyShowCaseView.Builder(this)
            .enableAutoTextPosition()
            .fitSystemWindows(true)
            .focusOn(findViewById(R.id.action_overflow_btn))
            .title("点击这里查看更多设置")
            .build()

        FancyShowCaseQueue().apply {
            add(backBtnCaseView)
            add(addAnnexCaseView)
            add(actionOverflowCaseView)
            completeListener = object : OnCompleteListener {
                override fun onComplete() {
                    Toast.makeText(this@LeaveSheetActivity, "Finished", Toast.LENGTH_SHORT).show()
                }
            }
            //show()
        }
    }


    private fun showSettingsDialog() {
        val binding = DialogSettingsBinding.inflate(layoutInflater, null, false)
        binding.apply {
            lifecycleOwner = this@LeaveSheetActivity
            facultyAuditVisible = viewModel.facultyAuditVisible

            facultyAuditVisibilitySwitch.setOnCheckedChangeListener { _, check ->
                lifecycleScope.launch {
                    SettingsDataStore.saveFacultyAuditVisible(check)
                }
            }

            facultyAuditVisibilityPreference.setOnClickListener {
                facultyAuditVisibilitySwitch.isChecked = !facultyAuditVisibilitySwitch.isChecked
            }
        }

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("设置")
            .setView(binding.root)
            .create()

        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialog.setOnShowListener {
            with(this@LeaveSheetActivity.binding.scrollView) {
                smoothScrollTo(0, children.first().height - height, 2000)
            }
        }

        dialog.show()
    }


    private fun showAboutDialog() {
        Analytics.trackEvent("About clicked")

        val appIcon = packageManager.getApplicationIcon(packageName)
        val binding = DialogAboutBinding.inflate(layoutInflater, null, false)
        binding.apply {
            icon.setImageDrawable(appIcon)

            sourceCode.movementMethod = LinkMovementMethod.getInstance()
            sourceCode.text = getString(
                R.string.about_view_source_code,
                "<b><a href=\"https://github.com/bqliang/LeaveSheet\">GitHub</a></b>"
            ).toHtml()

            versionName.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(0)
                ).versionName
            } else {
                packageManager.getPackageInfo(packageName, 0).versionName
            }
        }

        MaterialAlertDialogBuilder(this)
            .setView(binding.root)
            .show()
    }
}