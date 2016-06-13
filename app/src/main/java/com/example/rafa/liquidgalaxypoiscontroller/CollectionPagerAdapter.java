package com.example.rafa.liquidgalaxypoiscontroller;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import com.jcraft.jsch.ChannelSftp;

public class CollectionPagerAdapter extends FragmentStatePagerAdapter {
    public CollectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public Fragment getItem(int position) {
        Bundle args = new Bundle();
        Fragment fr;
        switch (position) {
            case ChannelSftp.SSH_FX_OK /*0*/:
                if (POISFragment.getTourState()) {
                    POISFragment.resetTourSettings();
                }
                return new SearchFragment();
            case ChannelSftp.SSH_FX_EOF /*1*/:
                if (POISFragment.getTourState()) {
                    POISFragment.resetTourSettings();
                }
                fr = new POISFragment();
                args.putString("EDITABLE", "USER/POIS");
                fr.setArguments(args);
                return fr;
            case ChannelSftp.SSH_FX_NO_SUCH_FILE /*2*/:
                fr = new POISFragment();
                args.putString("EDITABLE", "USER/TOURS");
                fr.setArguments(args);
                return fr;
            default:
                return null;
        }
    }

    public int getCount() {
        return 3;
    }

    public CharSequence getPageTitle(int position) {
        switch (position) {
            case ChannelSftp.SSH_FX_OK /*0*/:
                return "SEARCH";
            case ChannelSftp.SSH_FX_EOF /*1*/:
                return "POIs";
            case ChannelSftp.SSH_FX_NO_SUCH_FILE /*2*/:
                return "TOURS";
            default:
                return "PAGE" + (position - 1);
        }
    }
}
