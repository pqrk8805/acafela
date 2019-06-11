package com.acafela.harmony.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.acafela.harmony.R;
import com.acafela.harmony.service.HarmonyService;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CallActivity extends AppCompatActivity {
    private static final String LOG_TAG = CallActivity.class.getName();

    public static final String INTENT_CONTROL = "CONTROL";
    public static final String INTENT_SERVERIP = "SERVER";
    public static final String INTENT_SERVERSENDPORT = "SERVERSENDPORT";
    public static final String INTENT_SERVERRCVPORT = "SERVERRCVPORT";
    public static final String INTENT_INITIATE_CALL = "INITIATE_CALL";
    public static final String INTENT_TERMINATE_CALL = "TERMINATE_CALL";
    public static final String INTENT_SIP_INVITE_CALL = "SIPINVITE";
    public static final String INTENT_SIP_ACCEPT_CALL = "SIPACCEPT";
    public static final String INTENT_SIP_TERMINATE_CALL = "SIPTERMINATE";
    private static final Pattern IP_ADDRESS
            = Pattern.compile(
            "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                    + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                    + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                    + "|[1-9][0-9]|[0-9]))");

    private TextView mTextViewLocalIP;
    private EditText mRemoteIpText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        mTextViewLocalIP = findViewById(R.id.textViewLocalIp);
        mRemoteIpText = findViewById(R.id.editTextRemoteIp);

        String LocalIP = "";
        int LocalIpAddressBin = 0;
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            LocalIpAddressBin = wifiInfo.getIpAddress();
            LocalIP = String.format(Locale.US, "%d.%d.%d.%d", (LocalIpAddressBin & 0xff), (LocalIpAddressBin >> 8 & 0xff), (LocalIpAddressBin >> 16 & 0xff), (LocalIpAddressBin >> 24 & 0xff));
            mTextViewLocalIP.setText(getString(R.string.LocalIp, LocalIP));
        }

        String BaseIP = String.format(Locale.US, "%d.%d.%d.", (LocalIpAddressBin & 0xff), (LocalIpAddressBin >> 8 & 0xff), (LocalIpAddressBin >> 16 & 0xff));
        mRemoteIpText.setText(BaseIP);
        int position = mRemoteIpText.length();
        mRemoteIpText.setSelection(position);
    }

    public void onClickInitiateCallBtn(View v) {
        Log.d(LOG_TAG, "onClickInitiateCallBtn");

        String RemoteIP = "";
        Matcher matcher = IP_ADDRESS.matcher(mRemoteIpText.getText().toString());
        if (matcher.matches()) {
            RemoteIP = mRemoteIpText.getText().toString();
        } else {
            final AlertDialog alert = new AlertDialog.Builder(CallActivity.this).create();
            alert.setTitle("Invailid IP");
            alert.setMessage("The IP Addresss you entered is invalid!");
            alert.setButton(-1, "Dismiss", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    alert.dismiss();
                }
            });
            alert.show();
        }
        int remoteSNDPort = Integer.parseInt(((TextView)findViewById(R.id.editTextRemoteSendPort)).getText().toString());
        int remoteRCVPort = Integer.parseInt(((TextView)findViewById(R.id.editTextRemoteRcvPort)).getText().toString());
        Intent intent = new Intent(getApplicationContext(), HarmonyService.class);
        intent.putExtra(INTENT_CONTROL, INTENT_INITIATE_CALL);
        intent.putExtra(INTENT_SERVERIP, RemoteIP);
        intent.putExtra(INTENT_SERVERSENDPORT, remoteSNDPort);
        intent.putExtra(INTENT_SERVERRCVPORT, remoteRCVPort);
        startService(intent);
    }

    public void onClickTerminateCallBtn(View v) {
        Log.d(LOG_TAG, "onClickTerminateCallBtn");

        Intent intent = new Intent(getApplicationContext(), HarmonyService.class);
        intent.putExtra(INTENT_CONTROL, INTENT_TERMINATE_CALL);
        startService(intent);
    }

    public void onClickSipInviteCallBtn(View v) {
        Log.d(LOG_TAG, "onClickTerminateCallBtn");

        Intent intent = new Intent(getApplicationContext(), HarmonyService.class);
        intent.putExtra(INTENT_CONTROL, INTENT_SIP_INVITE_CALL);
        startService(intent);
    }
    public void onClickSipAcceptCallBtn(View v) {
        Log.d(LOG_TAG, "onClickTerminateCallBtn");

        Intent intent = new Intent(getApplicationContext(), HarmonyService.class);
        intent.putExtra(INTENT_CONTROL, INTENT_SIP_ACCEPT_CALL);
        startService(intent);
    }
    public void onClickSipTerminateCallBtn(View v) {
        Log.d(LOG_TAG, "onClickTerminateCallBtn");

        Intent intent = new Intent(getApplicationContext(), HarmonyService.class);
        intent.putExtra(INTENT_CONTROL, INTENT_SIP_TERMINATE_CALL);
        startService(intent);
    }
}
