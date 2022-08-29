package com.zwj.scan;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.palmpay.scan.bean.ScanCodeType;
import com.palmpay.scan.utils.CodeUtils;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;

import java.util.List;

public class TestActivity extends AppCompatActivity {

    private ImageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity);
        imageView = findViewById(R.id.image);
        findViewById(R.id.button).setOnClickListener(view -> {
            PermissionX.init(this).permissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE).request((allGranted, grantedList, deniedList) -> {
                if (allGranted) {
                    startActivity(new Intent(TestActivity.this, MainActivity.class));

                }
            });
        });
        PermissionX.init(this).permissions( Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE).request((allGranted, grantedList, deniedList) -> {
            if (allGranted) {
                new Thread(() -> {
                    Bitmap bitmap = CodeUtils.INSTANCE.create("Hello Word", 200, 200, ScanCodeType.Aztec, "UTF-8", -1, 0, Color.BLACK, Color.WHITE);
                    runOnUiThread(() -> {
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                        }
                    });
                }).start();
            }
        });


    }
}
