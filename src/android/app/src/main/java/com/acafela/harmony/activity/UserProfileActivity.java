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

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.okhttp.OkHttpChannelBuilder;

import org.conscrypt.Conscrypt;

import java.security.KeyStore;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

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
            //Security.insertProviderAt(Conscrypt.newProvider(), 1);

            /*
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate cert = cf.generateCertificate(theRawCert);
            ks.setCertificateEntry("customca", cert);

            TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(ks);
            SSLContext context = SSLContext.getInstance("TLS", provider);
            context.init(null, trustManagerFactory.getTrustManagers(), null);
            SSLSocketFactory factory = context.getSocketFactory();
            */

            Log.i(LOG_TAG, serverAddress);
            mChannel = ManagedChannelBuilder
                                    .forAddress(serverAddress, SERVER_PORT)
                                    .usePlaintext()
                                    .build();
            /*
            mChannel = OkHttpChannelBuilder
                                    .forAddress(serverAddress, SERVER_PORT)
                                    .sslSocketFactory(factory)
                                    .build();
            */

            UserProfileBlockingStub blockingStub
                                    = UserProfileGrpc.newBlockingStub(mChannel);

            // just test to connection
            //
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
