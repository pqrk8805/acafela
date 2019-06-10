package com.acafela.harmony.crypto;

import com.acafela.harmony.crypto.aes.CryptoAES;

public class Crypto
{
    private Crypto()
    {}

    public static void init()
    {
        CryptoBroker broker = CryptoBroker.getInstance();
        broker.register("AES", new CryptoAES());
    }
}
