package com.example.rafa.liquidgalaxypoiscontroller;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class CollectionPagerAdapter extends FragmentStatePagerAdapter {

    private static final int SEARCH = 0;
    private static final int PAGE_TOURS = 1;

    public CollectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public Fragment getItem(int position) {
        switch (position) {
            case SEARCH:
                if (POISFragment.getTourState()) {
                    POISFragment.resetTourSettings();
                }
                return new SearchFragment();
            case PAGE_TOURS:
                return new TourUserFragment();
            default:
                return null;
        }
    }

    public int getCount() {
        return 2;
    }

    public CharSequence getPageTitle(int position) {
        switch (position) {
            case SEARCH:
                return "SEARCH";
            case PAGE_TOURS:
                return "TOURS";
            default:
                return "PAGE" + (position - 1);
        }
    }
}
