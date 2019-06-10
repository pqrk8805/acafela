package com.acafela.harmony.crypto.aes;

import android.util.Log;

import com.acafela.harmony.crypto.ICrypto;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class CryptoAES implements ICrypto
{
    private static final String LOG_TAG = "SEC_AES";
    private Cipher mEncryptCipher;
    private Cipher mDecryptCipher;

    public CryptoAES()
    {}

    @Override
    public ICrypto create()
    {
        return new CryptoAES();
    }

    @Override
    public void init(byte[] password)
    {
        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed(password);
            KeyGenerator keygen = KeyGenerator.getInstance("AES");
            keygen.init(128, sr); // 192 and 256 bits may not be available 
            SecretKey secretKey = keygen.generateKey();
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getEncoded(), "AES");

            mEncryptCipher = Cipher.getInstance("AES");
            mEncryptCipher.init(Cipher.ENCRYPT_MODE, keySpec);

            mDecryptCipher = Cipher.getInstance("AES");
            mDecryptCipher.init(Cipher.DECRYPT_MODE, keySpec);
        } catch (Exception e) {
            Log.e(LOG_TAG, "FAIL", e);
        }
    }

    @Override
    public byte[] encrypt(byte[] plane)
    {
        byte[] encrypted = null;
        try {
            encrypted = mEncryptCipher.doFinal(plane);
        } catch (Exception e) {
            Log.e(LOG_TAG, "FAIL", e);
        }
        return encrypted;
    }

    @Override
    public byte[] decrypt(byte[] encrypted)
    {
        byte[] plane = null;
        try {
            plane = mDecryptCipher.doFinal(encrypted);
        } catch (Exception e) {
            Log.e(LOG_TAG, "FAIL", e);
        }
        return plane;
    }
}
