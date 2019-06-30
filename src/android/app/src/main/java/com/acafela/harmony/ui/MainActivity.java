package com.acafela.harmony.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuCompat;
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
import com.acafela.harmony.directoryservice.DirectoryService;
import com.acafela.harmony.service.HarmonyService;
import com.acafela.harmony.ui.concall.ConcallReserveDlg;
import com.acafela.harmony.ui.dialpad.DialpadFragment;
import com.acafela.harmony.ui.main.ChangePwDialog;
import com.acafela.harmony.ui.main.RestorePwDialog;
import com.acafela.harmony.ui.main.SectionsPagerAdapter;
import com.acafela.harmony.ui.main.UserRegisterDialog;
import com.acafela.harmony.userprofile.UserInfo;

import java.io.File;

import static com.acafela.harmony.ui.AudioCallActivity.INTENT_ISCALLEE;
import static com.acafela.harmony.ui.AudioCallActivity.INTENT_ISCONFERENCECALL;
import static com.acafela.harmony.ui.AudioCallActivity.INTENT_PHONENUMBER;
import static com.acafela.harmony.ui.TestCallActivity.INTEMT_CALLEE_PHONENUMBER;
import static com.acafela.harmony.ui.TestCallActivity.INTENT_CONTROL;
import static com.acafela.harmony.ui.TestCallActivity.INTENT_ISVIDEO;
import static com.acafela.harmony.ui.TestCallActivity.INTENT_SIP_INVITE_CALL;
import static com.acafela.harmony.util.ConfigSetup.filename;

public class MainActivity extends AppCompatActivity implements DialpadFragment.Callback {
    private static final String TAG = MainActivity.class.getName();

    private static final int PERMISSION_ALL_ID = 1;
    private static final String[] PERMISSIONS = {
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.CAMERA
    };

    private static final int MENU_REGISTER = 0;
    private static final int MENU_CHANGEPASSWORD = 1;
    private static final int MENU_RESTOREPASSWORD = 2;
    private static final int MENU_RESERVCONCALL = 3;
    private static final int MENU_PHONENUMBER = 4;
    private static final String HIDDEN_TEST_MAIN = "9999";
    private static final String HIDDEN_UNREGISTER = "8888";
    private static final String HIDDEN_REMOVE_SERVER_IP = "7777";

    private DirectoryService mDirectoryService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        requestPermissions();

        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.app_name));
        setSupportActionBar(toolbar);

        Intent serviceIntent = new Intent(getApplicationContext(), HarmonyService.class);
        startService(serviceIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    @Override
    protected void onResume() {
        super.onResume();

        mDirectoryService = new DirectoryService(this);
        UserInfo.getInstance().load(this);
        if (UserInfo.getInstance().getPhoneNumber().isEmpty()) {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    showPopup("Please Register");
                }
            }, 1000);
        }else {
            mDirectoryService.update();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuCompat.setGroupDividerEnabled(menu, true);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (UserInfo.getInstance().getPhoneNumber().isEmpty()) {
            menu.getItem(MENU_REGISTER).setEnabled(true);
            menu.getItem(MENU_CHANGEPASSWORD).setEnabled(false);
            menu.getItem(MENU_RESTOREPASSWORD).setEnabled(false);
            menu.getItem(MENU_RESERVCONCALL).setEnabled(false);
            menu.getItem(MENU_PHONENUMBER).setVisible(false);
        }
        else {
            menu.getItem(MENU_REGISTER).setEnabled(false);
            menu.getItem(MENU_CHANGEPASSWORD).setEnabled(true);
            menu.getItem(MENU_RESTOREPASSWORD).setEnabled(true);
            menu.getItem(MENU_RESERVCONCALL).setEnabled(true);
            menu.getItem(MENU_PHONENUMBER).setVisible(true);
            menu.getItem(MENU_PHONENUMBER).setTitle(UserInfo.getInstance().getPhoneNumber());
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
            case R.id.menu_reserve_concall:
                showConcallReserveDialog();
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
        restorePwDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.i(TAG, "ChangePwDialog onDismiss");
                if (restorePwDialog.getResponse() == 0) {
                    showPopup("We`ve sent an email to " + restorePwDialog.getEmail());
                }
            }
        });
    }

    private void showConcallReserveDialog()
    {
        final ConcallReserveDlg dlg = new ConcallReserveDlg(this);
        dlg.show();
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

    @Override
    public void initiateCall(String raw, boolean isVideo) {
        Log.i(TAG, "initiateCall: " + raw);

        if (raw.equals(HIDDEN_TEST_MAIN)) {
            Intent activityIntent = new Intent(this, TestMainActivity.class);
            startActivity(activityIntent);
            return;
        }
        else if (raw.equals(HIDDEN_REMOVE_SERVER_IP)) {
            String path = getFilesDir().getAbsolutePath() + "/" + filename;
            File file = new File(path);
            file.delete();
            return;
        }
        else if (raw.equals(HIDDEN_UNREGISTER)) {
            UserInfo.getInstance().setPhoneNumber("");
            finish();
            return;
        }

        if (UserInfo.getInstance().getPhoneNumber().isEmpty()) {
            showPopup("Please Register");
            return;
        }

        boolean isConferenceCall = false;
        if (raw.length() == 5 && raw.charAt(0) == '#') {
            isConferenceCall = true;
        }

        if (raw.length() == 4 || isConferenceCall) {
            Intent serviceIntent = new Intent(getApplicationContext(), HarmonyService.class);
            serviceIntent.putExtra(INTENT_CONTROL, INTENT_SIP_INVITE_CALL);
            serviceIntent.putExtra(INTEMT_CALLEE_PHONENUMBER, raw);
            serviceIntent.putExtra(INTENT_ISVIDEO, isVideo);
            startService(serviceIntent);

            if (isVideo) {
                Intent activityIntent = new Intent(this, VideoCallActivity.class);
                activityIntent.putExtra(INTENT_PHONENUMBER, raw);
                activityIntent.putExtra(INTENT_ISCALLEE, false);
                if (isConferenceCall) {
                    activityIntent.putExtra(INTENT_ISCONFERENCECALL, true);
                }
                else {
                    activityIntent.putExtra(INTENT_ISCONFERENCECALL, false);
                }
                startActivity(activityIntent);
            }
            else {
                Intent activityIntent = new Intent(this, AudioCallActivity.class);
                activityIntent.putExtra(INTENT_PHONENUMBER, raw);
                activityIntent.putExtra(INTENT_ISCALLEE, false);
                startActivity(activityIntent);
            }

            return;
        }

        showPopup("Please Dial Valid PhoneNumber.");
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void requestPermissions() {
        if(!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this,
                    PERMISSIONS,
                    PERMISSION_ALL_ID);
        }
    }
}