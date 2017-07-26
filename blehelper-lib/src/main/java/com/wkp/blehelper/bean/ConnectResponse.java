package com.wkp.blehelper.bean;

import com.wkp.blehelper.ble.BleHelper;

/**
 * Created by user on 2017/7/19.
 */

public class ConnectResponse {
    /**
     * 连接状态码
     */
    @BleHelper.ConnectStatue
    public int status;
    /**
     * 接收数据
     */
    public byte[] notifyValue;
}
