package com.zwj.scan

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.permissionx.guolindev.PermissionX
import com.permissionx.guolindev.callback.RequestCallback

class MainActivity : AppCompatActivity() {

    private var scanCodeTextView: TextView? = null
    private val scanActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (RESULT_OK == it.resultCode) {
                scanCodeTextView?.text = it.data?.getStringExtra(ScanActivity.SCAN_CODE_KEY)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        scanCodeTextView = findViewById(R.id.scan_code_tv)
        findViewById<Button>(R.id.to_scan).setOnClickListener {
            PermissionX.init(this).permissions(Manifest.permission.CAMERA)
                .request { allGranted, _, _ ->
                    if (allGranted) {
                        scanActivityResult.launch(Intent(this, ScanActivity::class.java))
                    } else {
                        Toast.makeText(this, "权限已拒绝，请开启权限", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

}



