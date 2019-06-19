package com.acafela.harmony.controller;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import com.acafela.harmony.Config;
import com.acafela.harmony.R;
import com.acafela.harmony.crypto.CryptoKeyRpc;
import com.acafela.harmony.crypto.ICrypto;
import com.acafela.harmony.communicator.*;
import com.acafela.harmony.sip.SipMessage;
import com.acafela.harmony.sip.SipMessage.SIPMessage;
import com.acafela.harmony.crypto.Crypto;
import com.acafela.harmony.crypto.CryptoBroker;
import com.acafela.harmony.userprofile.UserInfo;
import io.grpc.ManagedChannel;

public class VoipController {
    public static final int CONTROL_SEND_PORT = 5000;
    public static final int CONTROL_RECIEVE_PORT = 5001;
    private static final String LOG_TAG = "[AcafelaController]";
    private static final int BUFFER_SIZE = 128;
    private boolean UdpListenerThreadRun = false;
    private DatagramSocket socket;
    private  InetAddress mIpAddress;

    private Context mContext;
    private int sesssionNo=0;
    private String sesssionID;
    private int msgSeq =0;
    private ICrypto mCrypto;
    private boolean isCaller =false;
    private boolean isRun =false;
    RingController mRingControl = null;
    private CryptoKeyRpc mCryptoRpc;
    private List<DataCommunicator> mSessionList;
    private String mCalleeNumber;
    private String mCallerNumber;


    public VoipController(Context context)
    {
        mSessionList = new ArrayList<DataCommunicator>();
        mContext = context;
        mRingControl= new RingController(mContext);
        mCryptoRpc = new CryptoKeyRpc(
                        Config.SERVER_IP,
                        Config.RPC_PORT_CRYPTO_KEY,
                        context.getResources().openRawResource(R.raw.ca),
                        context.getResources().openRawResource(R.raw.server));
        try {
            this.mIpAddress = InetAddress.getByName(Config.SERVER_IP);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception Answer Message: " + e);
            return ;
        }
    }

    public void startListenerController() {
        if(isCaller ==true) return;

        Crypto.init();
        UdpListenerThreadRun = true;
        Thread UDPListenThread = new Thread(new Runnable() {
            public void run() {
                try {
                    // Setup the socket to receive incoming messages
                    byte[] buffer = new byte[1024];
                    socket = new DatagramSocket(null);
                    socket.setReuseAddress(true);
                    socket.bind(new InetSocketAddress(CONTROL_RECIEVE_PORT));
                    DatagramPacket packet = new DatagramPacket(buffer, BUFFER_SIZE);
                    Log.i(LOG_TAG, "Incoming call listener started");
                    while (UdpListenerThreadRun) {
                        // Listen for incoming call requests
                        //Log.i(LOG_TAG, "Listening for incoming calls");
                        socket.receive(packet);
                        String senderIP = packet.getAddress().getHostAddress();
                        byte[] data = new byte[packet.getLength()];
                        System.arraycopy(packet.getData(), packet.getOffset(),data, 0, packet.getLength());
                        SIPMessage sipMessage = SIPMessage.parseFrom(data);

                       // String message = new String(buffer, 0, packet.getLength());
                        Log.e(LOG_TAG, "Got UDP message from " + senderIP + ", message: " + sipMessage.toString());
                        handle(sipMessage);
                        //broadcastIntent(senderIP, message);
                    }
                    Log.e(LOG_TAG, "Call Listener ending");
                    socket.disconnect();
                    socket.close();

                } catch (Exception e) {
                    UdpListenerThreadRun = false;
                    e.printStackTrace();
                }
            }
        });
        UDPListenThread.start();
        isRun = true;
    }

