package com.acafela.harmony.directoryservice;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.acafela.harmony.Config;
import com.acafela.harmony.R;
import com.acafela.harmony.dirserv.DirectoryServiceGrpc.DirectoryServiceBlockingStub;
import com.acafela.harmony.dirserv.DirectoryServiceOuterClass.DirInfo;
import com.acafela.harmony.rpc.Common;
import com.acafela.harmony.userprofile.UserInfo;
import com.acafela.harmony.util.ConfigSetup;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.grpc.StatusRuntimeException;

import static android.content.Context.WIFI_SERVICE;

public class DirectoryService {
    private static final String TAG = DirectoryService.class.getName();
    private static final int NUM_OF_RETRY = 3;

    private Context mContext;

    public DirectoryService(Context context) {
        mContext = context;
    }

    public void update() {
        String LocalIP = "";
        int LocalIpAddressBin = 0;
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            LocalIpAddressBin = wifiInfo.getIpAddress();
            LocalIP = String.format(Locale.US, "%d.%d.%d.%d", (LocalIpAddressBin & 0xff), (LocalIpAddressBin >> 8 & 0xff), (LocalIpAddressBin >> 16 & 0xff), (LocalIpAddressBin >> 24 & 0xff));
        }

        // When phone is Wifi Hot spot, wifiInfo.getIpAddress() returns "0.0.0.0"
        // But, default IP address of hotspot is "192.168.43.1".
        if (LocalIP.compareTo("0.0.0.0") == 0) {
            LocalIP = "192.168.43.1";
        }

        Log.i(TAG, "update directoryService: " + LocalIP);
        DirectoryServiceRpc directoryServiceRpc = new DirectoryServiceRpc(
                ConfigSetup.getInstance().getServerIP(mContext),
                Config.RPC_PORT_DIRECTORY_SERVICE,
                mContext.getResources().openRawResource(R.raw.ca),
                mContext.getResources().openRawResource(R.raw.server));

        DirectoryServiceBlockingStub blockingStub = directoryServiceRpc.getBlockingStub();

        DirInfo info = DirInfo.newBuilder()
                        .setAddress(LocalIP)
                        .setPhoneNumber(UserInfo.getInstance().getPhoneNumber())
                        .build();
        for (int i = 0; i < NUM_OF_RETRY; ++i) {
            try {
                Common.Error error = blockingStub
                            .withDeadlineAfter(1, TimeUnit.SECONDS)
                            .update(info);
                Log.d(TAG, "RPC Result(" + error.getErr() + "): " + error.toString());
                if (error.getErr() == 0)
                    break;
            } catch (StatusRuntimeException e) {
                Log.w(TAG, "DirectoryService Update is Interrupted");
            }
        }
        directoryServiceRpc.shutdown();
    }
}
