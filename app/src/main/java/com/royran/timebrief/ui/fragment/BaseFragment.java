package com.royran.timebrief.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.orhanobut.logger.Logger;

import butterknife.ButterKnife;

public abstract class BaseFragment
        extends Fragment {

    ViewGroup mViewGroup;

    protected abstract int getLayoutResourceId();

    protected abstract void onCreateView();

    public abstract void onEnable();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.d("BaseFragment::onCreateView");
        if (mViewGroup == null) {
            mViewGroup = ((ViewGroup) inflater.inflate(getLayoutResourceId(), container, false));
            ButterKnife.bind(this, mViewGroup);
            onCreateView();
        } else {
            ViewGroup view = (ViewGroup) mViewGroup.getParent();
            if (view != null) {
                view.removeView(mViewGroup);
            }
        }
        return mViewGroup;
    }

}
