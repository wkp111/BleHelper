# BleHelper
蓝牙通信帮助库
<br>
<br>
## Gradle集成<br>
compile 'com.wkp:BleHelper:1.0.5'
<br>
<br>
## 使用举例<br>
1.manifest配置<br>
\<!--权限配置--><br>
`<uses-permission android:name="android.permission.BLUETOOTH"/>`<br>
`<uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>`<br>
`<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>`<br>
\<!--是否应用只用于具有Ble设备上--><br>
`<uses-feature
        android:name="android.hardware.bluetooth_le"
        android:required="false"/>`<br>
\<!--动态权限申请界面--><br>
`<activity android:name="com.wkp.blehelper.activity.InvisibleActivity"/>`
<br>
<br>
2.代码示例<br>
<code>
//api 21

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)

public class MainActivity extends AppCompatActivity {

    private TextView mTextView;
    private ListView mListView;
    private List<ScanResult> mScanResults = new ArrayList<>();
    private BleHelper mBleHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = ((TextView) findViewById(R.id.tv));
        mListView = ((ListView) findViewById(R.id.lv));
        mListView.setAdapter(mAdapter);
        //获取单例
        mBleHelper = BleHelper.getInstance(this);
    }

    private BaseAdapter mAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return mScanResults.size();
        }

        @Override
        public Object getItem(int i) {
            return mScanResults.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            TextView textView = new TextView(viewGroup.getContext());
            textView.setText(mScanResults.get(i).getDevice().getName());
            return textView;
        }
    };
    
    //点击扫描连接
    public void scanConnect(View view) {
        final long start = System.currentTimeMillis();
        //扫描过滤设备名（包含）
        List<String> nameFilters = new ArrayList<>();
        nameFilters.add("pk");
        //接收数据限制（主要用于并包）
        NotifyLimit notifyLimit = new NotifyLimit(BluetoothRelativeUtils.STX, BluetoothRelativeUtils.ETX, 2);
        //扫描时间   通信超时时间
        mBleHelper.scanAndConnectFirst(nameFilters, 800, 5000, UUIDConstants.PEAKE_SERVICE_UUID, UUIDConstants.PEAKE_CHARACTER_UUID, notifyLimit, new Comparator<ScanResult>() {
            //扫描设备排序
            @Override
            public int compare(ScanResult scanResult, ScanResult t1) {
                return (int) (((Math.abs(scanResult.getRssi()) - 59) / (10 * 2.0) - (Math.abs(t1.getRssi()) - 59) / (10 * 2.0)) * 10);
            }
        }, new ScanAndConnectCallBack() {
            //扫描回调
            @Override
            public boolean onScanBack(ScanResponse scanResponse) {
                switch (scanResponse.status) {
                    case BleHelper.SCAN_STATE_ERROR_NO_SUPPORT:
                        mTextView.setText("设备不支持Ble");
                        break;
                    case BleHelper.SCAN_STATE_ERROR_NO_PERMISSION:
                        mTextView.setText("没有Ble权限");
                        break;
                    case BleHelper.SCAN_STATE_START:
                        mTextView.setText("开始扫描");
                        mScanResults.clear();
                        mAdapter.notifyDataSetChanged();
                        break;
                    case BleHelper.SCAN_STATE_FOUND:
                        mTextView.setText(scanResponse.scanResult.getDevice().getName());
                        break;
                    case BleHelper.SCAN_STATE_END:
                        mTextView.setText("扫描完毕");
                        mScanResults.addAll(scanResponse.results);
                        mAdapter.notifyDataSetChanged();
                        break;
                }
                return false;
            }
            //连接回调
            @Override
            public void onConnectResult(ConnectResponse connectResponse) {
                switch (connectResponse.status) {
                    case BleHelper.CONNECT_STATE_DISCONNECTED:
                        mTextView.setText("未连接");
                        break;
                    case BleHelper.CONNECT_STATE_CONNECTING:
                        mTextView.setText("正在连接");
                        break;
                    case BleHelper.CONNECT_STATE_CONNECTED:
                        mTextView.setText("已连接");
                        break;
                }
            }
            //发现服务回调，return null为结束通信；字节数组为要发送内容（支持内部分包）
            @Nullable
            @Override
            public byte[] onDiscoverServiceResult(ConnectResponse connectResponse) {
                try {
                    mTextView.setText("发现服务");
                    return sendMsg((byte) 0x8, AESUtils.cPassWord);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
            //发送成功回调，return true为结束通信；false为继续通信
            @Override
            public boolean onWriteResult(ConnectResponse connectResponse) {
                mTextView.setText("发送完成");
                return false;
            }
            //接收数据回调，return false为不发送数据；true为发送数据；null为结束通信
            @NonNull
            @Override
            public DoubleData<Boolean, byte[]> onNotifyResult(ConnectResponse connectResponse) {
                byte[] notifyValue = connectResponse.notifyValue;
                byte[] inProcess = BluetoothRelativeUtils.PackInProcess(notifyValue);
                if (inProcess.length >= 2 && inProcess[0] == 0x4) {
                    if (inProcess[1] == 0x00) {
                        Toast.makeText(MainActivity.this, "开门成功", Toast.LENGTH_SHORT).show();
                    } else if (inProcess[1] == 0x01) {
                        Toast.makeText(MainActivity.this, "开门失败", Toast.LENGTH_SHORT).show();
                    }
                    long end = System.currentTimeMillis();
                    long time = end - start;
                    mTextView.setText("通信完成:"+time / 1000+"秒");
                    return new DoubleData<Boolean, byte[]>(null, null);
                } else if (inProcess.length > 1 && inProcess[0] == 0x9) {
                    try {
                        byte[][] nextReceive = BluetoothRelativeUtils.nextReceive(inProcess);
                        byte[] randomKey = AESUtils.generateRandomKey(nextReceive[0]);
                        byte[] decrypt = AESUtils.decrypt(nextReceive[1], randomKey);
                        if (Arrays.equals(AESUtils.cPassWord, decrypt)) {
                            mTextView.setText("校验成功");
                            byte[] cardNo = AESUtils.toDestByte("89686524");
                            return new DoubleData<Boolean, byte[]>(true, sendMsg((byte) 0xA, cardNo));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return new DoubleData<Boolean, byte[]>(false, null);
            }
            //通信超时回调
            @Override
            public void onRunnableTimeout(ConnectResponse connectResponse) {
                mTextView.setText("通信超时");
            }
        });
    }
}
</code>
