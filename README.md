# BleHelper [ ![Download](https://api.bintray.com/packages/wkp/maven/BleHelper/images/download.svg) ](https://bintray.com/wkp/maven/BleHelper/_latestVersion)
蓝牙通信帮助库
<br>
<br>
## Gradle集成<br>
```groovy

dependencies{
      compile 'com.wkp:BleHelper:1.0.7'
      //Android Studio3.0+可用以下方式
      //implementation 'com.wkp:BleHelper:1.0.7'
} 
```
Note：可能存在Jcenter还在审核阶段，这时会集成失败！
<br>
## 使用举例<br>
```java
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
```
Note：代码仅供参考，更多API请根据实际设定！

## 注意事项<br/>
* 部分Android6.0以上手机扫描不到蓝牙设备，需要开启位置定位功能。<br/>
```java      
    //判断位置定位是否打开   
    public static final boolean isLocationEnable(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean networkProvider = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean gpsProvider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (networkProvider || gpsProvider) return true;
        return false;
    }
        
    //打开位置信息设置界面
    private void setLocationService() {
        Intent locationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        this.startActivityForResult(locationIntent, REQUEST_CODE_LOCATION_SETTINGS);
    }
```
Note：也可以自己直接打开定位功能！
## 寄语<br/>
控件支持直接代码创建，还有更多API请观看<a href="https://github.com/wkp111/BleHelper/blob/master/blehelper-lib/src/main/java/com/wkp/blehelper/ble/BleHelper.java">BleHelper.java</a>内的注释说明。<br/>
欢迎大家使用，感觉好用请给个Star鼓励一下，谢谢！<br/>
大家如果有更好的意见或建议以及好的灵感，请邮箱作者，谢谢！<br/>
QQ邮箱：1535514884@qq.com<br/>
163邮箱：15889686524@163.com<br/>
Gmail邮箱：wkp15889686524@gmail.com<br/>

## 版本更新<br/>
* v1.0.7<br/>
内置动态权限申请界面，内置所需权限配置，无需再mainfest重新配置<br/><br/>
* v1.0.6<br/>
修复部分6.0以上手机扫描通信BUG<br/><br/>
* v1.0.5<br/>
新创建蓝牙4.0帮助库<br/>
## License

   Copyright 2017 wkp

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
