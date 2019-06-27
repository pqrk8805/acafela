package com.acafela.harmony.ui.contacts;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.acafela.harmony.R;

public class ContactAddDialog extends Dialog
{
    private static final String LOG_TAG = "ContactAddDlg";
    private DatabaseHelper mDbHelper;

    public ContactAddDialog(@NonNull Context context,
                            @NonNull DatabaseHelper dbHelper) {
        super(context);

        mDbHelper = dbHelper;
        setViews(context);
    }

    private void setViews(final Context context) {
        setContentView(R.layout.dialog_contact_add);
        setCancelable(true);

        final EditText etName = findViewById(R.id.editText_name);
        final EditText etPhone = findViewById(R.id.editText_phone);
        final EditText etEmail = findViewById(R.id.editText_email);
        Button buttonRegister = findViewById(R.id.btn_add);
        buttonRegister.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(LOG_TAG, "onClick() ADD");
                long id = mDbHelper.insert(
                                    etName.getText().toString(),
                                    etPhone.getText().toString(),
                                    etEmail.getText().toString());
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
