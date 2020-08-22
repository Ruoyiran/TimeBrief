package com.royran.timebrief.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.protobuf.InvalidProtocolBufferException;
import com.orhanobut.logger.Logger;
import com.royran.timebrief.R;
import com.royran.timebrief.archive.FileArchiveManager;
import com.royran.timebrief.constants.Constants;
import com.royran.timebrief.events.DataLoadedEvent;
import com.royran.timebrief.models.RealmTimeRecord;
import com.royran.timebrief.pb.RealmTimeRecordPb;
import com.royran.timebrief.ssl.RpmSSL;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BackupUtils {
    public final static String DEFAULT_SERVER_ADDR = "http://192.168.1.100:59000";
    public final static String PREF_SERVER_ADDR = "pref_server_addr";

    public static List<File> getBackupFileList() {
        final File backupDir = new File(FileArchiveManager.getBackupDirectory());
        Logger.d("backupDir: " + backupDir.getAbsolutePath());
        File[] allFiles = backupDir.listFiles();
        if (allFiles == null || allFiles.length <= 0) {
            return new ArrayList<>();
        }
        List<File> buckupFiles = new ArrayList<>();
        for (File file : allFiles) {
            if (!file.isFile()) {
                continue;
            }
            if (!file.getName().endsWith(Constants.BACKUP_FILE_EXTENSION)) {
                continue;
            }
            buckupFiles.add(file);
        }
        Collections.sort(buckupFiles, (f1, f2) -> {
            if (f2.lastModified() > f1.lastModified()) {
                return 1;
            } else if (f2.lastModified() < f1.lastModified()) {
                return -1;
            }
            return 0;
        });
        return buckupFiles;
    }

    public static void restoreData(Context context, File file) {
        if (file == null) {
            Logger.e("file is null");
            Toast.makeText(context, context.getString(R.string.restore_failed), Toast.LENGTH_SHORT).show();
            return;
        }
        byte[] data = FileUtils.readBytes(file);
        byte[] decData = RpmSSL.decryptBytes(data);
        if (decData == null || decData.length == 0) {
            Toast.makeText(context, context.getString(R.string.read_data_error), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            RealmTimeRecordPb.RealmTimeRecords pbRecords = RealmTimeRecordPb.RealmTimeRecords.parseFrom(decData);
            List<RealmTimeRecordPb.RealmTimeRecord> records = pbRecords.getRecordsList();
            List<RealmTimeRecord> allRecords = new ArrayList<>();
            for (RealmTimeRecordPb.RealmTimeRecord r : records) {
                RealmTimeRecord newRecord = TimeRecordUtils.copyRealmTimeRecordFromPb(r);
                allRecords.add(newRecord);
            }
            Logger.i("restore data, data size: %d, records: %d", decData.length, pbRecords.getRecordsCount());
            if (allRecords.size() > 0) {
                showConfirmDialogBeforeRestore(context, file.getName(), allRecords);
            } else {
                Toast.makeText(context, context.getString(R.string.no_data), Toast.LENGTH_SHORT).show();
            }
        } catch (InvalidProtocolBufferException e) {
            Toast.makeText(context, context.getString(R.string.parse_data_failed), Toast.LENGTH_SHORT).show();
            Logger.e("InvalidProtocolBufferException: %s", e.getMessage());
        }
    }

    public static void restoreData(Context context, byte[] data) {
        byte[] decData = RpmSSL.decryptBytes(data);
        if (decData == null || decData.length == 0) {
            Toast.makeText(context, context.getString(R.string.no_data), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            RealmTimeRecordPb.RealmTimeRecords pbRecords = RealmTimeRecordPb.RealmTimeRecords.parseFrom(decData);
            List<RealmTimeRecordPb.RealmTimeRecord> records = pbRecords.getRecordsList();
            List<RealmTimeRecord> allRecords = new ArrayList<>();
            for (RealmTimeRecordPb.RealmTimeRecord r : records) {
                RealmTimeRecord newRecord = TimeRecordUtils.copyRealmTimeRecordFromPb(r);
                allRecords.add(newRecord);
            }
            Logger.i("restore data, data size: %d, records: %d", decData.length, pbRecords.getRecordsCount());
            if (allRecords.size() > 0) {
                new MaterialDialog.Builder(context).
                        content(context.getString(R.string.restore_hint, records.size())).
                        positiveText(context.getString(R.string.ok)).
                        negativeText(context.getString(R.string.cancel)).onPositive(
                        (dialog, which) -> {
                            RealmHelper.removeAllRecords();
                            RealmHelper.addTimeRecords(allRecords);
                            Toast.makeText(context, context.getString(R.string.restore_success), Toast.LENGTH_SHORT).show();
                            EventBus.getDefault().post(new DataLoadedEvent());
                        }
                ).show();
            } else {
                Toast.makeText(context, context.getString(R.string.no_data), Toast.LENGTH_SHORT).show();
            }
        } catch (InvalidProtocolBufferException e) {
            Toast.makeText(context, context.getString(R.string.parse_data_failed), Toast.LENGTH_SHORT).show();
            Logger.e("InvalidProtocolBufferException: %s", e.getMessage());
        }
    }

    private static void showConfirmDialogBeforeRestore(Context context, String filename, final List<RealmTimeRecord> records) {
        new MaterialDialog.Builder(context).
                content(context.getString(R.string.restore_hint2, records.size())).
                positiveText(context.getString(R.string.ok)).
                negativeText(context.getString(R.string.cancel)).onPositive(
                (dialog, which) -> {
                    RealmHelper.removeAllRecords();
                    RealmHelper.addTimeRecords(records);
                    Toast.makeText(context, context.getString(R.string.restore_success), Toast.LENGTH_SHORT).show();
                }
        ).show();
    }

    public static void deleteBackupFile(Context context, File file, OnDeleteFileListener listener) {
        new MaterialDialog.Builder(context).
                content(context.getString(R.string.delete_confirm)).
                positiveText(context.getString(R.string.ok)).
                negativeText(context.getString(R.string.cancel)).
                onPositive((dialog, which) -> {
                    if (FileUtils.deleteFile(file)) {
                        Toast.makeText(context, context.getString(R.string.delete_success), Toast.LENGTH_SHORT).show();
                        if (listener != null)
                            listener.onDeleteSuccess();
                    } else {
                        Toast.makeText(context, context.getString(R.string.delete_failed), Toast.LENGTH_SHORT).show();
                        if (listener != null)
                            listener.onDeleteFailed();
                    }
                }).show();
    }

    public static byte[] getBackupData() {
        List<RealmTimeRecord> records = RealmHelper.getAllTimeRecords();
        if (records == null || records.isEmpty()) {
            return null;
        }
        RealmTimeRecordPb.RealmTimeRecords.Builder builder = RealmTimeRecordPb.RealmTimeRecords.newBuilder();
        for (RealmTimeRecord r : records) {
            RealmTimeRecordPb.RealmTimeRecord newRecord = TimeRecordUtils.copyRealmTimeRecordToPb(r);
            builder.addRecords(newRecord);
        }
        RealmTimeRecordPb.RealmTimeRecords pbRecords = builder.build();
        byte[] data = pbRecords.toByteArray();
        byte[] encData = RpmSSL.encryptBytes(data);
        Logger.i("backup data, data size: %d, records: %d", data.length, pbRecords.getRecordsCount());
        if (encData == null) {
            Logger.e("failed to encrypt data");
        } else {
            Logger.i("encrypt data success, data size: %d, enc data size: %d", data.length, encData.length);
        }
        return encData;
    }

    public static void setBackupServerAddr(String addr) {
        SharedPreferenceUtils.write(PREF_SERVER_ADDR, addr);
    }

    public static String getBackupServerAddr() {
        return SharedPreferenceUtils.read(PREF_SERVER_ADDR, DEFAULT_SERVER_ADDR);
    }

    public static void uploadData(Activity activity) {
        String url = getBackupServerAddr() + "/api/v1/timebrief/upload";
        Logger.i("uploadData - post " + url);
        byte[] data = getBackupData();
        if (data == null) {
            Toast.makeText(activity, activity.getString(R.string.no_data), Toast.LENGTH_LONG).show();
            return;
        }
        ProgressDialog progressDialog = ProgressDialog.show(activity, activity.getString(R.string.wait), activity.getString(R.string.in_request));
        HttpUtils.post(activity, url, data, new HttpCallback() {
            @Override
            public void onSuccess(byte[] body) {
                progressDialog.dismiss();
                Logger.i("upload success");
                Toast.makeText(activity, activity.getString(R.string.upload_success), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception error) {
                progressDialog.dismiss();
                Toast.makeText(activity, activity.getString(R.string.upload_failed), Toast.LENGTH_SHORT).show();
                Logger.e(error.getMessage() + "");
            }
        });
    }

    public static void downloadData(final Activity activity) {
        final ProgressDialog progressDialog = ProgressDialog.show(activity, activity.getString(R.string.wait), activity.getString(R.string.in_request));
        String url = getBackupServerAddr() + "/api/v1/timebrief/download";
        Logger.i("downloadData - post " + url);
        HttpUtils.post(activity, url, null, new HttpCallback() {
            @Override
            public void onSuccess(byte[] body) {
                progressDialog.dismiss();
                if (body == null) {
                    Toast.makeText(activity, activity.getString(R.string.no_data), Toast.LENGTH_SHORT).show();
                    return;
                }
                Logger.i("onSuccess, data length: %d", body.length);
                restoreData(activity, body);
            }

            @Override
            public void onFailure(Exception error) {
                progressDialog.dismiss();
                Toast.makeText(activity, activity.getString(R.string.download_failed), Toast.LENGTH_SHORT).show();
                Logger.e(error.getMessage() + "");
            }
        });
    }

    public static void backupData(Context context) {
        byte[] data = BackupUtils.getBackupData();
        if (data == null) {
            Toast.makeText(context, context.getString(R.string.no_data), Toast.LENGTH_LONG).show();
            return;
        }
        String filename = FileArchiveManager.getBackupFilename();
        boolean ok = FileUtils.saveToFile(FileArchiveManager.getBackupDirectory(), filename, data);
        if (ok) {
            Toast.makeText(context, context.getString(R.string.save_file_to) + FileArchiveManager.getBackupDirectory() + "/" + filename, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, context.getString(R.string.save_failed), Toast.LENGTH_SHORT).show();
        }
    }

    public interface OnDeleteFileListener {
        void onDeleteSuccess();

        void onDeleteFailed();
    }
}
