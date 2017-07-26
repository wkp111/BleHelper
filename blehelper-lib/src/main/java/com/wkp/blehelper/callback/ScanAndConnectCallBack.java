package com.wkp.blehelper.callback;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.wkp.blehelper.bean.ConnectResponse;
import com.wkp.blehelper.bean.DoubleData;
import com.wkp.blehelper.bean.ScanResponse;

/**
 * Created by user on 2017/7/20.
 */

public interface ScanAndConnectCallBack {
    /**
     * @param scanResponse 扫描结果（扫描状态、设备信息、设备集合）
     * @return 当扫描到第一个满足条件设备就连接功能开启时，返回true则连接该设备
     */
    boolean onScanBack(ScanResponse scanResponse);

    void onConnectResult(ConnectResponse connectResponse);

    /**
     * @param connectResponse
     * @return 待发送数据
     */
    @Nullable
    byte[] onDiscoverServiceResult(ConnectResponse connectResponse);

    /**
     * @param connectResponse
     * @return 是否结束通信
     */
    boolean onWriteResult(ConnectResponse connectResponse);

    /**
     * Boolean : true 发送   false 不发送   null 结束通信
     *
     * @param connectResponse
     * @return
     */
    @NonNull
    DoubleData<Boolean, byte[]> onNotifyResult(ConnectResponse connectResponse);

    /**
     * 此次通信任务超时
     *
     * @param connectResponse
     */
    void onRunnableTimeout(ConnectResponse connectResponse);
}
