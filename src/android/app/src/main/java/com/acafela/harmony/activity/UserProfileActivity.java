package com.acafela.harmony.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.acafela.harmony.R;
import com.acafela.harmony.userprofile.UserProfileGrpc;
import com.acafela.harmony.userprofile.UserProfileGrpc.UserProfileBlockingStub;
import com.acafela.harmony.userprofile.UserProfileOuterClass.Empty;
import com.acafela.harmony.userprofile.UserProfileOuterClass.VersionInfo;

import javax.net.ssl.SSLSocketFactory;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.okhttp.OkHttpChannelBuilder;

public class UserProfileActivity extends AppCompatActivity
{
    private static final String LOG_TAG = "UserProfile_Act";
    private static final int SERVER_PORT = 9000;
    private static final Empty EMPTY_MSG = Empty.getDefaultInstance();

    private ManagedChannel mChannel = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
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

        try {
            mChannel = ManagedChannelBuilder
                                    .forAddress(serverAddress, SERVER_PORT)
                                    .usePlaintext()
                                    .build();
            UserProfileBlockingStub blockingStub
                                    = UserProfileGrpc.newBlockingStub(mChannel);

            // just test to connection
            //
            VersionInfo versionInfo = blockingStub.getVersion(EMPTY_MSG);
            Log.i(LOG_TAG, "Server Version: " + versionInfo.getVersion());
            TextView tv = findViewById(R.id.txtUserProfileServerVersion);
            tv.setText(versionInfo.getVersion());
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
