package com.wkp.blehelper.bean;

import android.bluetooth.le.ScanResult;

import com.wkp.blehelper.ble.BleHelper;

import java.util.List;

/**
 * Created by user on 2017/7/18.
 */

public class ScanResponse {
    /**
     * 扫描状态
     */
    @BleHelper.ScanStatue
    public int status;
    /**
     * 设备信息
     */
    public ScanResult scanResult;
    /**
     * 设备集合
     */
    public List<ScanResult> results;

    public ScanResponse() {
    }

    @Override
    public String toString() {
        return "ScanResponse{" +
                "status=" + status +
                ", scanResult=" + scanResult +
                ", results=" + results +
                '}';
    }
}
