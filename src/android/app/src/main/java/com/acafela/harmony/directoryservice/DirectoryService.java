package com.acafela.harmony.directoryservice;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.acafela.harmony.Config;
import com.acafela.harmony.R;
import com.acafela.harmony.dirserv.DirectoryServiceGrpc;
import com.acafela.harmony.dirserv.DirectoryServiceGrpc.DirectoryServiceBlockingStub;
import com.acafela.harmony.dirserv.DirectoryServiceOuterClass;
import com.acafela.harmony.rpc.Common;
import com.acafela.harmony.userprofile.UserInfo;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.grpc.StatusRuntimeException;

import static android.content.Context.WIFI_SERVICE;

public class DirectoryService {
    private static final String TAG = DirectoryService.class.getName();

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

        DirectoryServiceRpc directoryServiceRpc = new DirectoryServiceRpc(
                Config.SERVER_IP,
                Config.RPC_PORT_DIRECTORY_SERVICE,
                mContext.getResources().openRawResource(R.raw.ca),
                mContext.getResources().openRawResource(R.raw.server));

        DirectoryServiceBlockingStub blockingStub = directoryServiceRpc.getBlockingStub();

        Common.Error error = null;
        try {
            error = blockingStub.withDeadlineAfter(2, TimeUnit.SECONDS).update(DirectoryServiceOuterClass.DirInfo.newBuilder().
                    setAddress(LocalIP).
                    setPhoneNumber(UserInfo.getInstance().getPhoneNumber()).
                    build());

            Log.e(TAG, error.toString());
        } catch (StatusRuntimeException e) {
            Log.i(TAG, "DirectoryService Update is Interrupted");
        }
        directoryServiceRpc.shutdown();
    }
}
