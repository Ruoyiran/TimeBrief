package com.royran.timebrief.archive;

import android.os.Environment;
import android.text.format.DateFormat;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.royran.timebrief.R;
import com.royran.timebrief.constants.Constants;
import com.royran.timebrief.models.RealmTimeRecord;
import com.royran.timebrief.pb.RealmTimeRecordPb;
import com.royran.timebrief.ssl.RpmSSL;
import com.royran.timebrief.utils.RealmHelper;
import com.royran.timebrief.utils.TimeRecordUtils;

import java.io.File;
import java.util.Calendar;
import java.util.List;

public class FileArchiveManager {
    private static final String TIME_BRIEF_ROOT_DIR = "TimeBrief";
    public static String getBackupDirectory() {
        final File dir = Environment.getExternalStoragePublicDirectory(TIME_BRIEF_ROOT_DIR+"/Backup");
        return dir.getAbsolutePath();
    }

    public static String getBackupFilename() {
        return DateFormat.format("yyyy-MM-dd_HH-mm-ss", Calendar.getInstance().getTime()).toString() + Constants.BACKUP_FILE_EXTENSION;
    }

    public static String getExportDirectory() {
        final File dir = Environment.getExternalStoragePublicDirectory(TIME_BRIEF_ROOT_DIR+"/Exported");
        return dir.getAbsolutePath();
    }
}
