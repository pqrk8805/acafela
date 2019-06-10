package com.acafela.harmony.crypto;

import android.util.Log;

import java.util.HashMap;
import java.util.Set;

public class CryptoBroker
{
    private static final String LOG_TAG = "CryptoBroker";
    private HashMap<String, ICrypto> mRegisterBook;

    private static class LazyHolder {
        static final CryptoBroker INSTANCE = new CryptoBroker();
    }

    public static CryptoBroker getInstance() {
        return LazyHolder.INSTANCE;
    }

    private CryptoBroker()
    {
        mRegisterBook = new HashMap<>();
    }

    public void register(
                    String algorithmName,
                    ICrypto prototype)
    {
        Log.i(LOG_TAG, "register: " + algorithmName);
        synchronized (mRegisterBook) {
            mRegisterBook.put(algorithmName, prototype);
        }
    }

    public Set<String> getRegisterList()
    {
        synchronized (mRegisterBook) {
            return mRegisterBook.keySet();
        }
    }

    public synchronized ICrypto create(String algorithmName)
    {
        synchronized (mRegisterBook) {
            ICrypto crypto = null;
            ICrypto prototype = mRegisterBook.get(algorithmName);
            if (prototype != null)
                crypto = prototype.create();
            return crypto;
        }
    }
}
