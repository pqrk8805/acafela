package com.acafela.harmony.ui.concall;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.acafela.harmony.Config;
import com.acafela.harmony.R;
import com.acafela.harmony.concall.ConCallReservRpc;
import com.acafela.harmony.ui.contacts.ContactDbHelper;
import com.acafela.harmony.userprofile.UserInfo;
import com.acafela.harmony.util.ConfigSetup;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


public class ConcallReserveDlg extends Dialog
{
    private static final String LOG_TAG = "CCReservDlg";

    private List<Participant> mParticipants;
    private ParticipantListAdaptor mParticipantsAdaptor;
    private ContactDbHelper mContactDbHelper;

    public ConcallReserveDlg(@NonNull Context context)
    {
        super(context);

        mParticipants = new ArrayList<>();
        mParticipantsAdaptor = new ParticipantListAdaptor(mParticipants);
        mContactDbHelper = ContactDbHelper.CreateHelper(context);
        setViews(context);
    }

    private void setViews(final Context context) {
        setContentView(R.layout.dialog_concall_reserve);
        setCancelable(true);

        final EditText edFrom = findViewById(R.id.et_concall_from);
        final EditText edTo = findViewById(R.id.et_concall_to);
        setEditTextForDateTime(context, edFrom);
        setEditTextForDateTime(context, edTo);

        ListView listView = findViewById(R.id.lv_concall_participants);
        listView.setAdapter(mParticipantsAdaptor);

        findViewById(R.id.fab_concall_add_participants).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.d(LOG_TAG, "onClick() Add Participant");
                if (mParticipants.size() >= 4) {
                    Toast.makeText(context, "Max participants is 4", Toast.LENGTH_LONG).show();
                } else {
                    showAddParticipantDlg(context);
                }
            }
        });

        Button buttonRegister = findViewById(R.id.btn_reserve);
        buttonRegister.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(LOG_TAG, "onClick() Reserve");

                List<String> pl = new ArrayList<>();
                for (Participant p : mParticipants) {
                    pl.add(p.phone);
                }

                ConCallReservRpc rpc = new ConCallReservRpc(
                                    ConfigSetup.getInstance().getServerIP(context),
                                    Config.RPC_PORT_CONFERENCE_CALL_RESERVE);
                int err = rpc.reserve(
                                    UserInfo.getInstance().getPhoneNumber(),
                                    edFrom.getText().toString(),
                                    edTo.getText().toString(),
                                    pl);
                Log.d(LOG_TAG, "Result: " + err);
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

    private static void setEditTextForDateTime(final Context context, final EditText ebDest)
    {
        ebDest.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                final Calendar currentDate = Calendar.getInstance();
                final Calendar date = Calendar.getInstance();
                new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                           @Override
                           public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                date.set(year, monthOfYear, dayOfMonth);
                                new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                        date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                        date.set(Calendar.MINUTE, minute);
                                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyy-MM-dd HH:mm");
                                        String dateStr = simpleDateFormat.format(date.getTime());
                                        ebDest.setText(dateStr);
                                    }
                                }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), false).show();
                           }
                }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
            }
        });
    }

    private void showAddParticipantDlg(final Context context)
    {
        ConcallAddParticipantDlg dlg = new ConcallAddParticipantDlg(context, new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                String phone = ((EditText)v).getText().toString();
                Participant p = new Participant(
                                            phone,
                                            mContactDbHelper.query(phone));
                mParticipants.add(p);
                mParticipantsAdaptor.notifyDataSetChanged();
            }
        });
        dlg.show();
    }
}
