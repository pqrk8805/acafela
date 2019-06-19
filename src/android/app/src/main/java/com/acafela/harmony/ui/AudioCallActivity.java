package com.acafela.harmony.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.acafela.harmony.R;
import com.acafela.harmony.service.HarmonyService;

import static com.acafela.harmony.ui.CallActivity.INTENT_CONTROL;
import static com.acafela.harmony.ui.CallActivity.INTENT_SIP_ACCEPT_CALL;
import static com.acafela.harmony.ui.CallActivity.INTENT_SIP_TERMINATE_CALL;

public class AudioCallActivity extends AppCompatActivity {
    private static final String TAG = AudioCallActivity.class.getName();

    public static final String INTENT_PHONENUMBER = "INTENT_PHONENUMBER";
    public static final String INTENT_ISRINGING = "INTENT_ISCALEE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_calling);

        Intent intent = getIntent();

        TextView phoneNumberTextView = findViewById(R.id.tv_phonenumber);
        phoneNumberTextView.setText(intent.getStringExtra(INTENT_PHONENUMBER));

        boolean isRinging = intent.getBooleanExtra(INTENT_ISRINGING, false);
        if (isRinging) {
            findViewById(R.id.fourth_container).setVisibility(View.GONE);
        }
        else {
            findViewById(R.id.fourth_container_ringing).setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onClickAcceptCallBtn(View v) {
        Intent serviceIntent = new Intent(getApplicationContext(), HarmonyService.class);
        serviceIntent.putExtra(INTENT_CONTROL, INTENT_SIP_ACCEPT_CALL);
        startService(serviceIntent);

        findViewById(R.id.fourth_container).setVisibility(View.VISIBLE);
        findViewById(R.id.fourth_container_ringing).setVisibility(View.GONE);
    }

    public void onClickEndCallBtn(View v) {
        Intent serviceIntent = new Intent(getApplicationContext(), HarmonyService.class);
        serviceIntent.putExtra(INTENT_CONTROL, INTENT_SIP_TERMINATE_CALL);
        startService(serviceIntent);

        finish();
    }
}