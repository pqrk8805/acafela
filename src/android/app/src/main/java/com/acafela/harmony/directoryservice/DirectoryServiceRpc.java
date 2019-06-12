package com.acafela.harmony.directoryservice;

import com.acafela.harmony.dirserv.DirectoryServiceGrpc;
import com.acafela.harmony.dirserv.DirectoryServiceGrpc.DirectoryServiceBlockingStub;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class DirectoryServiceRpc
{
    private static final String LOG_TAG = "DirectoryServiceRpc";
    private ManagedChannel mChannel;

    public DirectoryServiceRpc(
                        String address,
                        int port)
    {
        initInSecureChannel(address, port);
    }

    public DirectoryServiceBlockingStub getBlockingStub()
    {
        return DirectoryServiceGrpc.newBlockingStub(mChannel);
    }

    public void shutdown()
    {
        if (mChannel != null) {
            mChannel.shutdown();
            mChannel = null;
        }
    }

    private void initInSecureChannel(
                                String address,
                                int port)
    {
        mChannel = ManagedChannelBuilder
                                    .forAddress(address, port)
                                    .usePlaintext()
                                    .build();

    }
}

