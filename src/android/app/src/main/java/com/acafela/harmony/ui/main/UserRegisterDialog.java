package com.acafela.harmony.ui.main;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.acafela.harmony.Config;
import com.acafela.harmony.R;
import com.acafela.harmony.userprofile.FormatChecker;
import com.acafela.harmony.userprofile.UserInfo;
import com.acafela.harmony.userprofile.UserProfileGrpc;
import com.acafela.harmony.userprofile.UserProfileOuterClass;
import com.acafela.harmony.userprofile.UserProfileOuterClass.RegisterResp;
import com.acafela.harmony.userprofile.UserProfileRpc;

import io.grpc.StatusRuntimeException;

public class UserRegisterDialog extends Dialog {
    private static final String TAG = UserRegisterDialog.class.getName();

    public UserRegisterDialog(@NonNull Activity activity) {
        super(activity);

        setViews(activity);
    }

    private void setViews(final Activity activity) {
        setContentView(R.layout.dialog_register);
        setCancelable(true);

        final EditText editText_email = findViewById(R.id.editText_email);
        final EditText editText_password = findViewById(R.id.editText_password);
        final EditText editText_repeatPassword = findViewById(R.id.editText_repeatPassword);
        Button buttonRegister = findViewById(R.id.btn_register);
        buttonRegister.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String email = editText_email.getText().toString();
                if (!FormatChecker.isValidEmail(email)) {
                    editText_email.setError("Enter a valid email address");
                    return;
                }

                String password = editText_password.getText().toString();
                if (!FormatChecker.isValidPassword(password)) {
                    editText_password.setError("Input 4 digits");
                    return;
                }

                String repeatPassword = editText_repeatPassword.getText().toString();
                if (!FormatChecker.isValidRepeatPassword(password, repeatPassword)) {
                    editText_repeatPassword.setError("Password should be same");
                    return;
                }

                UserRegisterTask userRegisterTask = new UserRegisterTask(email, password, activity);
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
    }

    private class UserRegisterTask extends AsyncTask<Void, Void, Void> {
        String mEmail;
        String mPw;
        Activity mActivity;
        ProgressDialog mProgressDialog;

        public UserRegisterTask(String email, String pw, Activity activity) {
            mEmail = email;
            mPw = pw;
            mActivity = activity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mProgressDialog = new ProgressDialog(mActivity,
                    R.style.Theme_AppCompat_DayNight_Dialog);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setMessage("Registering User...");
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

            RegisterResp registerResp = null;
            try {
                registerResp = blockingStub.registerUser(UserProfileOuterClass.RegisterParam.newBuilder().
                        setEmailAddress(mEmail).setPassword(mPw).build());

                Log.i(TAG, registerResp.toString());
            } catch (StatusRuntimeException e) {
                e.printStackTrace();
            }

            if (registerResp != null) {
                if (0 == registerResp.getError().getErr()) {
                    UserInfo.getInstance().setPhoneNumber(registerResp.getPhoneNumber());
                }
            }

            mProgressDialog.dismiss();
            dismiss();

            return null;
        }
    }
}
