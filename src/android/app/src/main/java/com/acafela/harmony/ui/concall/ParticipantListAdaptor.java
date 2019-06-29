package com.acafela.harmony.ui.concall;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class ParticipantListAdaptor extends BaseAdapter
{
    private static final String LOG_TAG = "ContactAdator";

    private LayoutInflater mInflater = null;
    private List<Participant> mParticipants;

    public ParticipantListAdaptor(
                                List<Participant> participants)
    {
        mParticipants = participants;
    }

    @Override
    public int getCount()
    {
        return mParticipants.size();
    }

    @Override
    public Object getItem(int position)
    {
        return mParticipants.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (convertView == null)
        {
            final Context context = parent.getContext();
            if (mInflater == null) {
                mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            convertView = new TextView(context);

            final int index = position;
            convertView.setLongClickable(true);
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Log.e(LOG_TAG, "onLongClick()");
                    mParticipants.remove(index);
                    Toast.makeText(context, "Removed", Toast.LENGTH_LONG).show();
                    notifyDataSetChanged();
                    return true;
                }
            });
        }

        TextView itemView = (TextView)convertView;
        itemView.setHeight(65);

        String phone = mParticipants.get(position).phone;
        String name = mParticipants.get(position).name;
        itemView.setText(phone + " (" + name + ')');

        return convertView;
    }
}
