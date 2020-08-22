package com.royran.timebrief.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

public class StuckViewPager
        extends ViewPager {
    private boolean mIsScrollable = false;

    public StuckViewPager(Context context) {
        super(context);
    }

    public StuckViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean isScrollable() {
        return mIsScrollable;
    }

    public void setScrollable(boolean scrollable) {
        this.mIsScrollable = scrollable;
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        return false;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!this.mIsScrollable) {
            return false;
        }
        return super.onTouchEvent(event);
    }
}
