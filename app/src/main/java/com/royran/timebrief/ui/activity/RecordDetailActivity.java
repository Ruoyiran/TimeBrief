package com.royran.timebrief.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.orhanobut.logger.Logger;
import com.royran.timebrief.constants.Constants;
import com.royran.timebrief.models.RealmTimeRecord;

import com.royran.timebrief.R;

import com.royran.timebrief.ui.adapter.TimeRecordDetailRecyclerAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

public class RecordDetailActivity extends BaseActivity implements BaseActivity.OnActivityResultListener {
    private ArrayList<RealmTimeRecord> mRecords;

    @BindView(R.id.text_title)
    TextView mTextTitle;

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    TimeRecordDetailRecyclerAdapter mRecyclerAdapter;

    @Override
    public int getLayoutId() {
        return R.layout.activity_record_detail;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        mRecords = (ArrayList<RealmTimeRecord>) bundle.getSerializable(Constants.TIME_RECORDS_EXTRA_STRING);
        if (mRecords == null || mRecords.isEmpty()) {
            Logger.e("no time records");
            return;
        }
        addOnActivityResultListener(this);
        mRecyclerAdapter = new TimeRecordDetailRecyclerAdapter(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerAdapter.setDataList(mRecords, null);
    }

    public void setTitle(String title) {
        mTextTitle.setText(title);
    }

    @Override
    public void onBackPressed() {
        this.setResult(RESULT_OK);
        super.onBackPressed();
    }

    @OnClick(R.id.image_home)
    void onBackClicked() {
        onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.d("onActivityResult - requestCode: %d, resultCode: %d", requestCode, resultCode);
        if (resultCode == RESULT_OK && requestCode == Constants.OPEN_EDIT_ENTRY_ACTIVITY_REQUEST_CODE) {
            boolean isChanged = data.getBooleanExtra(Constants.DATA_IS_CHANGED_EXTRA_STRING, false);
            Logger.d("onActivityResult isChanged: %s", isChanged);
            if (isChanged) {
                RealmTimeRecord record = (RealmTimeRecord) data.getSerializableExtra(Constants.TIME_RECORD_EXTRA_STRING);
                Logger.d("getActivityDateMillis: %d", record.getActivityDateMillis());
                updateRecord(record);
                mRecyclerAdapter.setDataList(mRecords, null);
            }
        }
    }

    private void updateRecord(RealmTimeRecord record) {
        for (int i = 0; i < mRecords.size(); ++i) {
            if (mRecords.get(i).getId() == record.getId()) {
                mRecords.set(i, record);
                break;
            }
        }
    }

    private static Intent getBundleIntent(Context activity, ArrayList<RealmTimeRecord> records) {
        Intent intent = new Intent(activity, RecordDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.TIME_RECORDS_EXTRA_STRING, records);
        intent.putExtras(bundle);
        return intent;
    }

    public static void open(Activity activity, ArrayList<RealmTimeRecord> records, int requestCode) {
        Intent intent = getBundleIntent(activity, records);
        activity.startActivityForResult(intent, requestCode);
    }
}
