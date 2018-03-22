package com.lalala.fangs.neutv;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * Created by fang on 2018/3/22.
 */

public class SectionsPagerAdapter extends FragmentPagerAdapter {
    private ArrayList<Fragment> datas;
    private ArrayList<String> titles;

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void setData(ArrayList<Fragment> datas, ArrayList<String> titles) {
        this.datas = datas;
        this.titles = titles;
    }


    @Override
    public Fragment getItem(int position) {
        return datas == null ? null : datas.get(position);
    }

    @Override
    public int getCount() {
        return datas == null ? 0 : datas.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }

}