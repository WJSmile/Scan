package com.zwj.scan;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.permissionx.guolindev.PermissionX;


public class TestActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity);
        findViewById(R.id.button).setOnClickListener(view -> {
            PermissionX.init(this).permissions(Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE).request((allGranted, grantedList, deniedList) -> {
                if (allGranted) {
                    startActivity(new Intent(TestActivity.this, MainActivity.class));
                }
            });
        });

    }
}
