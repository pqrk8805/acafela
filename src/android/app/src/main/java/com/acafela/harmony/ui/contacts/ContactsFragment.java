package com.acafela.harmony.ui.contacts;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.acafela.harmony.R;

public class ContactsFragment extends Fragment
                                implements DialogInterface.OnDismissListener {

    private FloatingActionButton mFab;
    private DatabaseHelper mDbHelper;
    private ContactAdapter mContactAdaptor;
    private DialogInterface.OnDismissListener mDismissListener;

    public ContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(
                        LayoutInflater inflater,
                        ViewGroup container,
                        Bundle savedInstanceState)
    {
        mDismissListener = this;
        mDbHelper = DatabaseHelper.createContactDatabaseHelper(getContext());
        mContactAdaptor = new ContactAdapter(getContext(), mDbHelper);
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        ListView listView = view.findViewById(R.id.contact_listview);
        listView.setAdapter(mContactAdaptor);

        mFab = view.findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ContactAddDialog contactAddDlg
                                        = new ContactAddDialog(getContext(), mDbHelper);
                contactAddDlg.setOnDismissListener(mDismissListener);
                contactAddDlg.show();
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

    @Override
    public void onDismiss(DialogInterface dialog) {
        mContactAdaptor.notifyDataSetChanged();
    }
}
