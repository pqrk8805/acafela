package com.acafela.harmony.ui.contacts;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.acafela.harmony.R;
import com.acafela.harmony.service.HarmonyService;
import com.acafela.harmony.ui.AudioCallActivity;
import com.acafela.harmony.ui.TestCallActivity;
import com.acafela.harmony.ui.VideoCallActivity;

import java.util.List;

public class ContactAdapter extends BaseAdapter
                                implements DialogInterface.OnDismissListener
{
    private static final String LOG_TAG = "ContactAdator";

    private LayoutInflater inflater = null;
    private List<ContactEntry> mContacts;
    private DatabaseHelper mDbHelper;
    private Context mContext;
    private DialogInterface.OnDismissListener mDismissListener;

    public ContactAdapter(Context context, DatabaseHelper dbHelper)
    {
        mDismissListener = this;
        mContext = context;
        mDbHelper = dbHelper;
        mContacts = mDbHelper.query();
    }

    @Override
    public int getCount()
    {
        Log.i(LOG_TAG, "getCount");
        return mContacts.size();
    }

    @Override
    public Object getItem(int position)
    {
        Log.i(LOG_TAG, "getItem");
        return mContacts.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        Log.i(LOG_TAG, "getItemId");
        return mContacts.get(position).id;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent)
    {
        if (convertView == null)
        {
            final Context context = parent.getContext();
            if (inflater == null)
            {
                inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            convertView = inflater.inflate(R.layout.items_contact, parent, false);
            final View itemView = convertView;
            final int index = position;
            convertView.setLongClickable(true);
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Log.e(LOG_TAG, "onLongClick()");

                    PopupMenu popupMenu = new PopupMenu(mContext, itemView);
                    popupMenu.getMenuInflater().inflate(R.menu.menu_contact_item, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.menu_contact_delete:
                                    mDbHelper.delete(mContacts.get(index).id);
                                    notifyDataSetChanged();
                                    break;
                                case R.id.menu_contact_edit:
                                {
                                    final ContactEditDialog dlg = new ContactEditDialog(
                                                                            mContext,
                                                                            mDbHelper,
                                                                            mContacts.get(index));
                                    dlg.setOnDismissListener(mDismissListener);
                                    dlg.show();
                                }
                                    break;
                                default:
                                    break;
                            }
                            return true;
                        }
                    });
                    popupMenu.show();
                    return true;
                }
            });
        }

        TextView nameView = convertView.findViewById(R.id.contact_name);
        TextView numberView = convertView.findViewById(R.id.contact_number);

        final String phone = mContacts.get(position).phone;

        nameView.setText(mContacts.get(position).name
                            + " (" + mContacts.get(position).email + ")");
        numberView.setText(phone);

        Button btnVoiceCall = convertView.findViewById(R.id.contact_call_btn);
        btnVoiceCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(LOG_TAG, "onClick VoiceCall: " + phone);
                initiateCall(phone, false);
            }
        });

        Button btnVideoCall = convertView.findViewById(R.id.contact_videocall_btn);
        btnVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(LOG_TAG, "onClick VideoCall");
                initiateCall(phone, true);
            }
        });

        return convertView;
    }

    private void initiateCall(String phone, boolean isVideo)
    {
        Intent serviceIntent = new Intent(mContext, HarmonyService.class);
        serviceIntent.putExtra(TestCallActivity.INTENT_CONTROL, TestCallActivity.INTENT_SIP_INVITE_CALL);
        serviceIntent.putExtra(TestCallActivity.INTEMT_CALLEE_PHONENUMBER, phone);
        serviceIntent.putExtra(TestCallActivity.INTENT_ISVIDEO, isVideo);
        mContext.startService(serviceIntent);

        Intent activityIntent = new Intent(mContext,
                                           isVideo
                                                ? VideoCallActivity.class
                                                : AudioCallActivity.class);
        activityIntent.putExtra(AudioCallActivity.INTENT_PHONENUMBER, phone);
        activityIntent.putExtra(AudioCallActivity.INTENT_ISCALLEE, false);
        mContext.startActivity(activityIntent);
    }

    @Override
    public void notifyDataSetChanged()
    {
        mContacts = mDbHelper.query();
        super.notifyDataSetChanged();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        notifyDataSetChanged();
    }
}