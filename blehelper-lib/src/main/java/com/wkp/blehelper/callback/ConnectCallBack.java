package com.wkp.blehelper.callback;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.wkp.blehelper.bean.ConnectResponse;
import com.wkp.blehelper.bean.DoubleData;
import com.wkp.blehelper.bean.ScanResponse;

/**
 * Created by user on 2017/7/19.
 */

public abstract class ConnectCallBack implements ScanAndConnectCallBack {

    @Override
    public boolean onScanBack(ScanResponse scanResponse) {
        return false;
    }

    public abstract void onConnectResult(ConnectResponse connectResponse);

    /**
     * @param connectResponse
     * @return 待发送数据
     */
    @Nullable
    public abstract byte[] onDiscoverServiceResult(ConnectResponse connectResponse);

    /**
     * @param connectResponse
     * @return  是否结束通信
     */
    public abstract boolean onWriteResult(ConnectResponse connectResponse);

    /**
     * Boolean : true 发送   false 不发送   null 结束通信
     * @param connectResponse
     * @return
     */
    @NonNull
    public abstract DoubleData<Boolean, byte[]> onNotifyResult(ConnectResponse connectResponse);

    /**
     * 此次通信任务超时
     * @param connectResponse
     */
    public abstract void onRunnableTimeout(ConnectResponse connectResponse);
}
