package com.zwj.scan

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.withCreated
import com.gyf.immersionbar.ImmersionBar
import com.palmpay.scan.bean.CodeBean
import com.palmpay.scan.bean.ScanMode
import com.palmpay.scan.bean.ScanType
import com.palmpay.scan.callback.OnScanListener
import com.palmpay.scan.view.ScanView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {
    private lateinit var scanView: ScanView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        ImmersionBar.with(this).transparentBar().init()
        scanView = findViewById(R.id.scan_view)
        scanView.setScanMode(ScanMode.MANY)
        scanView.setScanType(ScanType.SCAN_FULL_SCREEN)
        scanView.setOnScanListener(object : OnScanListener {

            override fun onResult(result: List<CodeBean>): Boolean {
                Log.e(">>>>>>", (result[0].codeString))
               // finish()
                return true
            }
        })
    }

    override fun onStop() {
        super.onStop()
        scanView.release()
    }

}



