package com.royran.timebrief.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import com.google.android.material.tabs.TabLayout;
import com.orhanobut.logger.Logger;
import com.royran.timebrief.R;
import com.royran.timebrief.R2;
import com.royran.timebrief.ui.adapter.MainViewPagerAdapter;
import com.royran.timebrief.ui.fragment.BaseFragment;
import com.royran.timebrief.ui.fragment.SettingsFragment;
import com.royran.timebrief.ui.fragment.StatisticsFragment;
import com.royran.timebrief.ui.fragment.TodayFragment;
import com.royran.timebrief.ui.views.StuckViewPager;
import com.royran.timebrief.utils.BackupUtils;
import com.royran.timebrief.utils.SharedPreferenceUtils;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;

public class MainActivity extends BaseActivity {
    private final static String PREF_IS_FIRST_RUN = "is_first_run";
    private final static String PREF_LAST_BACKUP_TIME = "last_backup_time";
    private final static int REQUEST_WRITE_EXTERNAL_STORAGE_CODE = 100;

    @BindView(R2.id.view_pager)
    StuckViewPager mViewPager;

    @BindView(R2.id.bottom_tab_bar)
    TabLayout mBottomTabBar;

    MainViewPagerAdapter mViewPagerAdapter;

    ArrayList<BaseFragment> mFragments;
    private final static long BACKUP_TIME_FREQ = 24 * 60 * 60 * 1000; // 1天

    static {
        try {
            System.loadLibrary("rpmssl");
            Logger.i("load lib success");
        } catch (Exception e) {
            Logger.e("load lib failed, error: %s", e.getMessage());
        }
    }

    private static MainActivity instance;

    public static MainActivity getInstance() {
        return instance;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        Logger.i("MainActivity:onCreate");
        initFragments();
        initBottomTabs();
        if (!hasSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_EXTERNAL_STORAGE_CODE);
        } else {
            backupDataFirst();
        }
    }

    private boolean hasSelfPermission(String permission) {
        try {
            if (PermissionChecker.checkSelfPermission(this, permission) == PermissionChecker.PERMISSION_GRANTED) {
                return true;
            }
            return false;
        } catch (RuntimeException t) {
            return false;
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Logger.i("MainActivity:onSaveInstanceState");
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Logger.i("MainActivity:onRestoreInstanceState");
    }

    public static int getScreenWidth(Activity activity) {
        if (activity == null) {
            return 0;
        }
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

    private void initBottomTabs() {
        View tabView1 = LayoutInflater.from(this).inflate(R.layout.tab_item_main, null);
        View tabView2 = LayoutInflater.from(this).inflate(R.layout.tab_item_main, null);
        View tabView3 = LayoutInflater.from(this).inflate(R.layout.tab_item_main, null);
        ((ImageView) tabView1.findViewById(R.id.image_icon)).setImageResource(R.drawable.ic_tab_today);
        ((ImageView) tabView2.findViewById(R.id.image_icon)).setImageResource(R.drawable.ic_tab_analysis);
        ((ImageView) tabView3.findViewById(R.id.image_icon)).setImageResource(R.drawable.ic_tab_more);
        tabView3.findViewById(R.id.view_line).setVisibility(View.GONE);
        int width = (int) (getScreenWidth(this) / 3.0);
        tabView1.setLayoutParams(new RelativeLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT));
        tabView2.setLayoutParams(new RelativeLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT));
        tabView3.setLayoutParams(new RelativeLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT));
        mBottomTabBar.addTab(mBottomTabBar.newTab().setCustomView(tabView1));
        mBottomTabBar.addTab(mBottomTabBar.newTab().setCustomView(tabView2));
        mBottomTabBar.addTab(mBottomTabBar.newTab().setCustomView(tabView3));
        Objects.requireNonNull(mBottomTabBar.getTabAt(0)).select();
        mBottomTabBar.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Logger.i("onTabSelected: %d", tab.getPosition());
                mViewPager.setCurrentItem(tab.getPosition(), false);
                mFragments.get(tab.getPosition()).onEnable();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    public void jumpToTodayPage(long timeMs) {
        mBottomTabBar.getTabAt(0).select();
        mViewPager.setCurrentItem(0, false);
        ((TodayFragment) mFragments.get(0)).selectCalendar(timeMs);
    }

    public StatisticsFragment getStatisticsFragment() {
        return ((StatisticsFragment) mFragments.get(1));
    }

    private void initFragments() {
        mFragments = new ArrayList();
        mFragments.add(TodayFragment.newInstance());
        mFragments.add(StatisticsFragment.newInstance());
        mFragments.add(SettingsFragment.newInstance());
        mViewPagerAdapter = new MainViewPagerAdapter(getSupportFragmentManager(), mFragments);
        mViewPager.setAdapter(mViewPagerAdapter);
    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    @Override
    public void onRequestPermissionsResult(@SuppressLint("NeedOnRequestPermissionsResult") int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
                backupDataFirst();
            } else {
                boolean isFirstRun = SharedPreferenceUtils.read(PREF_IS_FIRST_RUN, true);
                if (isFirstRun) {
                    BackupUtils.downloadData(this);
                    SharedPreferenceUtils.write(PREF_IS_FIRST_RUN, false);
                }
            }
        }
    }

    private void backupDataFirst() {
        boolean isFirstRun = SharedPreferenceUtils.read(PREF_IS_FIRST_RUN, true);
        Log.i("MainActivity", "requestWriteExternalStorage - isFirstRun: " + isFirstRun);
        if (isFirstRun) {
            BackupUtils.downloadData(this);
            SharedPreferenceUtils.write(PREF_IS_FIRST_RUN, false);
        } else {
            long lastBackupTime = SharedPreferenceUtils.read(PREF_LAST_BACKUP_TIME, 0L);
            if (System.currentTimeMillis() - lastBackupTime > BACKUP_TIME_FREQ) {
                BackupUtils.backupData(this);
                BackupUtils.uploadData(this);
                SharedPreferenceUtils.write(PREF_LAST_BACKUP_TIME, System.currentTimeMillis());
            }
        }
    }
}
