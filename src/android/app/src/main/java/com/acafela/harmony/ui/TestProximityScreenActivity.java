package com.acafela.harmony.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.acafela.harmony.R;
import com.acafela.harmony.util.ProximityScreenController;


public class TestProximityScreenActivity extends AppCompatActivity
{
    private static final String LOG_TAG = "TEST_ProxiScr";
    private ProximityScreenController mController;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_proximityscreen);

        mController = new ProximityScreenController(this);

        getWindow();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mController.activate();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        mController.deactivate();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

}
