package com.acafela.harmony.concall;

import android.util.Log;

import com.acafela.harmony.rpc.Common;
import com.acafela.harmony.rpccomm.GrpcCommon;

import java.util.List;

import io.grpc.ManagedChannel;

import static com.acafela.harmony.concall.ConCallReserveGrpc.newBlockingStub;
import com.acafela.harmony.concall.ConCallReserv.ConCallResrvInfo.*;

public class ConCallReservRpc
{
    private static final String LOG_TAG = "CC_RPC";
    private ManagedChannel mChannel;

    public ConCallReservRpc(
                        String address,
                        int port)
    {
        mChannel = GrpcCommon.initInsecureChannel(address, port)
    }

    public ConCallReserveGrpc.ConCallReserveBlockingStub getBlockingStub()
    {
        return newBlockingStub(mChannel);
    }

    public int reserve(
                    String hostPhoneNumber,
                    String from,
                    String to,
                    List<String> participants)
    {
        if (mChannel == null) {
            Log.e(LOG_TAG, "channel is initialized.");
            return -100;
        }

        Builder builder = ConCallReserv.ConCallResrvInfo.newBuilder()
                .setHostPhonenumber(hostPhoneNumber)
                .setFrom(from)
                .setT




        Common.Error err = getBlockingStub().reserve();
;
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
