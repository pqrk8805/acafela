package com.acafela.harmony.ui.main;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.acafela.harmony.Config;
import com.acafela.harmony.R;
import com.acafela.harmony.rpc.Common;
import com.acafela.harmony.userprofile.FormatChecker;
import com.acafela.harmony.userprofile.UserInfo;
import com.acafela.harmony.userprofile.UserProfileGrpc;
import com.acafela.harmony.userprofile.UserProfileOuterClass.RestorePasswordParam;
import com.acafela.harmony.userprofile.UserProfileRpc;

import io.grpc.StatusRuntimeException;

import static com.acafela.harmony.ui.main.ChangePwDialog.RESPONSE_CANCEL;

public class RestorePwDialog extends Dialog {
    private static final String TAG = RestorePwDialog.class.getName();

    private String mEmail;

    private int mResponse;

    public RestorePwDialog(@NonNull Activity activity) {
        super(activity);

        setViews(activity);
    }

    private void setViews(final Activity activity) {
        setContentView(R.layout.dialog_restorepw);
        setCancelable(true);


        final EditText editText_phone = findViewById(R.id.editText_phone);
        editText_phone.setText(UserInfo.getInstance().getPhoneNumber());
        editText_phone.setEnabled(false);
        final EditText editText_email = findViewById(R.id.editText_email);
        Button buttonChangePw = findViewById(R.id.btn_restorepw);
        buttonChangePw.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String phoneNumber = editText_phone.getText().toString();
                String email = editText_email.getText().toString();
                if (!FormatChecker.isValidEmail(email)) {
                    editText_email.setError("Enter a valid email address");
                    return;
                }
                mEmail = email;

                RestorePwTask userRegisterTask = new RestorePwTask(phoneNumber, email, activity);
                userRegisterTask.execute();
            }
        });

        Button buttonCancel = findViewById(R.id.btn_cancel);
        buttonCancel.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                dismiss();
            }
        });
        mResponse = RESPONSE_CANCEL;
    }

    public String getEmail() {
        return mEmail;
    }

    public int getResponse() {
        return mResponse;
    }

    private class RestorePwTask extends AsyncTask<Void, Void, Void> {
        String mPhoneNumber;
        String mEmail;
        Activity mActivity;
        ProgressDialog mProgressDialog;

        public RestorePwTask(String phoneNumber, String email, Activity activity) {
            mPhoneNumber = phoneNumber;
            mEmail = email;
            mActivity = activity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mProgressDialog = new ProgressDialog(mActivity,
                    R.style.Theme_AppCompat_DayNight_Dialog);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setMessage("Restore Password");
            mProgressDialog.setOnCancelListener(new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Log.i(TAG, "progressDialog onCancel");
                    cancel(true);
                }
            });
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            UserProfileGrpc.UserProfileBlockingStub blockingStub = new UserProfileRpc(
                    Config.SERVER_IP,
                    Config.RPC_PORT_USER_PROFILE,
                    mActivity.getResources().openRawResource(R.raw.ca),
                    mActivity.getResources().openRawResource(R.raw.server)).
                    getBlockingStub();

            Common.Error error = null;
            try {
                error = blockingStub.restorePassword(RestorePasswordParam.newBuilder().
                        setPhoneNumber(mPhoneNumber).
                        setEmailAddress(mEmail).
                        build());

                Log.e(TAG, error.toString());
            } catch (StatusRuntimeException e) {
                Log.i(TAG, "UserRegisterTask is Interrupted");
            }

            if (error != null) {
                mResponse = error.getErr();
            }

            mProgressDialog.dismiss();
            dismiss();

            return null;
        }
    }
}
