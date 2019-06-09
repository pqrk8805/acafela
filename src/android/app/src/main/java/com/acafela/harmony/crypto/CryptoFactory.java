package com.acafela.harmony.crypto;

public class CryptoFactory
{
    private CryptoFactory()
    {}

    public static ICrypto create(String algorithmName)
    {
        return algorithmName.equals("AES") ? new CryptoAES()
             : null;
    }
}
