package com.acafela.harmony.directoryservice;

import android.util.Log;

import com.acafela.harmony.dirserv.DirectoryServiceGrpc;
import com.acafela.harmony.dirserv.DirectoryServiceGrpc.DirectoryServiceBlockingStub;
import com.acafela.harmony.dirserv.DirectoryServiceOuterClass.DirInfo;
import com.acafela.harmony.rpc.Common;
import com.acafela.harmony.rpccomm.GrpcCommon;

import java.io.InputStream;

import io.grpc.ManagedChannel;


public class DirectoryServiceRpc
{
    private static final String LOG_TAG = "DS_RPC";
    private ManagedChannel mChannel;

    public DirectoryServiceRpc(
                        String address,
                        int port,
                        InputStream rootCertIS,
                        InputStream serverCertIS)
    {
        mChannel = GrpcCommon.initSecureChannel(
                address, port, rootCertIS, serverCertIS);
    }

    public DirectoryServiceBlockingStub getBlockingStub()
    {
        return DirectoryServiceGrpc.newBlockingStub(mChannel);
    }

    public int updateInfo(
                        String phoneNumber,
                        String password,
                        String address)
    {
        if (mChannel == null) {
            Log.e(LOG_TAG, "channel is initialized.");
            return -100;
        }

        DirInfo info = DirInfo.newBuilder()
                              .setPhoneNumber(phoneNumber)
                              .setPassword(password)
                              .setAddress(address)
                              .build();
        Common.Error err = DirectoryServiceGrpc.newBlockingStub(mChannel)
                                               .update(info);
        return err.getErr();
    }

    public void shutdown()
    {
        if (mChannel != null) {
            mChannel.shutdown();
            mChannel = null;
        }
    }

}

