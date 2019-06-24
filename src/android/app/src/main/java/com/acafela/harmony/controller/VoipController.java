package com.acafela.harmony.controller;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.acafela.harmony.Config;
import com.acafela.harmony.R;
import com.acafela.harmony.crypto.CryptoKeyRpc;
import com.acafela.harmony.crypto.ICrypto;
import com.acafela.harmony.communicator.*;
import com.acafela.harmony.sip.SipMessage;
import com.acafela.harmony.sip.SipMessage.SIPMessage;
import com.acafela.harmony.crypto.Crypto;
import com.acafela.harmony.crypto.CryptoBroker;
import com.acafela.harmony.ui.AudioCallActivity;
import com.acafela.harmony.userprofile.UserInfo;


import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.acafela.harmony.sip.SipMessage.Command.*;
import static com.acafela.harmony.ui.AudioCallActivity.BROADCAST_BYE;
import static com.acafela.harmony.ui.AudioCallActivity.INTENT_ISCALLEE;
import static com.acafela.harmony.ui.AudioCallActivity.INTENT_PHONENUMBER;

public class VoipController {
    public static final int CONTROL_SEND_PORT = 5000;
    public static final int CONTROL_RECIEVE_PORT = 5001;
    private static final String LOG_TAG = "[AcafelaController]";
    private static final int BUFFER_SIZE = 128;
    public static final int CONTROL_TIMEOUT = 300;
    public static final int RETRY_COUNT = 3;
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

    private byte[]mSenderMsg;
    private int mRetryCnt;
    private Timer mTimer;
    private boolean mIsVideoCall;

    private  enum STATE {
        IDLE_STATE,
        INVITE_STATE,
        RINGING_STATE,
        CONNECTING_STATE,
    }
    private STATE mState;

