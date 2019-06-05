package com.acafela.harmony.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.acafela.harmony.R;
import com.acafela.harmony.service.HarmonyService;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getName();

    private static final String SERVERIPAddr = "192.168.1.6";

    private static final int PERMISSION_ALL_ID = 1;
    private static final String[] PERMISSIONS = {
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions();
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

    public void onClickRegisterUserBtn(View v) {
        Log.d(LOG_TAG, "onClickRegisterUserBtn");

        Toast.makeText(getApplicationContext(), "Not Implemented Register User", Toast.LENGTH_SHORT).show();
    }

    public void onClickCallBtn(View v) {
        Log.d(LOG_TAG, "onClickCallBtn");

        Intent intent = new Intent(this, CallActivity.class);
        startActivity(intent);
    }

    public void onClickSendBtn(View v)
    {
        WifiManager wifiMan = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        int ipAddress = wifiInf.getIpAddress();
        String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),(ipAddress >> 8 & 0xff),(ipAddress >> 16 & 0xff),(ipAddress >> 24 & 0xff));
        Toast.makeText(this, ip, Toast.LENGTH_LONG).show();
        sendMessage(ip);
    }

    private void sendMessage(final String message) {
        final Handler handler = new Handler();
        Thread thread = new Thread(new Runnable() {
            String stringData;
            @Override
            public void run() {
                DatagramSocket ds = null;
                try {
                    ds = new DatagramSocket();
                    InetAddress serverAddr = InetAddress.getByName(SERVERIPAddr);
                    DatagramPacket dp;
                    dp = new DatagramPacket(message.getBytes(), message.length(), serverAddr, 5000);
                    ds.send(dp);

                    byte[] lMsg = new byte[1000];
                    dp = new DatagramPacket(lMsg, lMsg.length);
                    ds.receive(dp);
                    stringData = new String(lMsg, 0, dp.getLength());
                    final TextView TESTTextView = findViewById(R.id.TESTTextView);
                    TESTTextView.post(new Runnable() {
                        public void run() {
                            TESTTextView.setText("RCV DATA : " + stringData);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (ds != null) {
                        ds.close();
                    }
                }
            }
        });
        thread.start();
    }

    public void onClickTestEncodingBtn(View v) {
        Intent intent = new Intent(this, TestEncodingActivity.class);
        startActivity(intent);
    }
}
