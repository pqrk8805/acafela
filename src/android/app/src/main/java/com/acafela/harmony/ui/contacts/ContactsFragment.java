package com.acafela.harmony.ui.contacts;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.acafela.harmony.R;

import java.util.ArrayList;

public class ContactsFragment extends Fragment {

    private FloatingActionButton mFab;

    public ContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // For Test, need add a logic for get data and assgined to objectData --------------------------------.
        String[] strDate = {"2017-01-03", "1965-02-23", "2016-04-13", "2010-01-01", "2017-06-20",
                "2012-07-08", "1980-04-14", "2016-09-26", "2014-10-11", "2010-12-24"};

        int nDatCnt=0;
        ArrayList<ContactModel> objectData = new ArrayList<>();
        for (int i=0; i<10; ++i)
        {
            ContactModel oItem = new ContactModel();
            oItem.name = "Data " + (i+1);
            oItem.number = strDate[nDatCnt++];
            objectData.add(oItem);
            if (nDatCnt >= strDate.length) nDatCnt = 0;
        }
        // For Test, need add a logic for get data --------------------------------.

        ListAdapter oAdapter = new ContactAdapter(objectData);
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        ListView listView = view.findViewById(R.id.contact_listview);
        listView.setAdapter(oAdapter);

        mFab = view.findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
