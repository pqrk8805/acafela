package com.acafela.harmony.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.acafela.harmony.R;
import com.acafela.harmony.service.HarmonyService;
import com.acafela.harmony.userprofile.UserInfo;

public class TestMainActivity extends AppCompatActivity {
    private static final String LOG_TAG = TestMainActivity.class.getName();

    private static final int PERMISSION_ALL_ID = 1;
    private static final String[] PERMISSIONS = {
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testmain);

        requestPermissions();
        UserInfo.getInstance().load(this);
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void requestPermissions() {
        if(!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this,
                    PERMISSIONS,
                    PERMISSION_ALL_ID);
        }
    }

    public void onClickCallBtn(View v) {
        Log.d(LOG_TAG, "onClickCallBtn");

        Intent serviceIntent = new Intent(getApplicationContext(), HarmonyService.class);
        startService(serviceIntent);

        Intent intent = new Intent(this, CallActivity.class);
        startActivity(intent);
    }

    public void onClickTestVideoEncodingBtn(View v)
    {
        Intent intent = new Intent(this, TestVideoEncodingActivity.class);
        startActivity(intent);
    }

    public void onClickTestEncodingBtn(View v) {
        Intent intent = new Intent(this, TestEncodingActivity.class);
        startActivity(intent);
    }

    public void onClickLaunchMainBtn(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void onClickTestAsyncEncodingBtn(View v) {
        Intent intent = new Intent(this, TestAsyncEncodingActivity.class);
        startActivity(intent);
    }
}
