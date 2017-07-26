package com.wkp.blehelper.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.wkp.blehelper.ble.BleHelper;
import com.wkp.blehelper.callback.PermissionCallBack;

import static com.wkp.blehelper.ble.BleHelper.BLE_PERMISSION_RESULT_CODE;

public class InvisibleActivity extends AppCompatActivity implements IInvisibleView {

    private PermissionCallBack mCallBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new BleHelper.InvisiblePresenterImpl(this);
    }

    @Override
    public void getPermission(PermissionCallBack callBack) {
        mCallBack = callBack;
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},BLE_PERMISSION_RESULT_CODE);
    }

    /**
     * 申请动态权限结果
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == BLE_PERMISSION_RESULT_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (Manifest.permission.ACCESS_FINE_LOCATION.equals(permissions[i])) {
                    if (mCallBack != null) {
                        mCallBack.onPermissionResult(permissions[i],grantResults[i] == PackageManager.PERMISSION_GRANTED);
                        mCallBack = null;
                    }
                    finish();
                    return;
                }
            }
            if (mCallBack != null) {
                mCallBack.onPermissionResult(Manifest.permission.ACCESS_FINE_LOCATION,false);
                mCallBack = null;
            }
        }
        finish();
    }
}
