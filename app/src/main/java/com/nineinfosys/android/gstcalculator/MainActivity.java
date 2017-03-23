package com.nineinfosys.android.gstcalculator;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity  {
    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;
    ImageView profilePictureView;
    EditText amount,rate;
    Spinner select;
    Button cal,calclear,buton;
    TextView gst,total;
    GstCalculator gstCalculator;
    DecimalFormat f = new DecimalFormat("##.00");
    public Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        amount = (EditText) findViewById(R.id.amountid);
        rate = (EditText) findViewById(R.id.rateid);
        gst = (TextView) findViewById(R.id.gstid);
        total = (TextView) findViewById(R.id.netid);
        cal = (Button) findViewById(R.id.buttoncalculate);
        calclear = (Button) findViewById(R.id.buttonclear);
        select = (Spinner) findViewById(R.id.getselectid);
        ArrayList select1 = new ArrayList();

        select1.add("Add GST");
        select1.add("Remove GST");

        ArrayAdapter<String> dataAdapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, select1);
        dataAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        select.setAdapter(dataAdapter1);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mNavigationView = (NavigationView) findViewById(R.id.shitstuff);


        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mNavigationView.setItemIconTintList(null);

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                mDrawerLayout.closeDrawers();


                //communicate
                if (menuItem.getItemId() == R.id.Share) {
                    final String appPackageName = getPackageName();
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    String shareBodyText = "https://play.google.com/store/apps/details?id=" + appPackageName;
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Subject/Title");
                    intent.putExtra(Intent.EXTRA_TEXT, shareBodyText);
                    startActivity(Intent.createChooser(intent, "Choose sharing method"));

                }

                if (menuItem.getItemId() == R.id.AppStore) {
                    final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://developer?id=GeniusNine+Info+Systems+LLP")));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=GeniusNine+Info+Systems+LLP")));
                    }
                }

                if (menuItem.getItemId() == R.id.GetApps) {

                    /*Intent intent=new Intent(MainActivityDrawer.this,RequestApp.class);
                    startActivity(intent);
*/

                }


                if (menuItem.getItemId() == R.id.RateUs) {
                    final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }


                }


                return false;
            }


        });

        /**
         * Setup Drawer Toggle of the Toolbar
         */

        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.app_name, R.string.app_name);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        cal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (amount.getText().toString().equals("")) {
                    amount.setError("Enter the Amount");
                } else if (rate.getText().toString().equals("")) {
                    rate.setError("Enter the Rate(%)");
                } else {
                    String selectunit = select.getSelectedItem().toString().trim();
                    double amountValue = Double.parseDouble(amount.getText().toString().trim());
                    double rateValue = Double.parseDouble(rate.getText().toString().trim());
                    gstCalculator = new GstCalculator(amountValue, rateValue, selectunit);
                    gst.setText(String.valueOf(f.format(gstCalculator.calGst1())));
                    total.setText(String.valueOf(f.format(gstCalculator.netprice())));
                }
            }
        });
        calclear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amount.setText("");
                rate.setText("");
                gst.setText("");
                total.setText("");


            }
        });

    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setMessage("Are you sure you want to close App?");
                alertDialogBuilder.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {

                                finish();

                            }
                        });

                alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {


                    }
                });

                //Showing the alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }


    }
}
