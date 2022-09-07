package com.zwj.scan

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import com.gyf.immersionbar.ImmersionBar
import com.palmpay.scan.bean.CodeBean
import com.palmpay.scan.bean.ScanMode
import com.palmpay.scan.callback.OnScanListener
import com.palmpay.scan.view.ScanView


class MainActivity : AppCompatActivity() {
    private lateinit var scanView: ScanView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        ImmersionBar.with(this).transparentBar().init()
        scanView = findViewById(R.id.scan_view)
        scanView.setScanMode(ScanMode.SIMPLE)
        scanView.setOnScanListener(object : OnScanListener {

            override fun onResult(result: List<CodeBean>): Boolean {
                Log.e(">>>>>>", (result[0].codeString))
                return true
            }
        })
    }

    override fun onStop() {
        super.onStop()
        scanView.release()
    }

}



