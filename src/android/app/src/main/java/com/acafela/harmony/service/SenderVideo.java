package com.acafela.harmony.service;


public class SenderVideo implements DataSender {
    private  String mIpAddress;
    private int mPort;

    public boolean setSession(String ip,int port)
    {
        this.mIpAddress = ip;
        this.mPort= port;
        return true;
    }

    public boolean startSender()
    {
        return true;
    }
    public boolean endSender()
    {
        return true;
    }
}

