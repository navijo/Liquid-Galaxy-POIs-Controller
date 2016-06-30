package com.example.rafa.liquidgalaxypoiscontroller;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.rafa.liquidgalaxypoiscontroller.PW.NearbyBeaconsFragment;
import com.example.rafa.liquidgalaxypoiscontroller.advancedTools.AdvancedToolsFragment;
import com.jcraft.jsch.ChannelSftp;

public class AdminCollectionPagerAdapter extends FragmentStatePagerAdapter {
    public AdminCollectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public Fragment getItem(int position) {
        Bundle args = new Bundle();
        switch (position) {
            case ChannelSftp.SSH_FX_OK /*0*/:
                Fragment fragmentPOIs = new POISFragment();
                args.clear();
                args.putString("EDITABLE", "ADMIN/POIS");
                fragmentPOIs.setArguments(args);
                return fragmentPOIs;
            case ChannelSftp.SSH_FX_EOF /*1*/:
                Fragment fragmentTours = new POISFragment();
                args.clear();
                args.putString("EDITABLE", "ADMIN/TOURS");
                fragmentTours.setArguments(args);
                return fragmentTours;
            case ChannelSftp.SSH_FX_NO_SUCH_FILE /*2*/:
                Fragment fragmentCategories = new POISFragment();
                args.clear();
                args.putString("EDITABLE", "ADMIN/CATEGORIES");
                fragmentCategories.setArguments(args);
                return fragmentCategories;
            case ChannelSftp.SSH_FX_PERMISSION_DENIED /*3*/:
                return new LGTools();
            case 4 /*4*/:
                return NearbyBeaconsFragment.newInstance();
            case 5 /*5*/:
                return AdvancedToolsFragment.newInstance();
            default:
                return null;
        }
    }

    public int getCount() {
        return 6;
    }

    public CharSequence getPageTitle(int position) {
        switch (position) {
            case ChannelSftp.SSH_FX_OK /*0*/:
                return "POIS";
            case ChannelSftp.SSH_FX_EOF /*1*/:
                return "TOURS";
            case ChannelSftp.SSH_FX_NO_SUCH_FILE /*2*/:
                return "CATEGORIES";
            case ChannelSftp.SSH_FX_PERMISSION_DENIED /*3*/:
                return "TOOLS";
            case 4 /*4*/:
                return "SCAN BEACON";
            case 5 /*5*/:
                return "ADVANCED TOOLS";
            default:
                return "PAGE" + (position - 1);
        }
    }
}
