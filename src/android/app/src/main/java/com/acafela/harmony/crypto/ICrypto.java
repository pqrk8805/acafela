package com.acafela.harmony.crypto;

public interface ICrypto
{
    ICrypto create();

    void init(byte[] password);
    byte[] decrypt(byte[] encrypted);
    byte[] encrypt(byte[] plane);
}
