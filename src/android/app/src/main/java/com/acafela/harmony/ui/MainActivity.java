package com.acafela.harmony.ui;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.acafela.harmony.R;
import com.acafela.harmony.ui.main.ChangePwDialog;
import com.acafela.harmony.ui.main.ContactsFragment;
import com.acafela.harmony.ui.main.DialpadFragment;
import com.acafela.harmony.ui.main.RestorePwDialog;
import com.acafela.harmony.ui.main.SectionsPagerAdapter;
import com.acafela.harmony.ui.main.UserRegisterDialog;
import com.acafela.harmony.userprofile.UserInfo;

import static com.acafela.harmony.ui.main.ChangePwDialog.RESPONSE_CANCEL;

public class MainActivity extends AppCompatActivity
        implements DialpadFragment.OnFragmentInteractionListener, ContactsFragment.OnFragmentInteractionListener {
    private static final String TAG = MainActivity.class.getName();

    private static final int MENU_REGISTER = 0;
    private static final int MENU_CHANGEPASSWORD = 1;
    private static final int MENU_RESTOREPASSWORD = 2;

    private FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                animateFab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        mFab = findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        mFab.hide();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.app_name));
        setSupportActionBar(toolbar);

        UserInfo.getInstance().setPhoneNumber("0000");
        if (UserInfo.getInstance().getPhoneNumber().isEmpty()) {
            new Handler().post(new Runnable() {
                public void run() {
                    showPopup("Please Register");
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (UserInfo.getInstance().getPhoneNumber().isEmpty()) {
            menu.getItem(MENU_REGISTER).setEnabled(true);
            menu.getItem(MENU_CHANGEPASSWORD).setEnabled(false);
            menu.getItem(MENU_RESTOREPASSWORD).setEnabled(false);
        }
        else {
            menu.getItem(MENU_REGISTER).setEnabled(false);
            menu.getItem(MENU_CHANGEPASSWORD).setEnabled(true);
            menu.getItem(MENU_RESTOREPASSWORD).setEnabled(true);

        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_register:
                showRegisterDialog();
                break;
            case R.id.menu_changepassword:
                showChangePwDialog();
                break;
            case R.id.menu_restorepassword:
                showRestorePwDialog();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }


    private void showRegisterDialog()
    {
        final UserRegisterDialog userRegisterDialog = new UserRegisterDialog(this);
        userRegisterDialog.show();
        userRegisterDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.i(TAG, "UserRegisterDialog onDismiss");
                if (UserInfo.getInstance().getPhoneNumber().isEmpty()) {
                    showPopup("Please Register");
                }
            }
        });
    }

    private void showChangePwDialog()
    {
        final ChangePwDialog changePwDialog = new ChangePwDialog(this);
        changePwDialog.show();
        changePwDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.i(TAG, "ChangePwDialog onDismiss");
                if (changePwDialog.getResponse() == 0) {
                    showPopup("Password is Changed");
                }
                else if (changePwDialog.getResponse() == -1) {
                    showPopup("Invalid Old Password");
                }
            }
        });
    }

    private void showRestorePwDialog()
    {
        final RestorePwDialog restorePwDialog = new RestorePwDialog(this);
        restorePwDialog.show();
    }

    private void showPopup(String text) {
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup, null);

        final PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        TextView textViewPopup = popupView.findViewById(R.id.textview_popup);
        textViewPopup.setText(text);
        popupWindow.setAnimationStyle(R.style.popup_window_animation);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }


    private void animateFab(int position) {
        switch (position) {
            case 0:
                mFab.hide();
                break;
            case 1:
            default:
                mFab.show();
                break;
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}