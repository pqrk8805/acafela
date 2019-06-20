package com.acafela.harmony.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.acafela.harmony.R;
import com.acafela.harmony.service.HarmonyService;

public class TestMainActivity extends AppCompatActivity {
    private static final String LOG_TAG = TestMainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testmain);
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
