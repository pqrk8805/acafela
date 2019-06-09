package com.acafela.harmony.crypto;

public interface ICrypto
{
    void init(byte[] password);
    byte[] decrypt(byte[] encrypted);
    byte[] encrypt(byte[] plane);
}
