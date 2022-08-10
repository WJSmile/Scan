package com.zwj.scan

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gyf.immersionbar.ImmersionBar
import com.palmpay.scan.view.ScanView

class MainActivity : AppCompatActivity() {
    private lateinit var scanView: ScanView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        ImmersionBar.with(this).transparentBar().init()
        scanView = findViewById(R.id.scan_view)
        scanView.setOnCodeResultListener {
            if (it.size == 1) {
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scanView.release()
    }

}



