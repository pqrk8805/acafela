package com.acafela.harmony.ui.concall;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.acafela.harmony.R;


public class ConcallAddParticipantDlg extends Dialog
{
    private static final String LOG_TAG = "CCAddPartDlg";

    private View.OnClickListener mOkClickListener;

    public ConcallAddParticipantDlg(@NonNull Context context,
                                    @NonNull View.OnClickListener okClickListener)
    {
        super(context);

        mOkClickListener = okClickListener;
        setViews(context);
    }

    private void setViews(final Context context) {
        setContentView(R.layout.dialog_concall_participant_add);
        setCancelable(true);

        final EditText editTest = findViewById(R.id.editText_phone);

        Button buttonRegister = findViewById(R.id.btn_add);
        buttonRegister.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(LOG_TAG, "onClick() Add");
                mOkClickListener.onClick(editTest);
                dismiss();
            }
        });

        Button buttonCancel = findViewById(R.id.btn_cancel);
        buttonCancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(LOG_TAG, "onClick() Cancel");
                dismiss();
            }
        });
    }
}
