package com.bqliang.leavesheet.edit

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bqliang.leavesheet.R
import com.bqliang.leavesheet.adapter.EditCollectionAdapter
import com.bqliang.leavesheet.databinding.ActivityEditBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.tabs.TabLayoutMediator


class EditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditBinding
    private val viewModel: EditViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit)

        onBackPressedDispatcher.addCallback(this) { showExitDialog() }

        setUpView()
    }


    private fun setUpView() {
        binding.appBarLayout.statusBarForeground = MaterialShapeDrawable.createWithElevationOverlay(this)
        binding.pager.adapter = EditCollectionAdapter(this)
        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
            tab.text = when (position) {
                0 -> "请假条"
                else -> "个人信息"
            }
        }.attach()
        binding.fab.setOnClickListener { update() }
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }


    private fun update() {
        viewModel.update()
        finish()
    }


    private fun showExitDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("提示")
            .setMessage("舍弃已编辑的内容？")
            .setPositiveButton("继续编辑", null)
            .setNegativeButton("保存退出") { _, _ -> update() }
            .setNeutralButton("直接退出") { _, _ -> finish() }
            .show()
    }


    private fun setLightStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.windowInsetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            val flags = window.decorView.systemUiVisibility
            window.decorView.systemUiVisibility = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }
}