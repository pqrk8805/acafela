package com.acafela.harmony.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.acafela.harmony.R;

public class AudioCallActivity extends AppCompatActivity {
    private static final String TAG = AudioCallActivity.class.getName();

    public static final String INTENT_PHONENUMBER = "INTENT_PHONENUMBER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);

        Intent intent = getIntent();

        TextView phoneNumberTextView = findViewById(R.id.tv_phonenumber);
        phoneNumberTextView.setText(intent.getStringExtra(INTENT_PHONENUMBER));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onClickEndCallBtn(View v) {
        finish();
    }
}
