package com.royran.timebrief.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.text.util.Linkify;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.royran.timebrief.R;
import com.royran.timebrief.ui.adapter.BackupListAdapter;
import com.royran.timebrief.utils.BackupUtils;

import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.OnClick;

public class AboutActivity extends BaseActivity {
    @BindView(R.id.text_title)
    TextView mTextTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTextTitle.setText(getString(R.string.about));
//        String text = "";
//        mTextContent.setText(text);
//        mTextContent.setClickable(true);
//        Linkify.addLinks(mTextContent, Linkify.EMAIL_ADDRESSES);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_about;
    }

    @OnClick(R.id.image_home)
    protected void onBackClicked() {
        finish();
    }

    public static void openAboutActivity(Context context) {
        Intent intent = new Intent(context, AboutActivity.class);
        context.startActivity(intent);
    }
}
