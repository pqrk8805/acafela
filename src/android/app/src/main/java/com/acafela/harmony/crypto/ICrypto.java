package com.acafela.harmony.crypto;

public interface ICrypto
{
    ICrypto create();

    void init(byte[] password);
    byte[] decrypt(byte[] encrypted, int offset, int length);
    byte[] encrypt(byte[] plane, int offset, int length);
}
