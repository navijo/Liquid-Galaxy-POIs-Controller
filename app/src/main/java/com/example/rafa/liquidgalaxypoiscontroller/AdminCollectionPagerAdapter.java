package com.example.rafa.liquidgalaxypoiscontroller;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by RAFA on 10/08/2015.
 */
public class AdminCollectionPagerAdapter extends FragmentStatePagerAdapter {
    public AdminCollectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Bundle args = new Bundle();

        switch (position) {
            case 0:
                POISFragment fragmentPOIs = new POISFragment();
                args.clear();
                args.putString("EDITABLE", "ADMIN/POIS");
                fragmentPOIs.setArguments(args);
                return fragmentPOIs;
            case 1:
                POISFragment fragmentTours = new POISFragment();
                args.clear();
                args.putString("EDITABLE", "ADMIN/TOURS");
                fragmentTours.setArguments(args);
                return fragmentTours;
            case 2:
                POISFragment fragmentCategories = new POISFragment();
                args.clear();
                args.putString("EDITABLE", "ADMIN/CATEGORIES");
                fragmentCategories.setArguments(args);
                return fragmentCategories;
            case 3:
                LGTools toolsFragment = new LGTools();
                return toolsFragment;
        }
        return null;
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "POIS";
            case 1:
                return "TOURS";
            case 2:
                return "CATEGORIES";
            case 3:
                return "LIQUID GALAXY TOOLS";
            default:
                return "PAGE" + (position - 1);
        }
    }

}
