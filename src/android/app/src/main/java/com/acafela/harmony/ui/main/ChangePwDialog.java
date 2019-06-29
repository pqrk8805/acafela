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
import com.acafela.harmony.userprofile.UserProfileGrpc;
import com.acafela.harmony.userprofile.UserProfileOuterClass;
import com.acafela.harmony.userprofile.UserProfileRpc;
import com.acafela.harmony.util.ConfigSetup;

import io.grpc.StatusRuntimeException;

public class ChangePwDialog extends Dialog {
    private static final String TAG = ChangePwDialog.class.getName();

    public static final int RESPONSE_CANCEL = -10;

    private int mResponse;

    public ChangePwDialog(@NonNull Activity context) {
        super(context);

        setViews(context);
    }

    private void setViews(final Activity activity) {
        setContentView(R.layout.dialog_changepw);
        setCancelable(true);

        final EditText editText_email = findViewById(R.id.editText_email);
        final EditText editText_oldPw = findViewById(R.id.editText_oldPw);
        final EditText editText_newPw = findViewById(R.id.editText_newPw);
        Button buttonChangePw = findViewById(R.id.btn_changepw);
        buttonChangePw.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                String email = editText_email.getText().toString();
                if (!FormatChecker.isValidEmail(email)) {
                    editText_email.setError("Enter a valid email address");
                    return;
                }

                String oldPw = editText_oldPw.getText().toString();
                if (!FormatChecker.isValidPassword(oldPw)) {
                    editText_oldPw.setError("Input 4 digits");
                    return;
                }

                String newPw = editText_newPw.getText().toString();
                if (!FormatChecker.isValidPassword(newPw)) {
                    editText_newPw.setError("Input 4 digits");
                    return;
                }

                ChangePwTask changePwTask = new ChangePwTask(email, oldPw, newPw, activity);
                changePwTask.execute();
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

    public int getResponse() {
        return mResponse;
    }

    private class ChangePwTask extends AsyncTask<Void, Void, Void> {
        String mEmail;
        String mOldPw;
        String mNewPw;
        Activity mActivity;
        ProgressDialog mProgressDialog;

        public ChangePwTask(String email, String oldPw, String newPw, Activity activity) {
            mEmail = email;
            mOldPw = oldPw;
            mNewPw = newPw;
            mActivity = activity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mProgressDialog = new ProgressDialog(mActivity,
                    R.style.Theme_AppCompat_DayNight_Dialog);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setMessage("Changing Password...");
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
                    ConfigSetup.getInstance().getServerIP(getContext()),
                    Config.RPC_PORT_USER_PROFILE,
                    mActivity.getResources().openRawResource(R.raw.ca),
                    mActivity.getResources().openRawResource(R.raw.server)).
                    getBlockingStub();

            Common.Error error = null;
            try {
                error = blockingStub.changePassword(UserProfileOuterClass.ChangePasswordParam.newBuilder().
                        setEmailAddress(mEmail).
                        setOldPassword(mOldPw).
                        setNewPassword(mNewPw).
                        build());

                Log.e(TAG, error.toString());
            } catch (StatusRuntimeException e) {
                Log.i(TAG, "ChangePwTask is Interrupted");
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
