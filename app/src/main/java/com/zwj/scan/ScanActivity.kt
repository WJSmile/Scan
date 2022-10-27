package com.zwj.scan

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.palmpay.scan.bean.CodeBean
import com.palmpay.scan.callback.OnScanListener
import com.palmpay.scan.view.ScanView


class ScanActivity : AppCompatActivity() {

    private var scanView: ScanView? = null

    companion object {
        const val SCAN_CODE_KEY = "scan_code_key"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparentStatusBar()
        setContentView(R.layout.activity_scan)
        scanView = findViewById(R.id.scan_view)
        scanView?.setOnScanListener(object : OnScanListener {
            override fun onResult(result: List<CodeBean>): Boolean {

                return true
            }

            override fun onPointClick(codeBean: CodeBean) {
                super.onPointClick(codeBean)
                val intent = Intent()
                intent.putExtra(SCAN_CODE_KEY, codeBean.codeString)
                setResult(RESULT_OK, intent)
                finish()
            }
        })
    }

    private fun transparentStatusBar() {
        val decorView: View = window.decorView
        decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.statusBarColor = Color.TRANSPARENT
    }

    override fun onDestroy() {
        super.onDestroy()
        scanView?.release()
    }
}