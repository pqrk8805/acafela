package com.acafela.harmony.ui.contacts;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.acafela.harmony.R;

import java.util.ArrayList;

public class ContactAdapter extends BaseAdapter
{
    LayoutInflater inflater = null;
    private ArrayList<ContactModel> mData = null;
    private int nListCnt = 0;

    public ContactAdapter(ArrayList<ContactModel> input_Data)
    {
        mData = input_Data;
        nListCnt = mData.size();
    }

    @Override
    public int getCount()
    {
        Log.i("TAG", "getCount");
        return nListCnt;
    }

    @Override
    public Object getItem(int position)
    {
        return null;
    }

    @Override
    public long getItemId(int position)
    {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent)
    {
        if (convertView == null)
        {
            final Context context = parent.getContext();
            if (inflater == null)
            {
                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            convertView = inflater.inflate(R.layout.items_contact, parent, false);
        }

        TextView mName = (TextView) convertView.findViewById(R.id.contact_name);
        TextView mNumber = (TextView) convertView.findViewById(R.id.contact_number);

        mName.setText(mData.get(position).name);
        mNumber.setText(mData.get(position).number);

        Button button = (Button) convertView.findViewById(R.id.contact_call_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("TAG", "onClick Call");
            }
        });

        return convertView;
    }
}