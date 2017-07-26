package com.wkp.blehelper.bean;

/**
 * Created by user on 2017/7/19.
 */

public class NotifyLimit {
    public byte start;      //接收数据开头字节
    public byte end;        //接收数据结尾字节
    public int checkCount;  //接收数据结尾后的校验字节数，>= 0, <=2

    public NotifyLimit(byte start, byte end, int checkCount) {
        this.start = start;
        this.end = end;
        this.checkCount = checkCount;
    }

    @Override
    public String toString() {
        return "NotifyLimit{" +
                "start=" + start +
                ", end=" + end +
                ", checkCount=" + checkCount +
                '}';
    }
}
