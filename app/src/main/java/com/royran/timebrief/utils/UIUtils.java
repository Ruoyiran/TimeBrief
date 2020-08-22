package com.royran.timebrief.utils;

import android.app.Activity;
import android.content.Context;
import android.os.IBinder;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;


public class UIUtils {
    private final static String TAG = "UIUtils";

    public static void showKeyboard(@NotNull Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    public static void hideKeyboard(@NonNull final Activity activity, final View view) {
        if (checkActivityDestroy(activity)) {
            Log.e(TAG, "activity is destroyed");
            return;
        }
        InputMethodManager imm =
                (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            final View currentFocus = activity.getCurrentFocus();
            IBinder windowToken = null;
            if (currentFocus != null) {
                windowToken = currentFocus.getWindowToken();
            } else {
                if (view != null) {
                    windowToken = view.getWindowToken();
                }
            }
            if (windowToken != null) {
                imm.hideSoftInputFromWindow(windowToken, 0);
            }
        }
    }

    //判断Activity是否Destroy
    public static boolean checkActivityDestroy(Activity activity) {
        return activity == null || activity.isFinishing() || activity.isDestroyed();
    }

    private static int searchText(String content, String search, boolean ignoreCase) {
        int index = -1;
        if (ignoreCase) {
            index = content.toLowerCase().indexOf(search.toLowerCase());
        } else {
            index = content.indexOf(search);
        }
        return index;
    }

    public static void setTextWithHighlight(TextView textView, String content, String highlightText, int color, boolean ignoreCase) {
        if (textView == null) {
            return;
        }
        if (TextUtils.isEmpty(content) || TextUtils.isEmpty(highlightText)) {
            textView.setText(content);
            return;
        }
        int index = searchText(content, highlightText, ignoreCase);
        if (index < 0) {
            textView.setText(content);
        } else {
            SpannableString msp = new SpannableString(content);
            int start = index;
            int end = start + highlightText.length();
            int offset = 0;
            while (index >= 0) {
                msp.setSpan(new BackgroundColorSpan(color),
                        offset + start, offset + end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                content = content.substring(end);
                offset += end;
                index = searchText(content, highlightText, ignoreCase);
                if (index >= 0) {
                    start = index;
                    end = start + highlightText.length();
                }
            }
            textView.setText(msp);
        }
    }
}
