package com.example.rafa.liquidgalaxypoiscontroller;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.rafa.liquidgalaxypoiscontroller.PW.NearbyBeaconsFragment;
import com.example.rafa.liquidgalaxypoiscontroller.advancedTools.AdvancedToolsFragment;

public class AdminCollectionPagerAdapter extends FragmentStatePagerAdapter {

    public static final int PAGE_TREEEVIEW = 0;
    public static final int PAGE_TOURS = 1;
    public static final int PAGE_TOOLS = 2;
    public static final int PAGE_TASKS = 3;
    public static final int PAGE_BEACONS = 4;

    public AdminCollectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public Fragment getItem(int position) {
        Bundle args = new Bundle();
        switch (position) {
            case PAGE_TREEEVIEW:
                return NewPOISList.newInstance();
            case PAGE_TOURS:
                Fragment fragmentTours = new POISFragment();
                args.clear();
                args.putString("EDITABLE", "ADMIN/TOURS");
                fragmentTours.setArguments(args);
                return fragmentTours;
            case PAGE_TOOLS:
                return new LGTools();
            case PAGE_TASKS:
                return AdvancedToolsFragment.newInstance();
            case PAGE_BEACONS:
                return NearbyBeaconsFragment.newInstance();
            default:
                return null;
        }
    }

    public int getCount() {
        return 5;
    }

    public CharSequence getPageTitle(int position) {
        switch (position) {
            case PAGE_TREEEVIEW:
                return "CATEGORIES & POIS";
            case PAGE_TOURS:
                return "TOURS";
            case PAGE_TOOLS:
                return "TOOLS";
            case PAGE_TASKS:
                return "LG TASKS";
            case PAGE_BEACONS:
                return "SCAN BEACON";
            default:
                return "PAGE" + (position - 1);
        }
    }
}
