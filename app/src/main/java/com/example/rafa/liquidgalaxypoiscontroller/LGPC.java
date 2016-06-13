package com.example.rafa.liquidgalaxypoiscontroller;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;

/*This is the MAIN Activity, the first that appears when the application is opened. On the
* bar there are some Tabs corresponding on some different contents.*/
public class LGPC extends ActionBarActivity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    CollectionPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lgpc);


        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new CollectionPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        showLogo();
    }

    private void showLogo() {

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.lg_logo);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_lgpc, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //echo 'planet=moon' > /tmp/query.txt


        //noinspection SimplifiableIfStatement
        if (id == R.id.action_information_help){
            Intent intent = new Intent(this, InfoActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_admin){
            //When the user decides to enter to the Administration section, first appears one
            //popup asking for a password.
            if(!POISFragment.getTourState()) {
                showPasswordAlert();
            }else{
                showAlert();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void showAlert(){
        // prepare the alert box
        final AlertDialog.Builder alertbox = new AlertDialog.Builder(LGPC.this);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // set the message to display
        alertbox.setMessage("Please, first stop the Tour.");

        // set a positive/yes button and create a listener
        alertbox.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            // When button is clicked
            public void onClick(DialogInterface arg0, int arg1) {

            }
        });
        // display box
        alertbox.show();
    }

    private void showPasswordAlert(){
        // prepare the alert box
        final AlertDialog.Builder alertbox = new AlertDialog.Builder(LGPC.this);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // set the message to display
        alertbox.setMessage("Please, enter the password:");
        final EditText input = new EditText(LGPC.this);
        input.setHint("Password");
        input.setTransformationMethod(PasswordTransformationMethod.getInstance());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertbox.setView(input);

        // set a positive/yes button and create a listener
        alertbox.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {

            // When button is clicked
            public void onClick(DialogInterface arg0, int arg1) {

                String pass = input.getText().toString();
                String correct_pass = prefs.getString("AdminPassword", "lg");
                if(pass.equals(correct_pass)){
                    Intent intent = new Intent(LGPC.this, LGPCAdminActivity.class);
                    startActivity(intent);
                }else{
                    incorrectPasswordAlertMessage();
                }
            }
        });

        // set a negative/no button and create a listener
        alertbox.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            // When button is clicked
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });
        // display box
        alertbox.show();
    }

    private void incorrectPasswordAlertMessage() {
        // prepare the alert box
        final AlertDialog.Builder alertbox = new AlertDialog.Builder(LGPC.this);

        // set the message to display
        alertbox.setTitle("Error");
        alertbox.setMessage("Incorrect password. Please, try it again or cancel the operation.");

        // set a positive/yes button and create a listener
        alertbox.setPositiveButton("Retry", new DialogInterface.OnClickListener() {

            // When button is clicked
            public void onClick(DialogInterface arg0, int arg1) {
                showPasswordAlert();
            }
        });

        // set a negative/no button and create a listener
        alertbox.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            // When button is clicked
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });
        // display box
        alertbox.show();
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

}