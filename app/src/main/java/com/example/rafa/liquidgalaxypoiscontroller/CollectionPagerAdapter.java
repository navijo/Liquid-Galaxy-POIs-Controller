package com.example.rafa.liquidgalaxypoiscontroller;

import android.os.Bundle;
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
        Bundle args = new Bundle();
        Fragment fr;
        switch (position) {

            case SEARCH:
                if (POISFragment.getTourState()) {
                    POISFragment.resetTourSettings();
                }
                return new SearchFragment();
            case PAGE_TOURS:
                fr = new POISFragment();
                args.putString("EDITABLE", "USER/TOURS");
                fr.setArguments(args);
                return fr;
//            case ChannelSftp.SSH_FX_OK /*0*/:
//                if (POISFragment.getTourState()) {
//                    POISFragment.resetTourSettings();
//                }
//                return new SearchFragment();
//            case ChannelSftp.SSH_FX_EOF /*1*/:
//                if (POISFragment.getTourState()) {
//                    POISFragment.resetTourSettings();
//                }
//                fr = new POISFragment();
//                args.putString("EDITABLE", "USER/POIS");
//                fr.setArguments(args);
//                return fr;
//            case ChannelSftp.SSH_FX_NO_SUCH_FILE /*2*/:
//                fr = new POISFragment();
//                args.putString("EDITABLE", "USER/TOURS");
//                fr.setArguments(args);
//                return fr;
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
//            case ChannelSftp.SSH_FX_OK /*0*/:
//                return "SEARCH";
//            case ChannelSftp.SSH_FX_EOF /*1*/:
//                return "POIs";
//            case ChannelSftp.SSH_FX_NO_SUCH_FILE /*2*/:
//                return "TOURS";
            default:
                return "PAGE" + (position - 1);
        }
    }
}
