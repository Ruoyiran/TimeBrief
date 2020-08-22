package com.royran.timebrief.ui.views;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.fragment.app.FragmentActivity;

import com.royran.timebrief.R;
import com.royran.timebrief.utils.UIUtils;

import java.util.Objects;

public class EditTextBottomDialog extends BottomDialogBase {

    private EditText mEditText;

    private ImageView mImageView;

    private OnOkListener mOnOkListener;

    public interface OnOkListener {
        void onOk(String text);
    }

    @Override
    public int getLayoutRes() {
        return R.layout.bottom_edit_text;
    }

    @Override
    public void bindView(View v) {
        setCancelable(false);
        mEditText = v.findViewById(R.id.edit_text);
        mImageView = v.findViewById(R.id.image_add);
        mEditText.setFocusable(true);
        mEditText.setFocusableInTouchMode(true);
        mEditText.requestFocus();
        UIUtils.showKeyboard(getContext());

        mImageView.setOnClickListener(v1 -> {
            if (mOnOkListener != null) {
                UIUtils.hideKeyboard(getActivity(), mEditText);
                mOnOkListener.onOk(mEditText.getText().toString());
                this.dismiss();
            }
        });
    }

    @Override
    public float getDimAmount() {
        return 0.9f;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public static void show(FragmentActivity activity, OnOkListener onOkListener) {
        EditTextBottomDialog dialog = new EditTextBottomDialog();
        dialog.mOnOkListener = onOkListener;
        dialog.show(activity.getSupportFragmentManager());
    }
}
