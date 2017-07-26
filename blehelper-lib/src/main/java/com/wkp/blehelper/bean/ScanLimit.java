package com.wkp.blehelper.bean;

import android.bluetooth.le.ScanResult;

import java.util.Comparator;
import java.util.List;

/**
 * Created by user on 2017/7/19.
 */

public class ScanLimit {
    /**
     * 过滤文本集
     */
    public List<String> filters;
    /**
     * 排序算法
     */
    public Comparator<ScanResult> sort;
    /**
     * 是否扫描到第一个即连接
     */
    public boolean isConnectScanFirst;
    /**
     * 是否扫描完成后连接第一个
     */
    public boolean isConnectResultFirst;

    public ScanLimit(List<String> filters, Comparator<ScanResult> sort, boolean isConnectScanFirst, boolean isConnectResultFirst) {
        this.filters = filters;
        this.sort = sort;
        this.isConnectScanFirst = isConnectScanFirst;
        this.isConnectResultFirst = isConnectResultFirst;
    }

    @Override
    public String toString() {
        return "ScanLimit{" +
                "filters=" + filters +
                ", sort=" + sort +
                ", isConnectScanFirst=" + isConnectScanFirst +
                ", isConnectResultFirst=" + isConnectResultFirst +
                '}';
    }
}
