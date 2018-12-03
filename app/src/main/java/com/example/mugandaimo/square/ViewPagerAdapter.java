package com.example.mugandaimo.square;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Muganda Imo on 7/23/2018.
 */

class ViewPagerAdapter extends FragmentPagerAdapter {

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;

        switch (position){
            case 0:
                fragment = new Chats();
                return fragment;
            case 1:
                fragment = new Friends();
                return fragment;
            case 2:
                fragment = new Groups();
                return fragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    public CharSequence getPageTitle(int position){
        switch (position){
            case 0:
                return "Chats";
            case 1:
                return "Friends";
            case 2:
                return "Groups";
            default:
                return null;
        }
    }
}
