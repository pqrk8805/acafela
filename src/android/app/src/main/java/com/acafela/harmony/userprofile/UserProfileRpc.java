package com.acafela.harmony.userprofile;

import com.acafela.harmony.rpccomm.GrpcCommon;
import com.acafela.harmony.userprofile.UserProfileGrpc.UserProfileBlockingStub;

import java.io.InputStream;

import io.grpc.ManagedChannel;


public class UserProfileRpc
{
    private static final String LOG_TAG = "UserProfileRpc";
    private ManagedChannel mChannel;

    public UserProfileRpc(
                        String address,
                        int port,
                        InputStream rootCertIS,
                        InputStream serverCertIS)
    {
        mChannel = GrpcCommon.initSecureChannel(
                                    address, port, rootCertIS, serverCertIS);
    }

    public UserProfileBlockingStub getBlockingStub()
    {
        return UserProfileGrpc.newBlockingStub(mChannel);
    }
}
