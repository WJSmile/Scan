package com.zwj.scan

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.gyf.immersionbar.ImmersionBar
import com.palmpay.scan.bean.CodeBean
import com.palmpay.scan.callback.OnScanListener
import com.palmpay.scan.view.ScanView
import java.nio.charset.Charset


class MainActivity : AppCompatActivity() {
    private lateinit var scanView: ScanView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        ImmersionBar.with(this).transparentBar().init()
        scanView = findViewById(R.id.scan_view)

        scanView.setOnScanListener(object :OnScanListener{

            override fun onResult(result: List<CodeBean>) {
                Log.e(">>>>>>",result[0].codeString)
                //finish()
            }

        })
    }

    override fun onDestroy() {
        super.onDestroy()
        scanView.release()
    }

}



