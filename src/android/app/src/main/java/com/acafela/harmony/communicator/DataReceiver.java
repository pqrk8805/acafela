package com.acafela.harmony.communicator;

public interface DataReceiver {
    public boolean setSession(String ip,int port);
    public boolean startReceiver();
    public boolean endReceiver();
}
