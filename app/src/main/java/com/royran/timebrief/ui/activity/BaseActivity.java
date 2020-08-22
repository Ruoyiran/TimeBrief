package com.royran.timebrief.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity {

    public abstract int getLayoutId();

    public List<OnActivityResultListener> onActivityResultListeners;

    private static BaseActivity mInstance;

    public static BaseActivity getInstance() {
        return mInstance;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInstance = this;
        Logger.i("BaseActivity:onCreate");
        setContentView(getLayoutId());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        ButterKnife.bind(this);
        onActivityResultListeners = new ArrayList<>();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Logger.i("BaseActivity:onStart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Logger.i("BaseActivity:onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logger.i("BaseActivity:onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.i("BaseActivity:onDestroy");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.d("BaseActivity::onActivityResult - requestCode: %d, resultCode: %d", requestCode, resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        for (OnActivityResultListener listener : onActivityResultListeners) {
            if (listener != null) {
                listener.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    public interface OnActivityResultListener {
        void onActivityResult(int requestCode, int resultCode, Intent data);
    }

    public void addOnActivityResultListener(OnActivityResultListener listener) {
        if (listener == null) {
            return;
        }
        onActivityResultListeners.add(listener);
    }
}
