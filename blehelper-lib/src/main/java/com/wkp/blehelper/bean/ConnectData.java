package com.wkp.blehelper.bean;

import com.wkp.blehelper.callback.ScanAndConnectCallBack;

/**
 * Created by user on 2017/7/20.
 */

public class ConnectData {
    public long connectTimeout;
    public String serviceUUID;
    public String characteristicUUID;
    public String notifyUUID;
    public NotifyLimit notifyLimit;
    public boolean isNotify;
    public ScanAndConnectCallBack callBack;

    public ConnectData(long connectTimeout, String serviceUUID, String characteristicUUID, String notifyUUID, NotifyLimit notifyLimit, boolean isNotify, ScanAndConnectCallBack
            callBack) {
        this.connectTimeout = connectTimeout;
        this.serviceUUID = serviceUUID;
        this.characteristicUUID = characteristicUUID;
        this.notifyUUID = notifyUUID;
        this.notifyLimit = notifyLimit;
        this.isNotify = isNotify;
        this.callBack = callBack;
    }
}
