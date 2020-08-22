package com.royran.timebrief.ui.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.royran.timebrief.ui.fragment.BaseFragment;

import java.util.List;

public class MainViewPagerAdapter
        extends FragmentPagerAdapter {
    List<BaseFragment> mFragments;

    public MainViewPagerAdapter(FragmentManager fragmentManager, List<BaseFragment> fragments) {
        super(fragmentManager);
        mFragments = fragments;
    }

    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        super.destroyItem(container, position, object);
    }

    public int getCount() {
        return mFragments.size();
    }

    public List<BaseFragment> getFragmentList() {
        return mFragments;
    }

    public Fragment getItem(int position) {
        if (position < 0 || position >= mFragments.size()) {
            return null;
        }
        return mFragments.get(position);
    }
}