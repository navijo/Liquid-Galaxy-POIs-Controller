package com.example.rafa.liquidgalaxypoiscontroller;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.rafa.liquidgalaxypoiscontroller.data.POIsProvider;

public class LGPCAdminActivity extends ActionBarActivity implements TabListener {
    AdminCollectionPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;

    PendingIntent intent;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lgpcadmin);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        this.mSectionsPagerAdapter = new AdminCollectionPagerAdapter(getSupportFragmentManager());
        this.mViewPager = (ViewPager) findViewById(R.id.pager_admin);
        this.mViewPager.setAdapter(this.mSectionsPagerAdapter);
        this.mViewPager.setOnPageChangeListener(new C02741(actionBar));
        for (int i = 0; i < this.mSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(actionBar.newTab().setText(this.mSectionsPagerAdapter.getPageTitle(i)).setTabListener(this));
        }

       intent = PendingIntent.getActivity(getBaseContext(), 0, new Intent(getIntent()), PendingIntent.FLAG_ONE_SHOT);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lgpcadmin, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        else if (id == R.id.reset_db) {
            resetDatabase();
//            resetApp();
            return true;
        }
        else if (id == R.id.action_information_help) {
            startActivity(new Intent(this, Help.class));
            return true;
        } else if (id != R.id.log_out) {
            return super.onOptionsItemSelected(item);
        } else {
            startActivity(new Intent(this, LGPC.class));
            return true;
        }
    }

    public void resetDatabase() {
       //this.deleteDatabase("com.example.rafa.liquidgalaxypoiscontroller/poi_controller.db");
        ContentResolver resolver = getApplicationContext().getContentResolver();
        ContentProviderClient client = resolver.acquireContentProviderClient("com.example.rafa.liquidgalaxypoiscontroller");
        POIsProvider provider = (POIsProvider) client.getLocalContentProvider();
        provider.resetDatabase();
        client.release();
    }

    public void resetApp() {
        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000, intent);
        System.exit(2);
    }

    public void onTabSelected(Tab tab, FragmentTransaction fragmentTransaction) {
        this.mViewPager.setCurrentItem(tab.getPosition());
        Log.d(String.valueOf(tab.getPosition()), "hello");
    }

    public void onTabUnselected(Tab tab, FragmentTransaction fragmentTransaction) {
    }

    public void onTabReselected(Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /* renamed from: com.example.rafa.liquidgalaxypoiscontroller.LGPCAdminActivity.1 */
    class C02741 extends SimpleOnPageChangeListener {
        final /* synthetic */ ActionBar val$actionBar;

        C02741(ActionBar actionBar) {
            this.val$actionBar = actionBar;
        }

        public void onPageSelected(int position) {
            this.val$actionBar.setSelectedNavigationItem(position);
        }
    }
}
