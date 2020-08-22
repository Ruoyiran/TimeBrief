package com.royran.timebrief.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.royran.timebrief.utils.BackupUtils;
import com.royran.timebrief.utils.ShareHelper;

import com.royran.timebrief.R;

import java.io.File;
import java.util.List;

public class BackupListAdapter extends BaseListAdapter<File> {
    Context mContext;
    private LayoutInflater mLayoutInflater;

    public BackupListAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (mItems == null) {
            return new View(mContext);
        }
        ListItemViewHolder viewHolder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.item_list, null);
            viewHolder = new ListItemViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ListItemViewHolder) convertView.getTag();
        }
        viewHolder.setData(mItems.get(position));
        return convertView;
    }

    public void setDataList(List<File> dataList) {
        mItems = dataList;
    }

    public class ListItemViewHolder {
        private TextView mTextItem;
        private TextView mTextDesc;
        private File mBackupFile;

        public ListItemViewHolder(View view) {
            if (view == null) {
                return;
            }
            mTextItem = view.findViewById(R.id.text_item);
            mTextDesc = view.findViewById(R.id.text_desc);
            view.findViewById(R.id.layout_list_item).setOnClickListener(v -> {
                if (mBackupFile == null) {
                    return;
                }
                showDialog(mContext, mBackupFile);
            });
        }

        public void setItemText(File file) {
            if (mTextItem == null || file == null) {
                return;
            }
            mTextItem.setText(file.getName());
            setDescText(file);
        }

        void setDescText(File file) {
            if (mTextDesc == null || file == null) {
                return;
            }
            String metrics = "B";
            float size = (float)(file.length());
            if (size >= 1024) {
                metrics = "KB";
                size /= 1024.0f;
                if (size >= 1024) {
                    metrics = "MB";
                    size /= 1024.0f;
                }
                size = ((int)(size*100))/100.0f;
            }
            mTextDesc.setText(size + metrics);
        }

        public void setData(File file) {
            setItemText(file);
            mBackupFile = file;
        }
    }

    private void showDialog(final Context context, final File file) {
        new MaterialDialog.Builder(context).
                content(file.getName()).
                neutralText("分享").
                positiveText("恢复备份").
                negativeText("删除").
                onPositive(
                        (dialog, which) -> BackupUtils.restoreData(context, file)).
                onNegative(
                        (dialog, which) -> {
                            BackupUtils.deleteBackupFile(context, file, new BackupUtils.OnDeleteFileListener() {
                                @Override
                                public void onDeleteSuccess() {
                                    setDataList(BackupUtils.getBackupFileList());
                                    notifyDataSetChanged();
                                }
                                @Override
                                public void onDeleteFailed() {
                                }
                            });
                        }).
                onNeutral(
                        (dialog, which) -> ShareHelper.shareFile(context, file.getAbsolutePath(), "分享备份文件"))
                .show();
    }

}
