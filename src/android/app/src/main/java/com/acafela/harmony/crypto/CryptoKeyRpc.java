package com.acafela.harmony.crypto;

import com.acafela.harmony.crypto.CryptoKeyOuterClass.Session;
import com.acafela.harmony.crypto.CryptoKeyOuterClass.Key;
import com.acafela.harmony.rpccomm.GrpcCommon;

import java.io.InputStream;

import io.grpc.ManagedChannel;

public class CryptoKeyRpc
{
    private static final String LOG_TAG = "CryptoKeyRpc";
    private ManagedChannel mChannel;

    public CryptoKeyRpc(
                    String address,
                    int port,
                    InputStream rootCertIS,
                    InputStream serverCertIS)
    {
        mChannel = GrpcCommon.initSecureChannel(
                                    address, port, rootCertIS, serverCertIS);
    }

    public byte[] getKey(String sessionId)
    {
        Session session = Session.newBuilder()
                                 .setId(sessionId)
                                 .build();
        Key key = CryptoKeyGrpc.newBlockingStub(mChannel).request(session);
        return key.getKey().toByteArray();
    }

    public void shutdown()
    {
        mChannel.shutdown();
    }
}
