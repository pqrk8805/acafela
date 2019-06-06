package com.acafela.harmony.service;

import android.util.Log;

import com.acafela.harmony.userprofile.UserProfileGrpc;
import com.acafela.harmony.userprofile.UserProfileGrpc.UserProfileBlockingStub;
import com.acafela.harmony.userprofile.UserProfileOuterClass.Empty;
import com.acafela.harmony.userprofile.UserProfileOuterClass.VersionInfo;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class UserProfile
{
    private static final String LOG_TAG = UserProfile.class.getName();

    private static final String SERVER_ADDRESS = "172.20.4.206";
    private static final int SERVER_PORT = 9000;
    private static final Empty EMPTY_MSG = Empty.getDefaultInstance();

    private ManagedChannel mChannel;


    public UserProfile()
    {
        Log.i(LOG_TAG, "UserProfile");
        connect();
    }


    private int connect()
    {
        mChannel = ManagedChannelBuilder
                            .forAddress(SERVER_ADDRESS, SERVER_PORT)
                            .usePlaintext()
                            .build();
        UserProfileBlockingStub blockingStub
                                    = UserProfileGrpc.newBlockingStub(mChannel);
        VersionInfo versionInfo = blockingStub.getVersion(EMPTY_MSG);
        Log.i(LOG_TAG, "Server Version: " + versionInfo.getVersion());

        return 0;
    }
}
