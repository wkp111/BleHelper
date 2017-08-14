package com.wkp.blehelper.ble;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;

import com.wkp.blehelper.activity.IInvisibleView;
import com.wkp.blehelper.activity.InvisibleActivity;
import com.wkp.blehelper.bean.ConnectData;
import com.wkp.blehelper.bean.ConnectResponse;
import com.wkp.blehelper.bean.DoubleData;
import com.wkp.blehelper.bean.NotifyLimit;
import com.wkp.blehelper.bean.ScanLimit;
import com.wkp.blehelper.bean.ScanResponse;
import com.wkp.blehelper.callback.ConnectCallBack;
import com.wkp.blehelper.callback.EnableBleCallBack;
import com.wkp.blehelper.callback.PermissionCallBack;
import com.wkp.blehelper.callback.ScanAndConnectCallBack;
import com.wkp.blehelper.callback.ScanCallBack;
import com.wkp.blehelper.util.CommonUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Created by wkp on 2017/7/18.
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public final class BleHelper {
    public static final int BLE_PERMISSION_RESULT_CODE = 520;
    private static final long SCAN_TIME_DEFAULT = 10000;
    private static final long SCAN_TIME_MIN = 500;
    private static final long CONNECT_TIME_OUT_DEFAULT = 10000;
    private static final long CONNECT_TIME_MIN = 3000;
    private static final int CONNECT_COUNT_DEFAULT = 3;

    public static final int SCAN_STATE_ERROR_NO_SUPPORT = 0x01;     //设备不支持Ble
    public static final int SCAN_STATE_ERROR_NO_PERMISSION = 0x02;  //动态权限没给予
    private static final int SCAN_STATE_ERROR_START_FAILED = 0x03;  //开启扫描失败
    private static final int SCAN_STATE_ERROR_NO_BLE = 0x04;        //蓝牙未开启
    private static final int SCAN_STATE_ERROR_END_FAILED = 0x05;    //结束扫描失败
    public static final int SCAN_STATE_START = 0x06;                //开始扫描
    public static final int SCAN_STATE_FOUND = 0x07;                //发现设备
    public static final int SCAN_STATE_END = 0x08;                  //扫描结束

    private static final int CONNECT_STATE_TAG = 100;               //连接状态线程通信标记
    public static final int CONNECT_STATE_DISCONNECTED = 0x09;      //断开连接状态
    public static final int CONNECT_STATE_CONNECTING = 0x0A;        //正在连接状态
    public static final int CONNECT_STATE_CONNECTED = 0x0B;         //连接成功状态

    private static final int SERVICE_STATE_TAG = 101;               //服务状态线程通信标记
    public static final int SERVICE_STATE_DISCONNECTED = 0x0C;      //发现服务失败
    public static final int SERVICE_STATE_CONNECTED = 0x0D;         //发现服务成功

    private static final int WRITE_STATE_TAG = 102;                 //发送数据状态线程通信标记
    public static final int WRITE_STATE_FAILED = 0x0E;              //发送数据失败
    public static final int WRITE_STATE_SUCCESS = 0x0F;             //发送数据成功

    private static final int NOTIFY_STATE_TAG = 103;                 //接收数据状态线程通信标记
    public static final int NOTIFY_STATE_FAILED = 0x10;              //接收数据失败
    public static final int NOTIFY_STATE_SUCCESS = 0x11;             //接收数据成功

    public static final int CONNECT_OR_WRITE_TIME_OUT = 0x12;        //连接通信超时

    private static String TAG = "BleHelper";
    private static BleHelper sBleHelper;
    private static Context sContext;
    private static BluetoothManager sBluetoothManager;
    private static BluetoothAdapter sBluetoothAdapter;
    private static PermissionCallBack mPermissionCallBack;
    private EnableBleCallBack mEnableBleCallBack;
    private ScanResponse mScanResponse;
    private ScanAndConnectCallBack mScanCallBack;
    private ScanLimit mScanLimit;
    private static int maxConnectCount = CONNECT_COUNT_DEFAULT;
    private static int connectCount = 0;
    private static ConnectResponse mConnectResponse;
    private static ScanAndConnectCallBack mConnectCallBack;
    private static String mServiceUUID;
    private static String mCharacteristicUUID;
    private static byte[][] sDetachMsg;
    private static int writeCount = 0;
    private static NotifyLimit mNotifyLimit;
    private static boolean mIsNotify;
    private static BluetoothGatt mBluetoothGatt;
    private static boolean isTimeout;
    private ConnectData mConnectData;
    private long mScanTime;

    @IntDef({SCAN_STATE_ERROR_NO_SUPPORT, SCAN_STATE_ERROR_NO_PERMISSION, SCAN_STATE_ERROR_START_FAILED, SCAN_STATE_ERROR_NO_BLE,
            SCAN_STATE_ERROR_END_FAILED, SCAN_STATE_START, SCAN_STATE_FOUND, SCAN_STATE_END})
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
    public @interface ScanStatue {
    }

    @IntDef({CONNECT_STATE_DISCONNECTED, CONNECT_STATE_CONNECTING, CONNECT_STATE_CONNECTED, SERVICE_STATE_DISCONNECTED,
            SERVICE_STATE_CONNECTED, WRITE_STATE_FAILED, WRITE_STATE_SUCCESS, NOTIFY_STATE_FAILED, NOTIFY_STATE_SUCCESS, CONNECT_OR_WRITE_TIME_OUT})
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
    public @interface ConnectStatue {
    }


    /* ---------------------------------------------------------以下为通信处理部分------------------------------------------------------ */


    /**
     * 初始化连接变量
     */
    private static void initConnect() {
        sHandler.removeCallbacks(connectTimeoutRunnable);
        connectCount = 0;
        maxConnectCount = CONNECT_COUNT_DEFAULT;
        mConnectResponse = null;
        mConnectCallBack = null;
        mServiceUUID = null;
        mCharacteristicUUID = null;
        sDetachMsg = null;
        writeCount = 0;
        mNotifyLimit = null;
        mIsNotify = false;
        mBluetoothGatt = null;
        results = null;
    }

    /**
     * 通讯处理
     */
    private static Handler sHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CONNECT_STATE_TAG:
                    processOnConnectionStateChange(msg.arg1, msg.arg2, (BluetoothGatt) msg.obj);
                    break;
                case SERVICE_STATE_TAG:
                    processOnServicesDiscovered(msg.arg1, (BluetoothGatt) msg.obj);
                    break;
            }
        }
    };

    /**
     * 处理连接状态改变回调
     *
     * @param status
     * @param newState
     * @param gatt
     */

    private static void processOnConnectionStateChange(int status, int newState, BluetoothGatt gatt) {
        if (mConnectResponse == null) {
            return;
        }

        if (status != BluetoothGatt.GATT_SUCCESS) {
            gatt.disconnect();
            return;
        }

        switch (newState) {
            case BluetoothAdapter.STATE_DISCONNECTED:
                if (mConnectResponse.status < CONNECT_STATE_CONNECTED) {
                    if (connectCount < maxConnectCount) {
                        gatt.connect();
                        connectCount++;
                    } else {
                        isTimeout = false;
                        initConnect();
                        gatt.close();
                        mConnectResponse.status = CONNECT_STATE_DISCONNECTED;
                        mConnectCallBack.onConnectResult(mConnectResponse);
                    }
                } else {
                    isTimeout = false;
                    initConnect();
                    gatt.close();
                }
                break;
            case BluetoothAdapter.STATE_CONNECTING:
                mConnectResponse.status = CONNECT_STATE_CONNECTING;
                mConnectCallBack.onConnectResult(mConnectResponse);
                break;
            case BluetoothAdapter.STATE_CONNECTED:
                mConnectResponse.status = CONNECT_STATE_CONNECTED;
                mConnectCallBack.onConnectResult(mConnectResponse);
                boolean services = gatt.discoverServices();
                if (!services) {
                    mConnectResponse.status = SERVICE_STATE_DISCONNECTED;
                    mConnectCallBack.onDiscoverServiceResult(mConnectResponse);
                    gatt.disconnect();
                }
                break;
        }
    }

    /**
     * 处理发现服务成功回调
     *
     * @param status
     * @param gatt
     */
    private static void processOnServicesDiscovered(int status, final BluetoothGatt gatt) {
        if (mConnectResponse == null) {
            return;
        }

        if (status != BluetoothGatt.GATT_SUCCESS) {
            mConnectResponse.status = SERVICE_STATE_DISCONNECTED;
            mConnectCallBack.onDiscoverServiceResult(mConnectResponse);
            gatt.discoverServices();
            return;
        }
        BluetoothGattService service = gatt.getService(UUID.fromString(mServiceUUID));
        if (service == null) {
            mConnectResponse.status = SERVICE_STATE_DISCONNECTED;
            mConnectCallBack.onDiscoverServiceResult(mConnectResponse);
            gatt.disconnect();
            return;
        }
        final BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(mCharacteristicUUID));
        if (characteristic == null) {
            mConnectResponse.status = SERVICE_STATE_DISCONNECTED;
            mConnectCallBack.onDiscoverServiceResult(mConnectResponse);
            gatt.disconnect();
            return;
        }

        if (mIsNotify) {
            boolean notification = gatt.setCharacteristicNotification(characteristic, true);
            if (!notification) {
                mConnectResponse.status = SERVICE_STATE_DISCONNECTED;
                mConnectCallBack.onDiscoverServiceResult(mConnectResponse);
                gatt.disconnect();
                return;
            }
        }

        mConnectResponse.status = SERVICE_STATE_CONNECTED;
        final byte[] sendData = mConnectCallBack.onDiscoverServiceResult(mConnectResponse);
        sendData(gatt, characteristic, sendData);
    }

    /**
     * 发送第一包数据
     *
     * @param gatt
     * @param characteristic
     * @param sendData
     */
    private static void sendData(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final byte[] sendData) {
        if (mConnectResponse == null) {
            return;
        }

        if (sendData == null || sendData.length == 0) {
            mConnectResponse.status = WRITE_STATE_FAILED;
            mConnectCallBack.onWriteResult(mConnectResponse);
            gatt.disconnect();
            return;
        }
        CommonUtils.runInSubThread(new Runnable() {
            @Override
            public void run() {
                sDetachMsg = detachMsg(sendData);
                writeCount = 0;
                characteristic.setValue(sDetachMsg[writeCount]);
                boolean writeCharacteristic = gatt.writeCharacteristic(characteristic);
                if (!writeCharacteristic) {
                    mConnectResponse.status = WRITE_STATE_FAILED;
                    CommonUtils.runInMainThread(new Runnable() {
                        @Override
                        public void run() {
                            mConnectCallBack.onWriteResult(mConnectResponse);
                            gatt.disconnect();
                        }
                    });
                }
            }
        });
    }

    /**
     * 处理发送数据成功回调
     *
     * @param gatt
     * @param characteristic
     * @param status
     */
    private void processOnCharacteristicWrite(final BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (mConnectResponse == null) {
            return;
        }

        if (status != BluetoothGatt.GATT_SUCCESS) {
            CommonUtils.runInMainThread(new Runnable() {
                @Override
                public void run() {
                    mConnectResponse.status = WRITE_STATE_FAILED;
                    mConnectCallBack.onWriteResult(mConnectResponse);
                    gatt.disconnect();
                }
            });
            return;
        }
        writeCount++;
        if (writeCount < sDetachMsg.length) {
            characteristic.setValue(sDetachMsg[writeCount]);
            boolean writeCharacteristic = gatt.writeCharacteristic(characteristic);
            if (!writeCharacteristic) {
                mConnectResponse.status = WRITE_STATE_FAILED;
                CommonUtils.runInMainThread(new Runnable() {
                    @Override
                    public void run() {
                        mConnectCallBack.onWriteResult(mConnectResponse);
                        gatt.disconnect();
                    }
                });
            }
        } else {
            sDetachMsg = null;
            mConnectResponse.status = WRITE_STATE_SUCCESS;
            CommonUtils.runInMainThread(new Runnable() {
                @Override
                public void run() {
                    boolean writeResult = mConnectCallBack.onWriteResult(mConnectResponse);
                    if (!mIsNotify) {
                        gatt.disconnect();
                    } else {
                        initNotify();
                    }
                    if (writeResult) {
                        gatt.disconnect();
                    }
                }
            });
        }
    }

    /**
     * 处理接收通知回调
     *
     * @param gatt
     * @param characteristic
     */
    private void processOnCharacteristicChanged(final BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (mConnectResponse == null) {
            return;
        }

        mConnectResponse.status = NOTIFY_STATE_SUCCESS;
        byte[] value = characteristic.getValue();
        if (value == null || value.length == 0) {
            CommonUtils.runInMainThread(new Runnable() {
                @Override
                public void run() {
                    mConnectResponse.status = NOTIFY_STATE_FAILED;
                    mConnectCallBack.onNotifyResult(mConnectResponse);
                    gatt.disconnect();
                }
            });
            return;
        }
        if (mNotifyLimit == null) {
            notifySuccess(gatt, characteristic, value);
            return;
        }
        processNotifyValue(gatt, characteristic, value);
    }

    /**
     * 通知接收成功
     *
     * @param gatt
     * @param characteristic
     * @param value
     */
    private void notifySuccess(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final byte[] value) {
        if (mConnectResponse == null) {
            return;
        }

        CommonUtils.runInMainThread(new Runnable() {
            @Override
            public void run() {
                mConnectResponse.notifyValue = value;
                DoubleData<Boolean, byte[]> doubleData = mConnectCallBack.onNotifyResult(mConnectResponse);
                if (doubleData.first == null) {
                    gatt.disconnect();
                } else {
                    if (doubleData.first) {
                        sendData(gatt, characteristic, doubleData.second);
                    }
                }
            }
        });
    }

    private boolean isStart;
    private boolean isEnd;
    private boolean isCheck;
    private int checkCount;
    private static byte[] results;

    /**
     * 初始化变量
     */
    private void initNotify() {
        isStart = false;
        isEnd = false;
        isCheck = false;
        checkCount = 0;
        results = new byte[0];
    }

    /**
     * 处理限制数据
     *
     * @param gatt
     * @param characteristic
     * @param value
     */
    private void processNotifyValue(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, byte[] value) {
        if (isCheck) {
            if (value.length >= checkCount) {
                byte[] copyValue = Arrays.copyOfRange(value, 0, checkCount);
                results = mergeMsg(results, copyValue);
                notifySuccess(gatt, characteristic, results);
                initNotify();
            } else {
                byte[] copyValue = Arrays.copyOfRange(value, 0, value.length);
                results = mergeMsg(results, copyValue);
                checkCount = checkCount - value.length;
            }

            if (isCheck) {
                return;
            }
        }
        int startIndex = indexOfValue(value, mNotifyLimit.start);
        int endIndex = indexOfValue(value, mNotifyLimit.end);
        if (startIndex > -1) {
            isStart = true;
        } else {
            startIndex = 0;
        }

        if (isStart) {
            if (endIndex > -1) {
                isEnd = true;
            } else {
                endIndex = value.length - 1;
            }

            if (isEnd) {
                int remainCount = value.length - endIndex - 1;
                if (remainCount >= mNotifyLimit.checkCount) {
                    byte[] copyValue = Arrays.copyOfRange(value, startIndex, endIndex + mNotifyLimit.checkCount + 1);
                    results = mergeMsg(results, copyValue);
                    notifySuccess(gatt, characteristic, results);
                    initNotify();
                } else {
                    byte[] copyValue = Arrays.copyOfRange(value, startIndex, endIndex + remainCount + 1);
                    results = mergeMsg(results, copyValue);
                    checkCount = mNotifyLimit.checkCount - remainCount;
                    isCheck = true;
                    isStart = false;
                    isEnd = false;
                }
            } else {
                byte[] copyValue = Arrays.copyOfRange(value, startIndex, endIndex + 1);
                results = mergeMsg(results, copyValue);
            }
        }
    }


    /* ---------------------------------------------------------以下为蓝牙开关部分------------------------------------------------------ */


    /**
     * 权限申请帮助类
     */
    public static class InvisiblePresenterImpl {
        public InvisiblePresenterImpl(IInvisibleView invisibleView) {
            invisibleView.getPermission(mPermissionCallBack);
            mPermissionCallBack = null;
        }
    }

    /**
     * 开启蓝牙的广播接收者
     */
    private BroadcastReceiver mEnableBleReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        if (mEnableBleCallBack != null) {
                            mEnableBleCallBack.onBleClose();
                        }
                        mEnableBleCallBack = null;
                        context.unregisterReceiver(this);
                        break;
                    case BluetoothAdapter.STATE_ON:
                        if (mEnableBleCallBack != null) {
                            mEnableBleCallBack.onBleOpen();
                        }
                        mEnableBleCallBack = null;
                        context.unregisterReceiver(this);
                        break;
                }
            }
        }
    };

    /**
     * 过滤蓝牙状态广播
     */
    private IntentFilter mEnableBleFilter = new IntentFilter();

    {
        mEnableBleFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
    }


    /* ---------------------------------------------------------以下为基础部分------------------------------------------------------ */


    /**
     * 私有构造
     */
    private BleHelper() {
    }

    /**
     * 获取Ble帮助实例
     *
     * @param context
     * @return
     */
    public static BleHelper getInstance(@NonNull Context context) {
        synchronized (TAG) {
            sContext = context;
            if (sBleHelper == null) {
                sBleHelper = new BleHelper();
            }
            initBle();
            return sBleHelper;
        }
    }

    /**
     * 初始化Ble
     */
    private static void initBle() {
        sBluetoothManager = (BluetoothManager) sContext.getSystemService(Context.BLUETOOTH_SERVICE);
        sBluetoothAdapter = sBluetoothManager.getAdapter();
    }

    /**
     * 判断是否具备动态权限
     */
    public boolean hasPermission() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || ContextCompat.checkSelfPermission(sContext, Manifest.permission
                .ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 申请动态权限
     *
     * @param callBack
     */
    public void getPermission(PermissionCallBack callBack) {
        if (hasPermission()) {
            callBack.onPermissionResult(Manifest.permission.ACCESS_FINE_LOCATION, true);
            return;
        }
        mPermissionCallBack = callBack;
        sContext.startActivity(new Intent(sContext, InvisibleActivity.class));
    }

    /**
     * 设备是否支持Ble
     *
     * @return
     */
    public boolean isSupport() {
        return sContext.getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * 是否开启蓝牙
     *
     * @return
     */
    public boolean isEnable() {
        return sBluetoothAdapter != null && sBluetoothAdapter.isEnabled();
    }

    /**
     * 是否正在扫描
     *
     * @return
     */
    public boolean isScanning() {
        return sBluetoothAdapter != null && sBluetoothAdapter.isDiscovering();
    }

    /**
     * 关闭蓝牙
     *
     * @return
     */
    public boolean disableBle() {
        return sBluetoothAdapter != null && sBluetoothAdapter.disable();
    }

    /**
     * 开启蓝牙
     */
    public void enableBle(@NonNull EnableBleCallBack callBack) {
        if (isEnable()) {
            callBack.onBleOpen();
            return;
        }
        mEnableBleCallBack = callBack;
        sContext.registerReceiver(mEnableBleReceiver, mEnableBleFilter);
        if (sBluetoothAdapter == null) {
            throw new NullPointerException("BluetoothAdapter == null 该设备不支持Ble");
        }
        sBluetoothAdapter.enable();
    }


    /* ---------------------------------------------------------以下为扫描部分------------------------------------------------------ */


    /**
     * 扫描蓝牙设备
     *
     * @param callBack
     */
    public void scanDevice(@NonNull ScanCallBack callBack) {
        scanDevice(true, callBack);
    }

    /**
     * 扫描蓝牙设备
     *
     * @param isAutoGetPermission
     * @param callBack
     */
    public void scanDevice(boolean isAutoGetPermission, @NonNull ScanCallBack callBack) {
        scanDevice(null, false, false, SCAN_TIME_DEFAULT, isAutoGetPermission, null, callBack);
    }

    /**
     * 扫描蓝牙设备
     *
     * @param scanTime
     * @param isAutoGetPermission
     * @param callBack
     */
    public void scanDevice(long scanTime, boolean isAutoGetPermission, @NonNull ScanCallBack callBack) {
        scanDevice(null, false, false, scanTime, isAutoGetPermission, null, callBack);
    }

    /**
     * 扫描蓝牙设备
     *
     * @param filters
     * @param isAutoGetPermission
     * @param callBack
     */
    public void scanDevice(@NonNull List<String> filters, boolean isAutoGetPermission, @NonNull ScanCallBack callBack) {
        scanDevice(filters, false, false, SCAN_TIME_DEFAULT, isAutoGetPermission, null, callBack);
    }

    /**
     * 扫描蓝牙设备
     *
     * @param filters
     * @param scanTime
     * @param isAutoGetPermission
     * @param callBack
     */
    public void scanDevice(@NonNull List<String> filters, long scanTime, boolean isAutoGetPermission, @NonNull ScanCallBack callBack) {
        scanDevice(filters, false, false, scanTime, isAutoGetPermission, null, callBack);
    }

    /**
     * 扫描蓝牙设备
     *
     * @param sort
     * @param isAutoGetPermission
     * @param callBack
     */
    public void scanDevice(boolean isAutoGetPermission, @NonNull Comparator<ScanResult> sort, @NonNull ScanCallBack callBack) {
        scanDevice(null, false, false, SCAN_TIME_DEFAULT, isAutoGetPermission, sort, callBack);
    }

    /**
     * 扫描蓝牙设备
     *
     * @param sort
     * @param scanTime
     * @param isAutoGetPermission
     * @param callBack
     */
    public void scanDevice(long scanTime, boolean isAutoGetPermission, @NonNull Comparator<ScanResult> sort, @NonNull ScanCallBack callBack) {
        scanDevice(null, false, false, scanTime, isAutoGetPermission, sort, callBack);
    }

    /**
     * 扫描蓝牙设备
     *
     * @param filters
     * @param sort
     * @param isAutoGetPermission
     * @param callBack
     */
    public void scanDevice(@NonNull List<String> filters, boolean isAutoGetPermission, @NonNull Comparator<ScanResult> sort,
                           @NonNull ScanCallBack callBack) {
        scanDevice(filters, false, false, SCAN_TIME_DEFAULT, isAutoGetPermission, sort, callBack);
    }

    /**
     * 扫描蓝牙设备
     *
     * @param filters
     * @param sort
     * @param isAutoGetPermission
     * @param callBack
     */
    public void scanDevice(@NonNull List<String> filters, long scanTime, boolean isAutoGetPermission,
                           @NonNull Comparator<ScanResult> sort, @NonNull ScanCallBack callBack) {
        scanDevice(filters, false, false, scanTime, isAutoGetPermission, sort, callBack);
    }


    /**
     * 扫描蓝牙设备
     *
     * @param nameFilters          扫描时要过滤的设备名（部分）
     * @param sort                 扫描设备集合排序
     * @param isConnectScanFirst   是否扫描到第一个满足条件设备就连接
     * @param isConnectResultFirst 是否扫描完成后连接排序在第一个的设备
     * @param scanTime             扫描时长
     * @param isAutoGetPermission  是否自动获取蓝牙各权限（>= 6.0 设备）
     * @param callBack             扫描回调
     */
    private void scanDevice(final List<String> nameFilters, final boolean isConnectScanFirst, final boolean isConnectResultFirst,
                            final long scanTime, boolean isAutoGetPermission, final Comparator<ScanResult> sort, @NonNull final ScanAndConnectCallBack callBack) {
        final long scanTimeout = scanTime <= SCAN_TIME_MIN ? SCAN_TIME_MIN : scanTime;
        mScanResponse = new ScanResponse();
        if (!isSupport()) {
            mScanResponse.status = SCAN_STATE_ERROR_NO_SUPPORT;
            callBack.onScanBack(mScanResponse);
            mScanResponse = null;
            return;
        }
        if (!hasPermission()) {
            mScanResponse.status = SCAN_STATE_ERROR_NO_PERMISSION;
            callBack.onScanBack(mScanResponse);
            if (!isAutoGetPermission) {
                mScanResponse = null;
                return;
            } else {
                getPermission(new PermissionCallBack() {
                    @Override
                    public void onPermissionResult(String permission, boolean isSuccess) {
                        if (!isSuccess) {
                            mScanResponse.status = SCAN_STATE_ERROR_NO_PERMISSION;
                            callBack.onScanBack(mScanResponse);
                            mScanResponse = null;
                        } else {
                            mScanLimit = new ScanLimit(nameFilters, sort, isConnectScanFirst, isConnectResultFirst);
                            preScan(scanTimeout, callBack);
                        }
                    }
                });
            }
        } else {
            mScanLimit = new ScanLimit(nameFilters, sort, isConnectScanFirst, isConnectResultFirst);
            preScan(scanTimeout, callBack);
        }
    }

    /**
     * 准备开始扫描
     *
     * @param scanTime
     * @param callBack
     */
    private void preScan(final long scanTime, final ScanAndConnectCallBack callBack) {
        enableBle(new EnableBleCallBack() {
            @Override
            public void onBleOpen() {
                mScanResponse.status = SCAN_STATE_START;
                mScanResponse.results = new ArrayList<>();
                callBack.onScanBack(mScanResponse);
                sHandler.postDelayed(mScanTimeoutRunnable, scanTime);
                mScanCallBack = callBack;
                sBluetoothAdapter.getBluetoothLeScanner().startScan(scanCallback);
            }

            @Override
            public void onBleClose() {
                mScanResponse.status = SCAN_STATE_ERROR_NO_BLE;
                callBack.onScanBack(mScanResponse);
                mScanResponse = null;
                mScanLimit = null;
            }
        });
    }

    /**
     * 扫描回调
     */
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            boolean isContains = false;
            if (mScanLimit == null) {
                return;
            }
            if (mScanLimit.filters != null && mScanLimit.filters.size() > 0) {
                for (String filter : mScanLimit.filters) {
                    isContains = result.getDevice().getName() != null && filter != null && result.getDevice().getName().toLowerCase().contains(filter.toLowerCase());
                    if (isContains) {
                        break;
                    }
                }
            } else {
                isContains = true;
            }
            if (isContains) {
                mScanResponse.status = SCAN_STATE_FOUND;
                mScanResponse.scanResult = result;
                boolean isAdd = true;
                for (ScanResult scanResult : mScanResponse.results) {
                    if (result.getDevice().getName() == null || scanResult.getDevice().getName().equals(result.getDevice().getName())) {
                        isAdd = false;
                        break;
                    }
                }
                if (isAdd) {
                    mScanResponse.results.add(mScanResponse.scanResult);
                }
                if (mScanLimit.isConnectScanFirst) {
                    boolean scanBack = mScanCallBack.onScanBack(mScanResponse);
                    if (scanBack) {
                        connectAndWrite(mScanResponse.scanResult, mConnectData.connectTimeout, mConnectData.serviceUUID, mConnectData.characteristicUUID,
                                mConnectData.notifyLimit, mConnectData.isNotify, false, mConnectData.callBack);
                        sBluetoothAdapter.getBluetoothLeScanner().stopScan(this);
                        return;
                    }
                }
                if (mScanLimit.sort != null) {
                    Collections.sort(mScanResponse.results, mScanLimit.sort);
                }
                mScanCallBack.onScanBack(mScanResponse);
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
        }


        @Override
        public void onScanFailed(int errorCode) {
            sBluetoothAdapter.getBluetoothLeScanner().stopScan(this);
            scanEnd();
        }
    };

    /**
     * 扫描结束
     */
    private void scanEnd() {
        sHandler.removeCallbacks(mScanTimeoutRunnable);
        mScanResponse.status = SCAN_STATE_END;
        if (mScanLimit.isConnectResultFirst && mScanResponse.results.size() > 0 && !mScanLimit.isConnectScanFirst) {
            connectAndWrite(mScanResponse.results.get(0), mConnectData.connectTimeout, mConnectData.serviceUUID, mConnectData.characteristicUUID,
                    mConnectData.notifyLimit, mConnectData.isNotify, false, mConnectData.callBack);
        }
        mScanCallBack.onScanBack(mScanResponse);
        mScanResponse = null;
        mScanLimit = null;
    }

    /**
     * 扫描结束任务
     */
    private Runnable mScanTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            sBluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
            scanEnd();
        }
    };


    /* ---------------------------------------------------------以下为通信部分------------------------------------------------------ */


    /**
     * 连接蓝牙设备
     *
     * @param scanResult
     * @param isAutoConnect
     * @param callback
     * @return
     */
    public BluetoothGatt connect(@NonNull ScanResult scanResult, boolean isAutoConnect, BluetoothGattCallback callback) {
        BluetoothDevice device = scanResult.getDevice();
        return device.connectGatt(sContext, isAutoConnect, callback);
    }

    /**
     * 连接蓝牙设备
     *
     * @param scanResult
     * @param isAutoConnect
     * @param callback
     * @param transport
     * @return
     */
    public BluetoothGatt connect(@NonNull ScanResult scanResult, boolean isAutoConnect, BluetoothGattCallback callback, int transport) {
        BluetoothDevice device = scanResult.getDevice();
        return device.connectGatt(sContext, isAutoConnect, callback, transport);
    }

    /**
     * 连接通信
     *
     * @param scanResult
     * @param serviceUUID
     * @param characteristicUUID
     * @param callBack
     * @return
     */
    public BluetoothGatt connectAndWrite(@NonNull ScanResult scanResult, String serviceUUID, String characteristicUUID,
                                         @NonNull ConnectCallBack callBack) {
        return connectAndWrite(scanResult, CONNECT_TIME_OUT_DEFAULT, serviceUUID, characteristicUUID, null, false, false, callBack);
    }

    /**
     * 连接通信
     *
     * @param scanResult
     * @param timeout
     * @param serviceUUID
     * @param characteristicUUID
     * @param callBack
     * @return
     */
    public BluetoothGatt connectAndWrite(@NonNull ScanResult scanResult, long timeout, String serviceUUID, String characteristicUUID,
                                         @NonNull ConnectCallBack callBack) {
        return connectAndWrite(scanResult, timeout, serviceUUID, characteristicUUID, null, false, false, callBack);
    }

    /**
     * 连接通信
     *
     * @param scanResult
     * @param serviceUUID
     * @param characteristicUUID
     * @param isAutoConnect
     * @param callBack
     * @return
     */
    public BluetoothGatt connectAndWrite(@NonNull ScanResult scanResult, String serviceUUID, String characteristicUUID, boolean isAutoConnect,
                                         @NonNull ConnectCallBack callBack) {
        return connectAndWrite(scanResult, CONNECT_TIME_OUT_DEFAULT, serviceUUID, characteristicUUID, null, false, isAutoConnect, callBack);
    }

    /**
     * 连接通信
     *
     * @param scanResult
     * @param timeout
     * @param serviceUUID
     * @param characteristicUUID
     * @param isAutoConnect
     * @param callBack
     * @return
     */
    public BluetoothGatt connectAndWrite(@NonNull ScanResult scanResult, long timeout, String serviceUUID, String characteristicUUID,
                                         boolean isAutoConnect, @NonNull ConnectCallBack callBack) {
        return connectAndWrite(scanResult, timeout, serviceUUID, characteristicUUID, null, false, isAutoConnect, callBack);
    }

    /**
     * 连接通信
     *
     * @param scanResult
     * @param serviceUUID
     * @param characteristicUUID
     * @param isNotify
     * @param callBack
     * @return
     */
    public BluetoothGatt connectAndWriteNotify(@NonNull ScanResult scanResult, String serviceUUID, String characteristicUUID,
                                               boolean isNotify, @NonNull ConnectCallBack callBack) {
        return connectAndWrite(scanResult, CONNECT_TIME_OUT_DEFAULT, serviceUUID, characteristicUUID, null, isNotify, false, callBack);
    }

    /**
     * 连接通信
     *
     * @param scanResult
     * @param timeout
     * @param serviceUUID
     * @param characteristicUUID
     * @param isNotify
     * @param callBack
     * @return
     */
    public BluetoothGatt connectAndWriteNotify(@NonNull ScanResult scanResult, long timeout, String serviceUUID, String characteristicUUID,
                                               boolean isNotify, @NonNull ConnectCallBack callBack) {
        return connectAndWrite(scanResult, timeout, serviceUUID, characteristicUUID, null, isNotify, false, callBack);
    }

    /**
     * 连接通信
     *
     * @param scanResult
     * @param serviceUUID
     * @param characteristicUUID
     * @param isNotify
     * @param isAutoConnect
     * @param callBack
     * @return
     */
    public BluetoothGatt connectAndWriteNotify(@NonNull ScanResult scanResult, String serviceUUID, String characteristicUUID, boolean isNotify,
                                               boolean isAutoConnect, @NonNull ConnectCallBack callBack) {
        return connectAndWrite(scanResult, CONNECT_TIME_OUT_DEFAULT, serviceUUID, characteristicUUID, null, isNotify, isAutoConnect, callBack);
    }

    /**
     * 连接通信
     *
     * @param scanResult
     * @param timeout
     * @param serviceUUID
     * @param characteristicUUID
     * @param isNotify
     * @param isAutoConnect
     * @param callBack
     * @return
     */
    public BluetoothGatt connectAndWriteNotify(@NonNull ScanResult scanResult, long timeout, String serviceUUID, String characteristicUUID,
                                               boolean isNotify, boolean isAutoConnect, @NonNull ConnectCallBack callBack) {
        return connectAndWrite(scanResult, timeout, serviceUUID, characteristicUUID, null, isNotify, isAutoConnect, callBack);
    }

    /**
     * 连接通信
     *
     * @param scanResult
     * @param serviceUUID
     * @param characteristicUUID
     * @param notifyLimit
     * @param callBack
     * @return
     */
    public BluetoothGatt connectAndWriteNotify(@NonNull ScanResult scanResult, String serviceUUID, String characteristicUUID,
                                               @NonNull NotifyLimit notifyLimit, @NonNull ConnectCallBack callBack) {
        return connectAndWrite(scanResult, CONNECT_TIME_OUT_DEFAULT, serviceUUID, characteristicUUID, notifyLimit, true, false, callBack);
    }

    /**
     * 连接通信
     *
     * @param scanResult
     * @param timeout
     * @param serviceUUID
     * @param characteristicUUID
     * @param notifyLimit
     * @param callBack
     * @return
     */
    public BluetoothGatt connectAndWriteNotify(@NonNull ScanResult scanResult, long timeout, String serviceUUID, String characteristicUUID,
                                               @NonNull NotifyLimit notifyLimit, @NonNull ConnectCallBack callBack) {
        return connectAndWrite(scanResult, timeout, serviceUUID, characteristicUUID, notifyLimit, true, false, callBack);
    }

    /**
     * 连接通信
     *
     * @param scanResult
     * @param serviceUUID
     * @param characteristicUUID
     * @param notifyLimit
     * @param isAutoConnect
     * @param callBack
     * @return
     */
    public BluetoothGatt connectAndWriteNotify(@NonNull ScanResult scanResult, String serviceUUID, String characteristicUUID,
                                               @NonNull NotifyLimit notifyLimit, boolean isAutoConnect, @NonNull ConnectCallBack callBack) {
        return connectAndWrite(scanResult, CONNECT_TIME_OUT_DEFAULT, serviceUUID, characteristicUUID, notifyLimit, true, isAutoConnect, callBack);
    }

    /**
     * 连接通信
     *
     * @param scanResult         扫描到的设备
     * @param timeout            超时时间
     * @param serviceUUID        远程通信服务UUID
     * @param characteristicUUID 远程通信特性UUID
     * @param notifyLimit        对接收数据限制处理（主要用于并包处理）
     * @param isAutoConnect      是否自动连接（中途断开时）
     * @param callBack           通信任务回调
     * @return 方便拓展
     */
    public BluetoothGatt connectAndWriteNotify(@NonNull ScanResult scanResult, long timeout, String serviceUUID, String characteristicUUID,
                                               @NonNull NotifyLimit notifyLimit, boolean isAutoConnect, @NonNull ConnectCallBack callBack) {
        return connectAndWrite(scanResult, timeout, serviceUUID, characteristicUUID, notifyLimit, true, isAutoConnect, callBack);
    }

    /**
     * 连接通信
     *
     * @param scanResult
     * @param serviceUUID
     * @param characteristicUUID
     * @param notifyLimit
     * @param isNotify
     * @param isAutoConnect
     * @param callBack
     * @return
     */
    private BluetoothGatt connectAndWrite(@NonNull ScanResult scanResult, long timeout, String serviceUUID, String characteristicUUID,
                                          NotifyLimit notifyLimit, boolean isNotify, boolean isAutoConnect, @NonNull ScanAndConnectCallBack callBack) {
        if (mBluetoothGatt != null && mConnectResponse != null) {
            mConnectResponse.status = CONNECT_STATE_DISCONNECTED;
            callBack.onConnectResult(mConnectResponse);
            return mBluetoothGatt;
        }
        initConnect();
        mConnectCallBack = callBack;
        mServiceUUID = serviceUUID;
        mCharacteristicUUID = characteristicUUID;
        mNotifyLimit = notifyLimit;
        mIsNotify = isNotify;
        mConnectResponse = new ConnectResponse();
        mConnectResponse.status = CONNECT_STATE_DISCONNECTED;
        BluetoothDevice device = scanResult.getDevice();
        mBluetoothGatt = device.connectGatt(sContext, isAutoConnect, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                sHandler.obtainMessage(CONNECT_STATE_TAG, status, newState, gatt).sendToTarget();
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                sHandler.obtainMessage(SERVICE_STATE_TAG, status, 0, gatt).sendToTarget();
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                processOnCharacteristicWrite(gatt, characteristic, status);
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                processOnCharacteristicChanged(gatt, characteristic);
            }
        });
        timeout = timeout <= CONNECT_TIME_MIN ? CONNECT_TIME_MIN : timeout;
        sHandler.postDelayed(connectTimeoutRunnable, isTimeout ? timeout * 2 : timeout);
        return mBluetoothGatt;
    }

    /**
     * 刷新缓存
     *
     * @param gatt
     * @return
     */
    public static boolean refreshDeviceCache(BluetoothGatt gatt) {
        try {
            BluetoothGatt localBluetoothGatt = gatt;
            Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
            if (localMethod != null) {
                boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
                return bool;
            }
        } catch (Exception localException) {

        }
        return false;
    }

    /**
     * 连接超时任务
     */
    private static Runnable connectTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            isTimeout = true;
            if (mBluetoothGatt != null) {
                boolean b = refreshDeviceCache(mBluetoothGatt);
                mBluetoothGatt.disconnect();
                mBluetoothGatt.close();
            }

            if (mConnectResponse != null) {
                mConnectResponse.status = CONNECT_OR_WRITE_TIME_OUT;
                mConnectCallBack.onRunnableTimeout(mConnectResponse);
                initConnect();
            }
        }
    };


    /* ---------------------------------------------------------以下为扫描连接部分------------------------------------------------------ */


    /**
     * 扫描到第一个满足条件设备就连接
     *
     * @param nameFilters
     * @param sort
     * @param connectTimeout
     * @param serviceUUID
     * @param characteristicUUID
     * @param isNotify           是否接收数据（接收的数据没有并包）
     * @param callBack
     */
    public void scanFirstAndConnect(@Nullable List<String> nameFilters, long connectTimeout, String serviceUUID, String characteristicUUID,
                                    boolean isNotify, @Nullable Comparator<ScanResult> sort, @NonNull ScanAndConnectCallBack callBack) {
        mConnectData = new ConnectData(connectTimeout, serviceUUID, characteristicUUID, null, isNotify, callBack);
        scanDevice(nameFilters, true, false, SCAN_TIME_DEFAULT, true, sort, callBack);
    }

    /**
     * 扫描到第一个满足条件设备就连接
     *
     * @param nameFilters
     * @param sort
     * @param connectTimeout
     * @param serviceUUID
     * @param characteristicUUID
     * @param notifyLimit        默认接收数据，并将接收数据并包处理
     * @param callBack
     */
    public void scanFirstAndConnect(@Nullable List<String> nameFilters, long connectTimeout, String serviceUUID, String characteristicUUID,
                                    @NonNull NotifyLimit notifyLimit, @Nullable Comparator<ScanResult> sort, @NonNull ScanAndConnectCallBack callBack) {
        mConnectData = new ConnectData(connectTimeout, serviceUUID, characteristicUUID, notifyLimit, true, callBack);
        scanDevice(nameFilters, true, false, SCAN_TIME_DEFAULT, true, sort, callBack);
    }

    /**
     * 扫描完成并连接设备集合的第一个设备
     *
     * @param nameFilters
     * @param sort
     * @param scanTime
     * @param connectTimeout
     * @param serviceUUID
     * @param characteristicUUID
     * @param isNotify           是否接收数据（接收的数据没有并包）
     * @param callBack
     */
    public void scanAndConnectFirst(@Nullable List<String> nameFilters, long scanTime, long connectTimeout, String serviceUUID,
                                    String characteristicUUID, boolean isNotify, @Nullable Comparator<ScanResult> sort, @NonNull ScanAndConnectCallBack callBack) {
        mConnectData = new ConnectData(connectTimeout, serviceUUID, characteristicUUID, null, isNotify, callBack);
        scanDevice(nameFilters, false, true, scanTime, true, sort, callBack);
    }

    /**
     * 扫描完成并连接设备集合的第一个设备
     *
     * @param nameFilters
     * @param sort
     * @param scanTime
     * @param connectTimeout
     * @param serviceUUID
     * @param characteristicUUID
     * @param notifyLimit        默认接收数据，并将接收数据并包处理
     * @param callBack
     */
    public void scanAndConnectFirst(@Nullable List<String> nameFilters, long scanTime, long connectTimeout, String serviceUUID, String characteristicUUID,
                                    @NonNull NotifyLimit notifyLimit, @Nullable Comparator<ScanResult> sort, @NonNull ScanAndConnectCallBack callBack) {
        mConnectData = new ConnectData(connectTimeout, serviceUUID, characteristicUUID, notifyLimit, true, callBack);
        scanDevice(nameFilters, false, true, scanTime, true, sort, callBack);
    }


    /* ---------------------------------------------------------以下为分并包部分------------------------------------------------------ */


    /**
     * 分包处理
     *
     * @param bytes
     * @return
     */
    public static byte[][] detachMsg(byte[] bytes) {
        byte[] outProcess = bytes;
        int length = (outProcess.length / 20) + 1;
        byte[][] results = new byte[length][];
        if (outProcess.length > 20) {
            for (int i = 0; i < length; i++) {
                byte[] temps = null;
                if (outProcess.length - 20 * i > 20) {
                    temps = new byte[20];
                    for (int j = 20 * i, k = 0; j < 20 * (i + 1) && k < 20; j++, k++) {
                        temps[k] = outProcess[j];
                    }
                } else {
                    temps = new byte[outProcess.length - 20 * i];
                    for (int j = 20 * i, k = 0; j < outProcess.length && k < 20; j++, k++) {
                        temps[k] = outProcess[j];
                    }
                }
                results[i] = temps;
            }
        } else {
            results[0] = outProcess;
        }

        return results;
    }

    /**
     * 并包处理
     *
     * @param first
     * @param rest
     * @return
     */
    public static byte[] mergeMsg(byte[] first, byte[]... rest) {
        int totalLength = first.length;
        for (byte[] array : rest) {
            totalLength += array.length;
        }
        byte[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (byte[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    /**
     * 获取索引
     *
     * @param array
     * @param value
     * @return
     */
    public static int indexOfValue(@NonNull byte[] array, byte value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }
}
