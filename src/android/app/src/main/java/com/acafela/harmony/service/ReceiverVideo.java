package com.acafela.harmony.service;

public class ReceiverVideo implements DataReceiver {
    private  String mIpAddress;
    private int mPort;

    public boolean setSession(String ip,int port)
    {
        this.mIpAddress = ip;
        this.mPort= port;
        return true;
    }

    public boolean startReceiver()
    {
        return true;
    }
    public boolean endReceiver()
    {
        return true;
    }

}
