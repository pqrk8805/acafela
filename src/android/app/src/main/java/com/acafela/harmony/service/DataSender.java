package com.acafela.harmony.service;

public interface DataSender {
    public boolean setSession(String ip,int port);
    public boolean startSender();
    public boolean endSender();
}
