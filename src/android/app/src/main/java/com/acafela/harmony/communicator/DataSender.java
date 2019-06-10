package com.acafela.harmony.communicator;

public interface DataSender {
    public boolean setSession(String ip,int port);
    public boolean startSender();
    public boolean endSender();
}
