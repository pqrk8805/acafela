package com.acafela.harmony.service;

public interface DataReceiver {
    public boolean setSession(String ip,int port);
    public boolean startReceiver();
    public boolean endReceiver();
}
