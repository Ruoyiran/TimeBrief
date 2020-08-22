package com.royran.timebrief.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.orhanobut.logger.Logger;

public class AppUtils {

    public static String getVersionName(Context context) {
        String version = "1.0.0";
        if (context == null) {
            return version;
        }
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            version = packInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Logger.e("NameNotFoundException: %s", e);
        }
        return version;
    }

}
