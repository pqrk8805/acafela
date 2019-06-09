package com.acafela.harmony.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.acafela.harmony.R;
import com.acafela.harmony.crypto.CryptoAES;
import com.acafela.harmony.crypto.CryptoFactory;
import com.acafela.harmony.userprofile.UserProfileGrpc.UserProfileBlockingStub;
import com.acafela.harmony.userprofile.UserProfileOuterClass.Empty;
import com.acafela.harmony.userprofile.UserProfileOuterClass.VersionInfo;
import com.acafela.harmony.userprofile.UserProfileRpc;

public class UserProfileActivity extends AppCompatActivity
{
    private static final String LOG_TAG = "UserProfile_Act";
    private static final int SERVER_PORT = 9000;
    private static final Empty EMPTY_MSG = Empty.getDefaultInstance();

    private UserProfileRpc mUserProfileRpc = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        com.acafela.harmony.crypto.ICrypto crypto = CryptoFactory.create("AES");
        crypto.init("12345".getBytes());
        byte[] encrypted = crypto.encrypt("abcdefg".getBytes());
        Log.d(LOG_TAG, "encrypted: " + new String(encrypted));

        byte[] plane = crypto.decrypt(encrypted);
        Log.d(LOG_TAG, "decrypted: " + new String(plane));

    }

    public void onClickConnectBtn(View v)
    {
        Log.d(LOG_TAG, "onClickConnectBtn");

        EditText ed = findViewById(R.id.editUserProfileServerAddress);
        String serverAddress = ed.getText().toString();
        if (serverAddress == null || serverAddress.length() == 0) {
            Log.d(LOG_TAG, "IP address is empty");
            return;
        }

        mUserProfileRpc = new UserProfileRpc(
                                serverAddress,
                                SERVER_PORT,
                                getResources().openRawResource(R.raw.ca),
                                getResources().openRawResource(R.raw.server));
        try {
            // just test to connection
            //
            UserProfileBlockingStub blockingStub
                                        = mUserProfileRpc.getBlockingStub();
            VersionInfo versionInfo = blockingStub.getVersion(EMPTY_MSG);
            Log.i(LOG_TAG, "Server Version: " + versionInfo.getVersion());
            TextView tv = findViewById(R.id.txtUserProfileServerVersion);
            tv.setText(versionInfo.getVersion());
        } catch (Exception e) {
            Log.e(LOG_TAG, "UNAVAILABLE", e);
            Toast.makeText(this, "UNAVAILABLE", Toast.LENGTH_LONG).show();
        }
    }
}
