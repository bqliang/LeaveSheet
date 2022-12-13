package com.bqliang.leavesheet.scan

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bqliang.leavesheet.main.PassPortActivity
import com.bqliang.leavesheet.utils.SoundPoolUtil
import com.bqliang.leavesheet.utils.launchActivity
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import com.permissionx.guolindev.PermissionX

class ScanActivity : AppCompatActivity() {

    companion object {
        private const val SCAN_TITLE_QR_CODE = 1
        private const val REQUEST_CODE_SCAN_ONE = 0X01
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isGrantedCameraPermission =
            PermissionX.isGranted(this, android.Manifest.permission.CAMERA)
        if (isGrantedCameraPermission) {
            startScanCamera()
        } else {
            requestCameraPermission { startScanCamera() }
        }
    }


    private fun requestCameraPermission(onGranted: () -> Unit) = PermissionX.init(this)
        .permissions(android.Manifest.permission.CAMERA)
        .explainReasonBeforeRequest()
        .onExplainRequestReason { scope, deniedList ->
            scope.showRequestReasonDialog(
                permissions = deniedList,
                message = "扫码功能需要您授予我们相机权限",
                positiveText = "好的",
                negativeText = "取消"
            )
        }
        .onForwardToSettings { scope, deniedList ->
            scope.showForwardToSettingsDialog(
                permissions = deniedList,
                message = "您需要在设置中手动授予相机权限",
                positiveText = "好的",
                negativeText = "取消"
            )
        }
        .request { allGranted, _, _ ->
            if (allGranted) {
                onGranted()
            } else {
                Toast.makeText(this, "无法获取照相机权限", Toast.LENGTH_SHORT).show()
                finish()
            }
        }


    private fun startScanCamera() {
        val scanOptions = HmsScanAnalyzerOptions.Creator()
            .setHmsScanTypes(HmsScan.QRCODE_SCAN_TYPE)
            .setViewType(SCAN_TITLE_QR_CODE)
            .setPhotoMode(false)
            .create()
        ScanUtil.startScan(this, REQUEST_CODE_SCAN_ONE, scanOptions)
    }


    @Deprecated(
        "Deprecated in Java", ReplaceWith(
            "super.onActivityResult(requestCode, resultCode, data)",
            "androidx.appcompat.app.AppCompatActivity"
        )
    )
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("deprecation")
        super.onActivityResult(requestCode, resultCode, data)

        val scanQrCodeSuccess =
            requestCode == REQUEST_CODE_SCAN_ONE && resultCode == RESULT_OK && data != null
        if (scanQrCodeSuccess) {
            launchActivity<PassPortActivity>()
            SoundPoolUtil.beep()
        }
        finish()
    }
}