package com.acafela.harmony.userprofile;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class UserInfo {
    private static final String TAG = UserInfo.class.getName();

    private static UserInfo INSTANCE;

    private static final String KEY_USERINFO = "UserInfo";
    private static final String KEY_PHONENUMBER = "PhoneNumber";

    private SharedPreferences mSharedPreferences;
    private String mPhoneNumber;

    public synchronized static UserInfo getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UserInfo();
        }
        return INSTANCE;
    }

    public void load(Context context) {
        mSharedPreferences = context.getSharedPreferences(KEY_USERINFO, Context.MODE_PRIVATE);

        mPhoneNumber = mSharedPreferences.getString(KEY_PHONENUMBER, "");
    }

    public void setPhoneNumber(String phoneNumber) {
        mPhoneNumber = phoneNumber;

        Editor editor = mSharedPreferences.edit();
        editor.putString(KEY_PHONENUMBER, mPhoneNumber);
        editor.commit();
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }
}
