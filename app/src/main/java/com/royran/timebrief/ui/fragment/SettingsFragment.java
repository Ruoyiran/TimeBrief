package com.royran.timebrief.ui.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.MaterialDialog;
import com.orhanobut.logger.Logger;
import com.royran.timebrief.R;
import com.royran.timebrief.ui.activity.AboutActivity;
import com.royran.timebrief.ui.activity.BackupManagementActivity;
import com.royran.timebrief.utils.AppUtils;
import com.royran.timebrief.utils.BackupUtils;
import com.royran.timebrief.utils.RealmHelper;
import com.royran.timebrief.utils.validator.UrlValidator;

import butterknife.BindView;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class SettingsFragment extends BaseFragment {
    private static final UrlValidator sUrlValidator = new UrlValidator(UrlValidator.ALLOW_2_SLASHES + UrlValidator.ALLOW_ALL_SCHEMES);

    @BindView(R.id.text_current_version)
    TextView mCurrentVersion;

    @BindView(R.id.text_server_addr)
    TextView mTextServerAddr;

    public static BaseFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.fragment_settings;
    }

    @Override
    protected void onCreateView() {
        Logger.d("SettingsFragment::onCreateView");
        mCurrentVersion.setText(AppUtils.getVersionName(getContext()));
        mTextServerAddr.setText(BackupUtils.getBackupServerAddr());
    }

    @Override
    public void onEnable() {
        Logger.d("SettingsFragment::onEnable");
    }

    @OnClick(R.id.layout_server_addr)
    protected void onServerAddrClicked() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_sigle_edittext, null);
        final EditText editText = view.findViewById(R.id.edit);
        editText.setText(mTextServerAddr.getText());
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.change_host)
                .setView(view)
                .setCancelable(true)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    dialog.dismiss();
                    String address = editText.getText().toString();
                    address = address.trim();
                    if (sUrlValidator.isValid(address)) {
                        mTextServerAddr.setText(address);
                        BackupUtils.setBackupServerAddr(address);
                    } else {
                        String errorMessage = getString(R.string.invalid_link, address);
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    @OnClick(R.id.layout_backup_data)
    protected void onBackupClicked() {
        SettingsFragmentPermissionsDispatcher.backupDataWithPermissionCheck(this);
    }

    @OnClick(R.id.layout_backup_management)
    protected void onBackupManagementClicked() {
        SettingsFragmentPermissionsDispatcher.openBackupManagementWithPermissionCheck(this);
    }

    @OnClick(R.id.layout_about)
    protected void onAboutClicked() {
        AboutActivity.openAboutActivity(getContext());
    }

    @OnClick(R.id.layout_upload_data)
    protected void onUploadDataClicked() {
        BackupUtils.uploadData(getActivity());
    }

    @OnClick(R.id.layout_download_data)
    protected void onDownloadDataClicked() {
        BackupUtils.downloadData(getActivity());
    }

    @OnClick(R.id.layout_clear_all)
    protected void onClearAllClicked() {
        new MaterialDialog.Builder(getContext()).
                content(getString(R.string.clear_data_hint)).
                positiveText(getString(R.string.ok)).
                negativeText(getString(R.string.cancel)).onPositive(
                (dialog, which) -> {
                    if (RealmHelper.removeAllRecords()) {
                        Toast.makeText(getContext(), getString(R.string.clear_success), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), getString(R.string.clear_failed), Toast.LENGTH_SHORT).show();
                    }
                }
        ).show();
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    protected void backupData() {
        BackupUtils.backupData(getContext());
    }

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
    protected void openBackupManagement() {
        BackupManagementActivity.openBackupManagementActivity(getContext());
    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        SettingsFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

}
