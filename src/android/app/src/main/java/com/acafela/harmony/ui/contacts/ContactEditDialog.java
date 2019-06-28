package com.acafela.harmony.ui.contacts;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.acafela.harmony.R;

public class ContactEditDialog extends Dialog
{
    private static final String LOG_TAG = "ContactAddDlg";

    public ContactEditDialog(@NonNull Context context,
                             @NonNull DatabaseHelper dbHelper,
                             @NonNull ContactEntry contactEntry) {
        super(context);

        setViews(dbHelper, contactEntry);
    }

    private void setViews(final DatabaseHelper dbHelper, final ContactEntry contact) {
        setContentView(R.layout.dialog_contact_edit);
        setCancelable(true);

        final EditText etName = findViewById(R.id.editText_name);
        final EditText etPhone = findViewById(R.id.editText_phone);
        final EditText etEmail = findViewById(R.id.editText_email);

        etName.setText(contact.name);
        etPhone.setText(contact.phone);
        etEmail.setText(contact.email);

        Button buttonRegister = findViewById(R.id.btn_ok);
        buttonRegister.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(LOG_TAG, "onClick() ADD");
                int n = dbHelper.update(
                                    contact.id,
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
