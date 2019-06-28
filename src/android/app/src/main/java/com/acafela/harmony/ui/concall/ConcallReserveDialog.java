package com.acafela.harmony.ui.concall;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.acafela.harmony.R;

public class ConcallReserveDialog extends Dialog
{
    private static final String LOG_TAG = "CCReservDlg";

    public ConcallReserveDialog(@NonNull Context context)
    {
        super(context);
        setViews(context);
    }

    private void setViews(final Context context) {
        setContentView(R.layout.dialog_reserve_concall);
        setCancelable(true);
    }
}