    private void handle( final SIPMessage message) {
        if(isCaller) {
            switch (message.getCmd()) {
                case RINGING:
                    Log.i(LOG_TAG, "Listening for ringing calls");
                    mRingControl.ringbackTone_start();
                    break;
                case ACCEPTCALL:
                    Log.i(LOG_TAG, "Listening for accept calls");
                    mRingControl.ringbackTone_stop();
                    break;
                case OPENSESSION:
                    byte[] keyByte = mCryptoRpc.getKey(sesssionID);
                    mCrypto  = CryptoBroker.getInstance().create("AES");
                    mCrypto.init(keyByte);

                    for(int i=0;i<message.getSessioninfo().getSessionsCount();i++) {
                        SipMessage.Session session = message.getSessioninfo().getSessions(i);
                        opensession(session.getSessiontype(), session.getIp(), session.getPort());
                    }
                    break;
                case BYE:
                    mRingControl.allStop();
                    destroyAllsession();
                    isCaller = false;
                    break;
                case STARTVIDEO:
                case STOPVIDEO:
                case TERMINATE:
                case MAKECALL:
                case INVITE:
                case UNRECOGNIZED:
                    break;
            }
        }
        else {
            switch (message.getCmd()) {
                case INVITE:
                    sesssionID = message.getSessionid();
                    mCallerNumber = message.getFrom();
                    mCalleeNumber = message.getTo();
                    mRingControl.ring_start();
                    sendMessage(SipMessage.Command.RINGING);
                    break;
                case OPENSESSION:

                    byte[] keyByte = mCryptoRpc.getKey(sesssionID);
                    mCrypto = CryptoBroker.getInstance().create("AES");
                    Log.e(LOG_TAG, "Send Message: " +"keyByte" + keyByte.length);
                    mCrypto.init(keyByte);
                    for(int i=0;i<message.getSessioninfo().getSessionsCount();i++) {
                        SipMessage.Session session = message.getSessioninfo().getSessions(i);
                        opensession(session.getSessiontype(), session.getIp(), session.getPort());
                    }
                    break;
                case BYE:
                    mRingControl.allStop();
                    destroyAllsession();
                case STARTVIDEO:
                case STOPVIDEO:
                case TERMINATE:
                case MAKECALL:
                case ACCEPTCALL:
                case UNRECOGNIZED:
                    break;
            }
        }
    }
    void opensession(SipMessage.SessionType type, String ip, int port)
    {
        Log.e(LOG_TAG, "Send Message: "+ type +"ip" +ip +"port"+ port);
        DataCommunicator communicator = null;
        switch(type)
        {
            case SENDAUDIO:
                communicator = new SenderAudio(mCrypto);
                break;
            case SENDVIDEO:
                communicator = new SenderVideo();
                break;
            case RECIEVEAUDIO:
                communicator = new ReceiverAudio(mContext, mCrypto);
                break;
            case RECIEVEVIDEO:
                communicator = new ReceiverVideo(mContext);
                break;
            case UNRECOGNIZED:
                return;
        }
        communicator.setSession(ip, port);
        communicator.startCommunicator();
        mSessionList.add(communicator);
    }
    void destroyAllsession()
    {
        for(DataCommunicator session:mSessionList)
            session.endCommunicator();
        mSessionList.clear();
    }
//////////////////////////////////////////////
    private void UdpSend( final byte[] buffer) {
        Thread replyThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramSocket socket = new DatagramSocket();
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, mIpAddress, CONTROL_SEND_PORT);
                    socket.send(packet);
                    //Log.e(LOG_TAG, "Send Message: "+ mIpAddress);
                    socket.disconnect();
                    socket.close();
                } catch (SocketException e) {
                    Log.e(LOG_TAG, "Failure. SocketException in UdpSend: " + e);
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Failure. IOException in UdpSend: " + e);
                }
            }
        });
        replyThread.start();
    }
    public void  sendMessage(SipMessage.Command cmd)
    {

        SIPMessage.Builder builder = SIPMessage.newBuilder();
        byte[] buffer = builder.
                setCmd(cmd).
                setFrom(mCallerNumber).
                setTo(mCalleeNumber).
                setSessionid(sesssionID).
                setSeq(msgSeq).
                build().
                toByteArray();
         //Log.e(LOG_TAG, "Exception Answer Message: " + buffer.length);
         UdpSend(buffer);
    }

    public void inviteCall(String calleeNumber)
    {
        //Add exception state
        mCalleeNumber = calleeNumber;
        mCallerNumber = UserInfo.getInstance().getPhoneNumber();
        sesssionID = UserInfo.getInstance().getPhoneNumber() + sesssionNo++;
        isCaller = true;

        sendMessage(SipMessage.Command.INVITE);
    }
    public void terminateCall()
    {
        //Add exception state
        sendMessage(SipMessage.Command.TERMINATE);
    }
    public void acceptCall()
    {
        if(isCaller) return;
        mRingControl.ring_stop();
        sendMessage(SipMessage.Command.ACCEPTCALL);
    }
}
