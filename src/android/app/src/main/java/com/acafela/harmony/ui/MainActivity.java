package com.acafela.harmony.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.acafela.harmony.userprofile.FormatChecker;
import com.acafela.harmony.R;
import com.acafela.harmony.ui.main.ContactsFragment;
import com.acafela.harmony.ui.main.DialpadFragment;
import com.acafela.harmony.ui.main.SectionsPagerAdapter;
import com.acafela.harmony.userprofile.UserProfileGrpc;
import com.acafela.harmony.userprofile.UserProfileRpc;

public class MainActivity extends AppCompatActivity
        implements DialpadFragment.OnFragmentInteractionListener, ContactsFragment.OnFragmentInteractionListener {
    private static final String TAG = MainActivity.class.getName();

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_register:
                registerDialog();
                break;
            case R.id.menu_changepassword:
                Toast.makeText(MainActivity.this, "Change Password Not Implemented", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_restorepassword:
                Toast.makeText(MainActivity.this, "Restore Password Not Implemented", Toast.LENGTH_SHORT).show();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void registerDialog()
    {
        final Dialog myDialog = new Dialog(this);
        myDialog.setContentView(R.layout.dialog_register);
        myDialog.setCancelable(true);
        myDialog.show();

        final EditText editText_email = myDialog.findViewById(R.id.editText_email);
        final EditText editText_password = myDialog.findViewById(R.id.editText_password);
        final EditText editText_repeatPassword = myDialog.findViewById(R.id.editText_repeatPassword);
        Button buttonRegister = myDialog.findViewById(R.id.btn_register);
        buttonRegister.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                String email = editText_email.getText().toString();
                if (!FormatChecker.isValidEmail(email)) {
                    editText_email.setError("Enter a valid email address");
                    return;
                }

                String password = editText_password.getText().toString();
                if (!FormatChecker.isValidPassword(password)) {
                    editText_password.setError("Input 4 digits");
                    return;
                }

                String repeatPassword = editText_repeatPassword.getText().toString();
                if (!FormatChecker.isValidRepeatPassword(password, repeatPassword)) {
                    editText_repeatPassword.setError("Password should be same");
                    return;
                }

                final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this,
                        R.style.Theme_AppCompat_DayNight_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Creating Account...");
                progressDialog.show();

                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                UserProfileRpc mUserProfileRpc = new UserProfileRpc(
                                        "10.0.0.1",
                                        0,
                                        getResources().openRawResource(R.raw.ca),
                                        getResources().openRawResource(R.raw.server));
                                UserProfileGrpc.UserProfileBlockingStub blockingStub
                                        = mUserProfileRpc.getBlockingStub();

                                progressDialog.dismiss();
                                myDialog.dismiss();
                            }
                        }, 3000);
            }
        });

        Button buttonCancel = myDialog.findViewById(R.id.btn_cancel);
        buttonCancel.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                myDialog.dismiss();
            }
        });

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