package com.example.rafa.liquidgalaxypoiscontroller;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by RAFA on 10/08/2015.
 */
public class CollectionPagerAdapter extends FragmentStatePagerAdapter {
    public CollectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Bundle args = new Bundle();
        Fragment fr;
        switch (position) {
            case 0:
                if(POISFragment.getTourState()){
                    POISFragment.resetTourSettings();
                }
                fr = new SearchFragment();//NOU FRAGMENT AMB SEARCH
                return fr;

            case 1:
                if(POISFragment.getTourState()){
                    POISFragment.resetTourSettings();
                }
                fr = new POISFragment();
                args.putString("EDITABLE", "USER/POIS");
                fr.setArguments(args);
                return fr;

            case 2:
                fr = new POISFragment();
                args.putString("EDITABLE", "USER/TOURS");
                fr.setArguments(args);
                return fr;
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "SEARCH";
            case 1:
                return "POIs";
            case 2:
                return "TOURS";
            default:
                return "PAGE" + (position - 1);
        }
    }
}
