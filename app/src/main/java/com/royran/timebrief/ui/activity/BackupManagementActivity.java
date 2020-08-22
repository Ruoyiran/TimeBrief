package com.royran.timebrief.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.royran.timebrief.R;
import com.royran.timebrief.ui.adapter.BackupListAdapter;
import com.royran.timebrief.utils.BackupUtils;

import butterknife.BindView;
import butterknife.OnClick;

public class BackupManagementActivity extends BaseActivity {
    @BindView(R.id.text_title)
    TextView mTextTitle;

    @BindView(R.id.list_view)
    ListView mListView;

    BackupListAdapter mListAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTextTitle.setText(getString(R.string.backup_management));
        mListAdapter = new BackupListAdapter(this);
        mListAdapter.setDataList(BackupUtils.getBackupFileList());
        mListView.setAdapter(mListAdapter);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_backup_management;
    }

    @OnClick(R.id.image_home)
    protected void onBackClicked() {
        finish();
    }

    public static void openBackupManagementActivity(Context context) {
        Intent intent = new Intent(context, BackupManagementActivity.class);
        context.startActivity(intent);
    }
}
