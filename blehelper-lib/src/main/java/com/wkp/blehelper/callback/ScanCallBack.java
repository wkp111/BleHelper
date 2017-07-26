package com.wkp.blehelper.callback;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.wkp.blehelper.bean.ConnectResponse;
import com.wkp.blehelper.bean.DoubleData;
import com.wkp.blehelper.bean.ScanResponse;

/**
 * Created by user on 2017/7/18.
 */

public abstract class ScanCallBack implements ScanAndConnectCallBack {
    public abstract boolean onScanBack(ScanResponse scanResponse);

    @Override
    public void onConnectResult(ConnectResponse connectResponse) {

    }

    @Nullable
    @Override
    public byte[] onDiscoverServiceResult(ConnectResponse connectResponse) {
        return new byte[0];
    }

    @Override
    public boolean onWriteResult(ConnectResponse connectResponse) {
        return false;
    }

    @NonNull
    @Override
    public DoubleData<Boolean, byte[]> onNotifyResult(ConnectResponse connectResponse) {
        return null;
    }

    @Override
    public void onRunnableTimeout(ConnectResponse connectResponse) {

    }
}