    public VoipController(Context context)
    {
        mState = STATE.IDLE_STATE;
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
                        socket.receive(packet);
                        String senderIP = packet.getAddress().getHostAddress();
                        byte[] data = new byte[packet.getLength()];
                        System.arraycopy(packet.getData(), packet.getOffset(),data, 0, packet.getLength());
                        SIPMessage sipMessage = SIPMessage.parseFrom(data);

                        if(sipMessage.getIsACK()) {
                            Log.e(LOG_TAG, "message get ACK [" + sipMessage.getCmd().toString() + "]");
                            if(mTimer!=null)  {
                                mTimer.cancel();
                                mTimer=null;
                            }
                            continue;
                        } else {
                            Log.e(LOG_TAG, "Send  ACK answer by message[" + sipMessage.getCmd().toString() + "]");
                            if(sipMessage.getCmd()==INVITE)
                            {
                                sesssionID = sipMessage.getSessionid();
                                mCallerNumber = sipMessage.getFrom();
                                mCalleeNumber = sipMessage.getTo();
                                mIsVideoCall = sipMessage.getIsVideoCall();
                            }
                            replyMessage(sipMessage);
                        }

                        Log.e(LOG_TAG, "Got UDP message from " + senderIP + ", message: " + sipMessage.toString());
                        handle(sipMessage);
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
                    if(mState!=STATE.RINGING_STATE)
                        mRingControl.ringbackTone_start();
                    mState = STATE.RINGING_STATE;
                    break;
                case ACCEPTCALL:
                    if(mState==STATE.RINGING_STATE)
                        mRingControl.ringbackTone_stop();
                    break;
                case OPENSESSION:
                    if(mState==STATE.CONNECTING_STATE) break;
                    byte[] keyByte = mCryptoRpc.getKey(sesssionID);
                    mCrypto  = CryptoBroker.getInstance().create("AES");
                    mCrypto.init(keyByte);

                    for(int i=0;i<message.getSessioninfo().getSessionsCount();i++) {
                        SipMessage.Session session = message.getSessioninfo().getSessions(i);
                        opensession(session.getSessiontype(), session.getIp(), session.getPort());
                    }
                    mState = STATE.CONNECTING_STATE;
                    break;
                case CLOSESESSION:
                    for(int i=0;i<message.getSessioninfo().getSessionsCount();i++) {
                        int idx;
                        SipMessage.Session session = message.getSessioninfo().getSessions(i);
                        for (idx=0; idx < mSessionList.size();idx++){
                            DataCommunicator data = (DataCommunicator) mSessionList.get(idx);
                            if(data.getPortNum()==session.getPort())
                            {
                                data.endCommunicator();
                                mSessionList.remove(data);
                            }
                        }
                    }
                    break;
                case BYE:
                    endCommunication();
                    mState = STATE.IDLE_STATE;
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
                    if(mState==STATE.IDLE_STATE) {
                        mRingControl.ring_start();
                        sendMessage(SipMessage.Command.RINGING);
                        Intent intent = new Intent(mContext, AudioCallActivity.class);
                        intent.putExtra(INTENT_PHONENUMBER, mCallerNumber);
                        intent.putExtra(INTENT_ISCALLEE, true);
                        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                    }
                    mState = STATE.RINGING_STATE;
                    break;
                case OPENSESSION:
                    if(mState==STATE.CONNECTING_STATE) break;
                    byte[] keyByte = mCryptoRpc.getKey(sesssionID);
                    mCrypto = CryptoBroker.getInstance().create("AES");
                    Log.e(LOG_TAG, "Send Message: " +"keyByte" + keyByte.length);
                    mCrypto.init(keyByte);
                    for(int i=0;i<message.getSessioninfo().getSessionsCount();i++) {
                        SipMessage.Session session = message.getSessioninfo().getSessions(i);
                        opensession(session.getSessiontype(), session.getIp(), session.getPort());
                    }
                    mState = STATE.CONNECTING_STATE;
                    break;
                case CLOSESESSION:
                    for(int i=0;i<message.getSessioninfo().getSessionsCount();i++) {
                        int idx;
                        SipMessage.Session session = message.getSessioninfo().getSessions(i);
                        for (idx=0; idx < mSessionList.size();idx++){
                            DataCommunicator data = (DataCommunicator) mSessionList.get(idx);
                            if(data.getPortNum()==session.getPort())
                            {
                                data.endCommunicator();
                                mSessionList.remove(data);
                            }
                        }
                    }
                    break;
                case BYE:
                    endCommunication();
                    mState = STATE.IDLE_STATE;
                    break;
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

    private void endCommunication() {
        mRingControl.allStop();
        destroyAllsession();
        finishCallActivity();
        isCaller = false;
        mState = STATE.IDLE_STATE;
        msgSeq = 0;
        mIsVideoCall =false;
    }

    private void finishCallActivity() {
        Log.i(LOG_TAG, "finishCallActivity");
        Intent intent = new Intent(BROADCAST_BYE);
        mContext.sendBroadcast(intent);
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
    public void  replyMessage(SIPMessage msg)
    {
        final byte[] SenderMsg = msg.toBuilder().setIsACK(true).build().toByteArray();
        UdpSend(SenderMsg);
    }
    public void  sendMessage(SipMessage.Command cmd)
    {
        SIPMessage.Builder builder = SIPMessage.newBuilder();
        final byte[] mSenderMsg = builder.
                setCmd(cmd).
                setIsVideoCall(mIsVideoCall).
                setFrom(mCallerNumber).
                setTo(mCalleeNumber).
                setSessionid(sesssionID).
                setSeq(msgSeq).
                build().
                toByteArray();
        UdpSend(mSenderMsg);
        Log.e(LOG_TAG, "Send Message : "  +  cmd.toString());

        mRetryCnt = RETRY_COUNT;
        if(mTimer!=null)  {
            mTimer.cancel();
            mTimer=null;
        }
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.e(LOG_TAG, "mRetryCnt : "  +  mRetryCnt);
                if(--mRetryCnt==0)
                {
                    mTimer.cancel();
                    Log.e(LOG_TAG, "Timeout Control Message" );
                    terminateCall();
                }
                else
                    UdpSend(mSenderMsg);
            }
        }, CONTROL_TIMEOUT, CONTROL_TIMEOUT);
        msgSeq++;
    }

    public void inviteCall(String calleeNumber, boolean isVideoCall)
    {
        if(mState != STATE.IDLE_STATE) return;
        mState = STATE.INVITE_STATE;
        mCalleeNumber = calleeNumber;
        mCallerNumber = UserInfo.getInstance().getPhoneNumber();
        sesssionID = UserInfo.getInstance().getPhoneNumber() + sesssionNo++;
        isCaller = true;
        mIsVideoCall = isVideoCall;

        sendMessage(INVITE);
    }

    public void terminateCall()
    {
        if(mState == STATE.IDLE_STATE) return;
        endCommunication();
        sendMessage(SipMessage.Command.TERMINATE);
    }
    public void acceptCall()
    {
        if(mState != STATE.RINGING_STATE) return;
        if(isCaller) return;
        mRingControl.ring_stop();
        sendMessage(SipMessage.Command.ACCEPTCALL);
    }
}
