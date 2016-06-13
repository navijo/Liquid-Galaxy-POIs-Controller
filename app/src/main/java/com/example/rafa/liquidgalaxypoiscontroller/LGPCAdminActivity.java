package com.example.rafa.liquidgalaxypoiscontroller;

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

public class LGPCAdminActivity extends ActionBarActivity implements TabListener {
    AdminCollectionPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;

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

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_lgpcadmin);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        this.mSectionsPagerAdapter = new AdminCollectionPagerAdapter(getSupportFragmentManager());
        this.mViewPager = (ViewPager) findViewById(R.id.pager_admin);
        this.mViewPager.setAdapter(this.mSectionsPagerAdapter);
        this.mViewPager.setOnPageChangeListener(new C02741(actionBar));
        for (int i = 0; i < this.mSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(actionBar.newTab().setText(this.mSectionsPagerAdapter.getPageTitle(i)).setTabListener(this));
        }
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
        } else if (id == R.id.action_information_help) {
            startActivity(new Intent(this, Help.class));
            return true;
        } else if (id != R.id.log_out) {
            return super.onOptionsItemSelected(item);
        } else {
            startActivity(new Intent(this, LGPC.class));
            return true;
        }
    }

    public void onTabSelected(Tab tab, FragmentTransaction fragmentTransaction) {
        this.mViewPager.setCurrentItem(tab.getPosition());
        Log.d(String.valueOf(tab.getPosition()), "hello");
    }

    public void onTabUnselected(Tab tab, FragmentTransaction fragmentTransaction) {
    }

    public void onTabReselected(Tab tab, FragmentTransaction fragmentTransaction) {
    }
}
