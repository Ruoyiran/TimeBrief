package com.royran.timebrief.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.royran.timebrief.R;
import com.royran.timebrief.ui.views.searchview.SearchView;

import butterknife.BindView;

/**
 * Created by Carson_Ho on 17/8/11.
 */

public class SearchActivity extends BaseActivity {

    @BindView(R.id.search_view)
    SearchView searchView;
    private static OnSearchListener mOnSearchListener;

    public interface OnSearchListener {
        void onSearch(String text);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_search;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        searchView.setOnClickSearch(text -> {
            if (mOnSearchListener != null) {
                mOnSearchListener.onSearch(text);
            }
            finish();
        });

        searchView.setOnClickBack(() -> finish());
    }

    public static void open(Context context, OnSearchListener listener) {
        SearchActivity.mOnSearchListener = listener;
        context.startActivity(new Intent(context, SearchActivity.class));
    }
}