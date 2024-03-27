package eu.mrogalski.saidit;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

public class SaidItActivity extends Activity {

    private static final int PERMISSION_REQUEST_CODE = 5465;
    private boolean isFragmentSet = false;
    private AlertDialog permissionDeniedDialog;
    private AlertDialog storagePermissionDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background_recorder);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(permissionDeniedDialog != null) {
            permissionDeniedDialog.dismiss();
        }
        if(storagePermissionDialog != null) {
            storagePermissionDialog.dismiss();
        }
        requestPermissions();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(permissionDeniedDialog != null) {
            permissionDeniedDialog.dismiss();
        }
        if(storagePermissionDialog != null) {
            storagePermissionDialog.dismiss();
        }
        requestPermissions();
    }

    private void requestPermissions() {
        // Ask for storage permission

        String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.FOREGROUND_SERVICE};
        if(Build.VERSION.SDK_INT >= 33) {
            permissions = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.FOREGROUND_SERVICE, Manifest.permission.POST_NOTIFICATIONS};
        }
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            permissions = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.FOREGROUND_SERVICE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        }
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Check if all permissions are granted
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                // All permissions are granted
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        // Permission already granted
                        if(storagePermissionDialog != null) {
                            storagePermissionDialog.dismiss();
                        }
                        showFragment();
                    } else {
                        // Request MANAGE_EXTERNAL_STORAGE permission
                        storagePermissionDialog = new AlertDialog.Builder(this)
                                .setTitle(R.string.permission_required)
                                .setMessage(R.string.permission_required_message)
                                .setPositiveButton(R.string.allow, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Open app settings
                                        Intent intent = new Intent();
                                        intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                                        intent.setData(Uri.fromParts("package", getPackageName(), null));
                                        startActivity(intent);
                                    }
                                })
                                .setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                                .setCancelable(false)
                                .show();
                    }
                } else {
                    // For devices below Android 10, request WRITE_EXTERNAL_STORAGE permission
                    // Use the code snippet from the previous response
                }
            } else {
                if(permissionDeniedDialog == null || !permissionDeniedDialog.isShowing()) {
                    showPermissionDeniedDialog();
                }
            }
        }
    }

    private void showFragment() {
        if (!isFragmentSet) {
            isFragmentSet = true;
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, new SaidItFragment(), "main-fragment")
                    .commit();
        }
    }
    private void showPermissionDeniedDialog() {
        permissionDeniedDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.permission_required)
                .setMessage(R.string.permission_required_message)
                .setPositiveButton(R.string.allow, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Open app settings
                        Intent intent = new Intent();
                        intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.fromParts("package", getPackageName(), null));
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }
}