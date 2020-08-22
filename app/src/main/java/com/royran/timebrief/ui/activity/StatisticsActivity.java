package com.royran.timebrief.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.haibin.calendarview.Calendar;
import com.orhanobut.logger.Logger;
import com.royran.timebrief.constants.Constants;
import com.royran.timebrief.models.RealmTimeRecord;
import com.royran.timebrief.utils.RealmHelper;

import com.royran.timebrief.R;

import com.royran.timebrief.ui.adapter.GroupItemDecoration;
import com.royran.timebrief.ui.adapter.MainRecyclerAdapter;
import com.royran.timebrief.ui.views.GroupRecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class StatisticsActivity extends BaseActivity {

    @BindView(R.id.recycler_main)
    GroupRecyclerView mRecyclerView;

    private MainRecyclerAdapter mRecyclerAdapter;

    private ArrayList<RealmTimeRecord> mRecords;

    private boolean mDataHasChanged = false;

    @Override
    public int getLayoutId() {
        return R.layout.activity_statistics;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        mRecords = (ArrayList<RealmTimeRecord>) bundle.getSerializable(Constants.TIME_RECORDS_EXTRA_STRING);
        if (mRecords == null) {
            Logger.e("no time records");
            return;
        }
        initRecycleView();
        updateDataAndRefreshUI(mRecords);
    }

      private void initRecycleView() {
        mRecyclerAdapter = new MainRecyclerAdapter(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.addItemDecoration(new GroupItemDecoration<String, RealmTimeRecord>());
    }

    public void updateDataAndRefreshUI(List<RealmTimeRecord> records) {
        mRecyclerAdapter.setDataList(records, new Calendar());
        mRecyclerView.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == Constants.OPEN_EDIT_ENTRY_ACTIVITY_REQUEST_CODE) {
            boolean isChanged = data.getBooleanExtra(Constants.DATA_IS_CHANGED_EXTRA_STRING, false);
            Logger.i("isChanged: %s", isChanged);
            if (isChanged) {
                RealmTimeRecord record = (RealmTimeRecord) data.getSerializableExtra(Constants.TIME_RECORD_EXTRA_STRING);
                Logger.d("getActivityDateMillis: %d",record.getActivityDateMillis());
                RealmHelper.updateTimeRecord(record);
                updateRecord(record);
                updateDataAndRefreshUI(mRecords);
                mDataHasChanged = true;
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = StatisticsActivity.getBundleIntent(this, mRecords);
        intent.putExtra(Constants.DATA_IS_CHANGED_EXTRA_STRING, mDataHasChanged);
        this.setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    private static Intent getBundleIntent(Context activity, ArrayList<RealmTimeRecord> records) {
        Intent intent = new Intent(activity, StatisticsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.TIME_RECORDS_EXTRA_STRING, records);
        intent.putExtras(bundle);
        return intent;
    }

    public static void openStatisticsActivity(Fragment fragment, ArrayList<RealmTimeRecord> records, int requestCode) {
        Intent intent = getBundleIntent(fragment.getContext(), records);
        fragment.startActivityForResult(intent, requestCode);
    }

    private void updateRecord(RealmTimeRecord record) {
        for (int i = 0; i < mRecords.size(); ++i) {
            if (mRecords.get(i).getId() == record.getId()) {
                mRecords.set(i, record);
                break;
            }
        }
    }
}
