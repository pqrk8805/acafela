package com.acafela.harmony;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.acafela.harmony.service.HarmonyService;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getName();

    public static final String INTENT_CONTROL = "CONTROL";
    public static final String INTENT_INITIATE_CALL = "INITIATE_CALL";
    public static final String INTENT_TERMINATE_CALL = "TERMINATE_CALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClickRegisterUserBtn(View v) {
        Log.d(LOG_TAG, "onClickRegisterUserBtn");

        Toast.makeText(getApplicationContext(), "Not Implemented Register User", Toast.LENGTH_SHORT).show();
    }

    public void onClickInitiateCallBtn(View v) {
        Log.d(LOG_TAG, "onClickInitiateCallBtn");

        Intent intent = new Intent(getApplicationContext(), HarmonyService.class);
        intent.putExtra(INTENT_CONTROL, INTENT_INITIATE_CALL);
        startService(intent);
    }

    public void onClickTerminateCallBtn(View v) {
        Log.d(LOG_TAG, "onClickTerminateCallBtn");

        Intent intent = new Intent(getApplicationContext(), HarmonyService.class);
        intent.putExtra(INTENT_CONTROL, INTENT_TERMINATE_CALL);
        startService(intent);
    }
}
