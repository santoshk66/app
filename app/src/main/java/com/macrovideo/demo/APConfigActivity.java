package com.macrovideo.demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.macrovideo.animate.RadarView;
import com.macrovideo.sdk.defines.Defines;
import com.macrovideo.sdk.defines.ResultCode;
import com.macrovideo.sdk.media.ILoginDeviceCallback;
import com.macrovideo.sdk.media.LoginHandle;
import com.macrovideo.sdk.media.LoginHelper;
import com.macrovideo.sdk.objects.DeviceInfo;
import com.macrovideo.sdk.objects.LoginParam;
import com.macrovideo.sdk.setting.DeviceNetworkSetting;
import com.macrovideo.sdk.setting.NetworkConfigInfo;
import com.macrovideo.sdk.tools.DeviceScanner;
import com.macrovideo.sdk.tools.Functions;
import com.macrovideo.sdk.tools.GlobalDefines;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class APConfigActivity extends Activity implements
        View.OnClickListener {
    private static final String TAG = "APConfigActivity";
    // //WIFI
    private WiFiAdnim mWiFiAdnim;
    private WifiManager mWiFiManager; // 定义一个WifiManager
    private WifiReceiver mwReceiver; // 定义一个WifiReceiver
    private android.net.wifi.WifiInfo mWifiInfo; // 定义一个wifiInfo
    private List<ScanResult> locaWifiDeiviceList = new ArrayList<ScanResult>();

    private LinearLayout lLayoutWifiInputPage; // 显示或隐藏准备配置，开始配置
    private ImageView btnSLBack, ivSLPwdVisible; // 返回按钮，开始声波配置按钮，现实隐藏wifi密码按钮
    private Button btnSLSearchBack = null;
    private Button btnSLStartConfig; // 下一步;
    private Button btnWifiQuikConfig;// AP配置
    private EditText etSLWifiSSID, etSLWifiPassword; // wifi名称，wifi密码

    private View soundWaveConfigConctentView = null;// ,
    // soundWaveConfigDemoState
    // = null;
    // //用于加载listView的View
    private Dialog soundWaveConfigDialog = null;// ,
    // soundWaveConfigDemoStateDialog
    // = null; //
    // 用于现实ListView的DiaLog
    private ImageView ivSoundWaveConfigWifiListViewBack; // wifi列表返回按钮
    private ListView lvSoundWaveConfigWifi; // 声波配置的wifi列表

    private ProgressDialog progressDialog; // 用于确定wifi列表是否出现
    private boolean bWifiPassword = true; // 用于判断是否隐藏密码

    private LinearLayout llayoutSLSearchingPage; // 显示或隐藏准备配置，开始配置
    private FrameLayout flayoutSLSearchingAnimate; // 显示搜索界面
    private RadarView searchAminatView;
    private String strConnSSID; // 当前手机连接的wifi用户名
    private MediaPlayer soundPlayer = null;// 声音播放
    private MediaPlayer soundPlayerHint = null; //

    private static final int WIFI_CONNECT = 0x11;
    private static final int WIFI_CONNECT2 = 0x12;
    private static final int WIFI_NOT_CONNECT = 0x14;
    private static final int SEEK_DEVICE_OVERTIME = 0x13;
    private static final int MY_PERMISSION_REQUEST_LOCATION = 0;
    private boolean bWifiOpen = false;
    private AlertDialog.Builder wifiNoticeDialog = null;
    private boolean bIsNoticeShow = false; //
    private boolean bIsConfiging = false; //

    private int nTimeoutDetectID = 0;

    private TextView tvTimeLeft = null;

    private ArrayList<DeviceInfo> deviceList = null;

    private int nConfigID = 0;
    private int mWifiEnrcrypt;

    private int n_BindDeviceThreadID = 0;
    private int bindDevice_result;

    private DeviceInfo Editinfo;

    boolean bHasUpdate = false;
    boolean bNewDevFound = false;

    static final int DEVICE_IS_EXISTANCE = 10004; // 设备已存在，添加失败

    private LinearLayout llWifiQuikConfig;
    private String strWiFiSSID = null;
    private String strWiFiPassword = null;
    private DeviceInfo mDeviceInfo = null;
    private int mConfigHotspotToWifiID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题 
        setContentView(R.layout.activity_ap_config);
        initView(); // 初始化界面控件
        initWifi(); // 初始化wifi 参数 并开启wifi广播
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();

        //initWifi(); // 初始化wifi 参数 并开启wifi广播
        //wifiChooseWindow(); // 初始化现实wifi列表的Dialog
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
        if (mWiFiManager != null) {
            WifiInfo wifiInfo = mWiFiManager.getConnectionInfo();
            if (wifiInfo != null) {
                String wifiName = wifiInfo.getSSID().replace("\"", "");
                Log.d(TAG, "onResume: wifiName=" + wifiName + " " + strWiFiSSID + " " + strWiFiPassword);
                if (!"<unknown ssid>".equals(wifiName) && wifiName.startsWith("MV") && strWiFiSSID != null && strWiFiPassword != null) {
                    Log.d(TAG, "onResume: start config");
                    String strDeviceID = wifiName.substring(2);
                    try {
                        mDeviceInfo = new DeviceInfo(-1, Integer.parseInt(strDeviceID), strDeviceID,
                                "192.168.1.1", 8800, "admin", "", null, null, 0);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }

                    // 连上指定的热点后进行热点配置
                    mConfigHotspotToWifiID++;
                    new ConfigHotspotToWifiThread(mConfigHotspotToWifiID, this, strWiFiPassword).start();

                    // 重新加载声音
                    if (isLanguage()) {
                        if (soundPlayerHint != null) {
                            soundPlayerHint.stop();
                        }

                        soundPlayerHint = MediaPlayer
                                .create(this, R.raw.device_perpare);
                        soundPlayerHint.setLooping(false);
                        soundPlayerHint.start();
                    }

                    nConfigID = LocalDefines.getConfigID();
                    bIsConfiging = true;

                    searchAminatView.startAnimate();
                    showSearchingPage();

//                    StartSearchDevice(); // 开始搜索设备
                    if (isLanguage() && soundPlayerHint != null) {
                        soundPlayerHint.stop(); // 关闭声音
                    }
                    soundPlayer = MediaPlayer.create(this, R.raw.seekmusic); // 加入声音
                    soundPlayer.setLooping(true);
                    soundPlayer.start();

                    tvTimeLeft.setText("" + 80);
                    nTimeoutDetectID++;

                    CountDownTimer timer = new CountDownTimer(80000, 1000) {
                        int nThreadID = nTimeoutDetectID;
                        int nCount = 80;

                        @Override
                        public void onTick(long millisUntilFinished) {
                            if (nThreadID != nTimeoutDetectID) {

                                return;
                            }
                            if (tvTimeLeft != null) {
                                nCount--;
                                if (nCount < 0)
                                    nCount = 0;
                                try {
                                    tvTimeLeft.setText("" + nCount);
                                } catch (Exception e) {

                                }

                            }
                        }

                        @Override
                        public void onFinish() {
                            if (nThreadID == nTimeoutDetectID) { //

                                Message msg = handler.obtainMessage();
                                msg.arg1 = SEEK_DEVICE_OVERTIME;
                                handler.sendMessage(msg);
                            }
                        }
                    };
                    timer.start();


                }
            }
        }
    }

    /**
     * 初始化控件
     */
    @SuppressWarnings("deprecation")
    private void initView() {
        llWifiQuikConfig = (LinearLayout) findViewById(R.id.llWifiQuikConfig);
        lLayoutWifiInputPage = (LinearLayout) findViewById(R.id.lLayoutWifiInputPage);

        btnSLBack = (ImageView) findViewById(R.id.btnSLBack);
        btnSLBack.setOnClickListener(this);

        ivSLPwdVisible = (ImageView) findViewById(R.id.ivSLPwdVisible);
        ivSLPwdVisible.setOnClickListener(this);

        btnSLStartConfig = (Button) findViewById(R.id.btnSLStartConfig);
        btnSLStartConfig.setOnClickListener(this);


        btnWifiQuikConfig = (Button) findViewById(R.id.btnWifiQuikConfig);// add
        // by
        // lin
        // 20160123
        boolean isZh = LocalDefines.isZh(this);
        //llWifiQuikConfig.setVisibility(isZh ? View.VISIBLE : View.GONE);
        btnWifiQuikConfig.setOnClickListener(this);

        etSLWifiSSID = (EditText) findViewById(R.id.etSLWifiSSID);
        etSLWifiSSID.setInputType(InputType.TYPE_NULL);
        etSLWifiSSID.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    etSLWifiSSID.setEnabled(false);
                } else {
                    etSLWifiSSID.setEnabled(true);
                }
            }
        });
        etSLWifiPassword = (EditText) findViewById(R.id.etSLWifiPassword);

        lLayoutWifiInputPage.setVisibility(View.VISIBLE);
        etSLWifiPassword
                .setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD); // 默认显示密码

        llayoutSLSearchingPage = (LinearLayout) findViewById(R.id.llayoutSLSearchingPage);
        flayoutSLSearchingAnimate = (FrameLayout) findViewById(R.id.flayoutSLSearchingAnimate);
        flayoutSLSearchingAnimate.setBackgroundColor(Color
                .parseColor("#f9f9f9"));
        llayoutSLSearchingPage.setVisibility(View.GONE);

        btnSLSearchBack = (Button) findViewById(R.id.btnSLSearchBack);
        btnSLSearchBack.setOnClickListener(this);
        // gif图
        searchAminatView = (RadarView) findViewById(R.id.searchAminatView);
        searchAminatView.setWillNotDraw(false); //
        tvTimeLeft = (TextView) findViewById(R.id.tvTimeLeft);
    }

    /**
     * 初始化wifi
     */
    private void initWifi() {
        mWiFiAdnim = new WiFiAdnim(this);
        mWiFiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mWifiInfo = mWiFiManager.getConnectionInfo();

        // 如果手机有连接wifi 则
        if (mWiFiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED
                && mWifiInfo != null) { // 如果wifi是打开状态
            strConnSSID = mWifiInfo.getSSID();
            // System.out.println("GetSSID= "+strConnSSID+", "+strConnSSID.equalsIgnoreCase("0x"));//

            if (strConnSSID != null && strConnSSID.length() > 0
                    && !strConnSSID.equalsIgnoreCase("0x") && !strConnSSID.equalsIgnoreCase("<unknown ssid>")) {
                // System.out.println("GetSSID= "+strConnSSID.substring(0,
                // 1)+", "+strConnSSID.substring(strConnSSID.length()-1,
                // strConnSSID.length()));//add for test

                if (strConnSSID.substring(0, 1).equals("\"")
                        && strConnSSID.substring(strConnSSID.length() - 1,
                        strConnSSID.length()).equals("\"")) {
                    strConnSSID = strConnSSID.substring(1,
                            (strConnSSID.length() - 1)); // 得到当前连接的用户铭，去掉前后双引号
                }

                etSLWifiSSID.setText(strConnSSID);
            } else {
                etSLWifiSSID.setText("");
            }
            // btnSLStartConfig.setEnabled(true);
            if (lLayoutWifiInputPage.getVisibility() == View.VISIBLE) {
                if (isLanguage()) {
                    soundPlayerHint = MediaPlayer.create(this,
                            R.raw.input_wifi_pwd);
                    soundPlayerHint.setLooping(false);
                    soundPlayerHint.start();
                }
            }

        } else { // wifi没有开启
            if (!bIsNoticeShow) {

                View view = View
                        .inflate(this, R.layout.show_alert_dialog, null);
                TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
                tv_title.setText(getString(R.string.wifiConnect));
                TextView tv_content = (TextView) view
                        .findViewById(R.id.tv_content);
                tv_content.setText(getString(R.string.wifi_start_bt));

                wifiNoticeDialog = new AlertDialog.Builder(this);
                wifiNoticeDialog.setView(view);
                wifiNoticeDialog.setPositiveButton(getString(R.string.wifi_is),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                mWiFiAdnim.openWifi(); // 开启wifi
                                bIsNoticeShow = false;
                                progressDialog = new ProgressDialog(
                                        APConfigActivity.this);
                                progressDialog
                                        .setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                progressDialog.setCanceledOnTouchOutside(false);
                                progressDialog
                                        .setMessage(getString(R.string.wifi_start));
                                progressDialog.show(); // 显示进度条

                            }
                        });
                wifiNoticeDialog.setNegativeButton(getString(R.string.wifi_no),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                bIsNoticeShow = false;
                            }
                        });
                wifiNoticeDialog.show();
                bIsNoticeShow = true;
            }

        }
        // /注册wifi广播接收器
        mwReceiver = new WifiReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION); // 网络状态改变
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION); // wifi 状态改变
        registerReceiver(mwReceiver, intentFilter);
        // //
        mWiFiAdnim.startScan(); // 开始扫描网络

    }

    /**
     * 创建一个内部类 进行广播扫描出来的热点信息
     *
     * @author Administrator
     */
    public class WifiReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {

            if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                // 网络状态改变
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                    etSLWifiSSID.setText("");
                } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {

                    mWifiInfo = mWiFiManager.getConnectionInfo();

                    // 获取当前wifi名称
                    strConnSSID = mWifiInfo.getSSID();
                    if (strConnSSID != null && strConnSSID.length() > 0 && !strConnSSID.equalsIgnoreCase("0x") && !strConnSSID.equalsIgnoreCase("<unknown ssid>")) {

                        if (strConnSSID.substring(0, 1).equals("\"")
                                && strConnSSID.substring(strConnSSID.length() - 1, strConnSSID.length()).equals("\"")) {
                            strConnSSID = strConnSSID.substring(1, (strConnSSID.length() - 1)); // 得到当前连接的用户名，去掉前后双引号
                        }

                        etSLWifiSSID.setText(strConnSSID);
                    } else {
                        etSLWifiSSID.setText("");
                    }
                }

            } else if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {//wifi打开与否
                int wifistate = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED);

                if (wifistate == WifiManager.WIFI_STATE_DISABLED) {
                    btnSLStartConfig.setEnabled(false);
                } else if (wifistate == WifiManager.WIFI_STATE_ENABLED) {
                    btnSLStartConfig.setEnabled(true);
                }
            }
            locaWifiDeiviceList = mWiFiManager.getScanResults(); // 存放所有热点信息

            // 根据信号的强度对locaWifiDeiviceList进行排序
            if (!locaWifiDeiviceList.isEmpty()) {
                Collections.sort(locaWifiDeiviceList,
                        new Comparator<ScanResult>() {
                            @Override
                            public int compare(ScanResult object1,
                                               ScanResult object2) {
                                int i = Math.abs(object1.level)
                                        + "".compareTo(Math.abs(object2.level)
                                        + "");
                                return i;
                            }
                        });
            }

            if (locaWifiDeiviceList != null && locaWifiDeiviceList.size() > 0
                    && progressDialog != null) {
                progressDialog.dismiss();
                bWifiOpen = true;
            }
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSLSearchBack: // 点击返回按钮
                searchAminatView.stopAnimate();
                try {
                    if (isLanguage() && soundPlayerHint != null) {
                        soundPlayerHint.stop(); // 关闭提示音
                    }

                    if (soundPlayer != null) {
                        soundPlayer.stop(); // 关闭声音
                    }
                } catch (Exception e) {

                }

                bIsConfiging = false;

                DeviceScanner.reset();
                showInputPage();
                nTimeoutDetectID++;

                break;
            case R.id.btnSLBack: // 点击返回按钮

                bIsConfiging = false;
                DeviceScanner.reset();

                try {
                    if (isLanguage() && soundPlayerHint != null) {
                        soundPlayerHint.stop(); // 关闭提示音
                    }

                    if (soundPlayer != null) {
                        soundPlayer.stop(); // 关闭声音
                    }
                } catch (Exception e) {

                }

                //Intent intent = new Intent(SmartLinkQuickWifiConfigActivity.this, MainActivity.class);
                //startActivity(intent);

                APConfigActivity.this.finish();

                break;

            case R.id.btnSLStartConfig: // 点击下一步按钮

                String strSSID = etSLWifiSSID.getText().toString();
                String strPassword = etSLWifiPassword.getText().toString();
                strWiFiSSID = strSSID;
                strWiFiPassword = strPassword;

                ShowAlert("", "请在手机系统设置中，将手机WiFi连接到“MV”开头的网络，再返回APP。", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        startActivity(intent);
                    }
                });

                break;
            case R.id.ivSoundWaveConfigBack: // wifi列表窗口返回按钮

                if (soundWaveConfigDialog != null) {
                    soundWaveConfigDialog.dismiss();
                }

                break;

            case R.id.ivSLPwdVisible: // 点击了显示隐藏密码按钮

                if (bWifiPassword) {
                    bWifiPassword = false;
                    etSLWifiPassword.setInputType(InputType.TYPE_CLASS_TEXT
                            | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    ivSLPwdVisible.setImageResource(R.drawable.netword_hide);
                    Editable etable = etSLWifiPassword.getText();
                    Selection.setSelection(etable, etable.length());
                } else {

                    bWifiPassword = true;
                    etSLWifiPassword
                            .setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    ivSLPwdVisible
                            .setImageResource(R.drawable.netword_show_password);
                    Editable etable = etSLWifiPassword.getText();
                    Selection.setSelection(etable, etable.length());
                }

                break;

            case R.id.btnWifiQuikConfig:// add by lin 20160123 AP配置
//                if (bIsSearching) {
//                    StopSearchDevice(); // 停止刷新
//                }
//
//                if (Build.VERSION.SDK_INT >= 23) {
//                    initGPS();
//                } else {
//                    Intent intentSeekFine = new Intent(this, DeviceQuickConfigActivity.class);
//                    this.startActivity(intentSeekFine);
//                    this.overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
//                    this.finish();
//                }
                break;

            default:
                break;
        }
    }

    /**
     * 判断当前语言
     *
     * @return
     */
    public boolean isLanguage() {
        boolean bisLanguage = false;

        String locale = Locale.getDefault().getLanguage();
        if (locale.equals("zh")) // 如果是zhongwen
        {
            bisLanguage = true;
        }
        return bisLanguage;
    }

    private void showInputPage() {

        lLayoutWifiInputPage.setVisibility(View.VISIBLE);
        llayoutSLSearchingPage.setVisibility(View.GONE);
    }

    private void showSearchingPage() {
        lLayoutWifiInputPage.setVisibility(View.GONE);
        llayoutSLSearchingPage.setVisibility(View.VISIBLE);
    }

    /**
     * // 连接指定wifi add  2016年6月21日
     *
     * @param ssid
     * @param psw
     * @param type
     */
    private void connectToSpecifiedWifi(String ssid, String psw, int type) {
        mWiFiAdnim.addNetWork(mWiFiAdnim.CreateWifiInfo(ssid, psw, type)); // 连接wifi
    }

    // 开始设备搜索
    public boolean StartSearchDevice() {

        try {
            if (!Functions.isNetworkAvailable(this.getApplicationContext())) {// 网络不可用

                Toast toast = Toast.makeText(this.getApplicationContext(),
                        getString(R.string.toast_network_unreachable),
                        Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        startBroadCastUdpThread();

        return true;

    }

    private int m_nSearchID = 0;//
    private boolean bIsSearching = false;
    private boolean mIsSearchingMode = false;

    private Handler handler = new Handler() {
        @SuppressLint("HandlerLeak")
        public void handleMessage(Message msg) {

            if (msg.arg1 == WIFI_CONNECT2) {
                if (!bWifiOpen) {
                    if (progressDialog != null) { // 如果wifi列表没刷新出来
                        progressDialog.dismiss();
                        Toast.makeText(APConfigActivity.this,
                                getString(R.string.wifiListingFail),
                                Toast.LENGTH_SHORT).show();
                    }
                }

            }

            // 设备收索超时
            if (msg.arg1 == SEEK_DEVICE_OVERTIME) {
                // 超时处理
                nTimeoutDetectID++;
                searchAminatView.stopAnimate();
                try {
                    if (isLanguage() && soundPlayerHint != null) {
                        soundPlayerHint.stop(); //
                    }

                    if (soundPlayer != null) {
                        soundPlayer.stop(); //
                    }
                } catch (Exception e) {

                }

                bIsConfiging = false;
                DeviceScanner.stopSmartConnection();
                showInputPage();

                ShowAlert(getString(R.string.snartLinkFailTitle),
                        getString(R.string.snartLinkFailHint));

            }

            // wifi连接成功
            if (msg.arg1 == WIFI_CONNECT) {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
            }

            // wifi连接失败
            if (msg.arg1 == WIFI_NOT_CONNECT) {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }

                Toast.makeText(APConfigActivity.this,
                        getString(R.string.connect_wifi_off),
                        Toast.LENGTH_SHORT).show();
            }

            // 搜索设备成功
            if (msg.arg1 == LocalDefines.DEVICE_SEARCH_RESULT) {
                nTimeoutDetectID++;
                bIsConfiging = false;

                DeviceScanner.reset();
                StopSearchDevice();
                DeviceInfo info = null;
                switch (msg.arg2) {
                    case LocalDefines.DEVICE_SEARCH_RESULT_OK:

                        if (deviceList != null && deviceList.size() > 0) {

                            // 成功搜索到设备
                            nTimeoutDetectID++;
                            if (soundPlayer != null) {
                                soundPlayer.stop();
                            }

                            lLayoutWifiInputPage.setVisibility(View.VISIBLE);
                            llayoutSLSearchingPage.setVisibility(View.GONE);
                            StopSearchDevice(); // 停止搜索设备

                            Toast toast = Toast.makeText(APConfigActivity.this,
                                    "配置完成", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();

                            MainActivity.deviceInfo = deviceList.get(0);

                            //Intent intent = new Intent(SmartLinkQuickWifiConfigActivity.this, MainActivity.class);
                            //startActivity(intent);

                            finish();

                        } else {
                            Toast toast = Toast.makeText(
                                    APConfigActivity.this,
                                    getString(R.string.no_dev_found),
                                    Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            StartSearchDevice(); // 开始搜索设备
                        }

                        break;
                    case LocalDefines.DEVICE_SEARCH_RESULT_FAIL:

                        Toast toast = Toast.makeText(
                                APConfigActivity.this,
                                getString(R.string.no_dev_found),
                                Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        break;
                }

            } else if (msg.arg1 == LocalDefines.BIND_DEVICE_RESULT_CODE) {
                String searchResultMsg = getString(R.string.add_device);
                Bundle bundle = msg.getData();
                DeviceInfo info2 = (DeviceInfo) bundle
                        .getParcelable("Bind_info");
                int index = bundle.getInt("Bind_index");
                if (msg.arg2 == 0) {

//                    if (!DatabaseManager.IsInfoExist(info2)) {
//                        bNewDevFound = true;
//                        if (DatabaseManager.AddServerInfo(info2)) {
//
//                            if (index == 0) {
//                                searchResultMsg = searchResultMsg
//                                        + info2.getStrName();
//                            } else {
//                                searchResultMsg = searchResultMsg + ", "
//                                        + info2.getStrName();
//                            }
//                        }
//                    } else {// 如果设备已存在，则更新一下ip地址和端口信息
//                        if (info2.getIsAlarmOn() == 0) {
//                            DatabaseManager.UpdateServerInfoState(info2);
//                        } else {
//                            DatabaseManager
//                                    .UpdateServerInfoStateWithAlarmState(info2);
//                        }
//                        bHasUpdate = true;
//
//                    }
                    if (deviceList.size() > 0) {

                        if (bNewDevFound) {
                            // add by luo 20150714
//                            LocalDefines.reloadDeviceInfoList();
//                            LocalDefines.isDeviceListSet = false;
//                            LocalDefines.nClientDeviceSettingThreadID++;
//                            new RegistClientWithDeviceArrayToServer(this,
//                                    LocalDefines.nClientDeviceSettingThreadID)
//                                    .start();
//
//                            LocalDefines.isAlibabaDeviceListSet = false;
//                            LocalDefines.nAlibabaClientDeviceSettingThreadID++;
//                            new AlibabaRegistClientWithDeviceArrayToServer(this, LocalDefines.nAlibabaClientDeviceSettingThreadID).start();
                            // end add by luo 20150714

                            // 成功搜索到设备
                            nTimeoutDetectID++;
                            if (soundPlayer != null) {
                                soundPlayer.stop();
                            }

                            lLayoutWifiInputPage.setVisibility(View.VISIBLE);
                            llayoutSLSearchingPage.setVisibility(View.GONE);
                            StopSearchDevice(); // 停止搜索设备
                            //Intent intent = new Intent(SmartLinkQuickWifiConfigActivity.this, MainActivity.class);
                            //startActivity(intent);

                            finish();

                        } else {
                            Toast toast_1 = Toast.makeText(
                                    APConfigActivity.this,
                                    getString(R.string.search_finish),
                                    Toast.LENGTH_SHORT);
                            toast_1.setGravity(Gravity.CENTER, 0, 0);
                            toast_1.show();

                            StartSearchDevice(); // 开始搜索设备
                        }
                    }
                    // 已存在

                } else if (msg.arg2 == DEVICE_IS_EXISTANCE) {
//                    if (info2.getIsAlarmOn() == 0) {
//                        DatabaseManager.UpdateServerInfoState(info2);
//                    } else {
//                        DatabaseManager
//                                .UpdateServerInfoStateWithAlarmState(info2);
//                    }

                }
//                else if (msg.arg2 == HttpUtils.RESULT_CODE_ERROR_IDENTITY) {
//                    httpResult401();
//                }
//                else if (msg.arg2 == HttpUtils.RESULT_CODE_SERVER_ERROR) {
//                    Toast.makeText(SmartLinkQuickWifiConfigActivity.this,
//                            getString(R.string.str_server_error),
//                            Toast.LENGTH_SHORT).show();
//                }
//                else if (msg.arg2 == HttpUtils.NEWERROR) {
//                    Toast.makeText(SmartLinkQuickWifiConfigActivity.this, R.string.Network_Error, Toast.LENGTH_SHORT).show();
//                }
                else {
                    Toast.makeText(APConfigActivity.this, R.string.str_bind_device_error, Toast.LENGTH_SHORT).show();
                }
            }

        }

    };

    // 信息提示框
    public void ShowAlert(String title, String msg) {

        if (hasWindowFocus()) {
            View view = View.inflate(this, R.layout.show_alert_dialog, null);
            TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
            tv_title.setText(title);
            TextView tv_content = (TextView) view.findViewById(R.id.tv_content);
            tv_content.setText(msg);
            new AlertDialog.Builder(this)
                    .setView(view)
                    .setPositiveButton(getString(R.string.alert_btn_OK), new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            setResult(android.app.Activity.RESULT_OK);
                        }

                    }).show();
        }
    }

    public void ShowAlert(String title, String msg, DialogInterface.OnClickListener listener) {

        if (hasWindowFocus()) {
            View view = View.inflate(this, R.layout.show_alert_dialog, null);
            TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
            tv_title.setText(title);
            TextView tv_content = (TextView) view.findViewById(R.id.tv_content);
            tv_content.setText(msg);
            new AlertDialog.Builder(this)
                    .setView(view)
                    .setPositiveButton(getString(R.string.alert_btn_OK), listener).show();
        }
    }

    // 停止设备搜索
    public void StopSearchDevice() {

        bIsSearching = false;
        m_nSearchID++;
        mConfigHotspotToWifiID++;
        mIsSearchingMode = false;
    }

    public void startBroadCastUdpThread() {
        m_nSearchID++;
        bIsSearching = true;
        new BroadCastUdp(m_nSearchID).start();
    }

    // 设备搜索线程
    public class BroadCastUdp extends Thread {

        private int nTreadSearchID = 0;

        public BroadCastUdp(int nSearchID) {
            nTreadSearchID = nSearchID;

        }

        public void run() {

            while (bIsSearching && nTreadSearchID == m_nSearchID) {

                deviceList = DeviceScanner.getDeviceListFromLan(nConfigID);

                if (deviceList != null && deviceList.size() > 0) {

                    Message msg = handler.obtainMessage();
                    msg.arg1 = LocalDefines.DEVICE_SEARCH_RESULT;
                    msg.arg2 = LocalDefines.DEVICE_SEARCH_RESULT_OK;
                    handler.sendMessage(msg);
                }

            }

        }

    }

    private static class ConfigHotspotToWifiThread extends Thread {
        private int mThreadID;
        private ScanResult mScanResult;
        private String mWifiPwd;
        private WeakReference<APConfigActivity> mWeakReference;

        public ConfigHotspotToWifiThread(int threadID, APConfigActivity apConfigActivity,
                                         /*ScanResult scanResult,*/ String wifiPwd) {
            mThreadID = threadID;
           // mScanResult = scanResult;
            mWifiPwd = wifiPwd;
            mWeakReference = new WeakReference<APConfigActivity>(apConfigActivity);
        }

        @Override
        public void run() {
            final APConfigActivity activity = mWeakReference.get();
            if (activity != null && activity.mConfigHotspotToWifiID == mThreadID) {
                if (activity.mDeviceInfo != null) {

                    LoginParam loginParam = new LoginParam();
                    loginParam.setDeviceInfo(activity.mDeviceInfo);
                    loginParam.setConnectType(Defines.LOGIN_FOR_SETTING);
                    Log.d(TAG, "run: start loginDevice");
                    LoginHelper.loginDevice(activity, loginParam, new ILoginDeviceCallback() {
                        @Override
                        public void onLogin(LoginHandle loginHandle) {
                            Log.d(TAG, "onLogin() called with: loginHandle = [" + loginHandle + "]");
                            if (loginHandle != null && loginHandle.getnResult() == ResultCode.RESULT_CODE_SUCCESS
                                    && activity.mConfigHotspotToWifiID == mThreadID) {


//                                if (mScanResult == null) {
//                                    return;
//                                }
                                String scanResultSSID = activity.strWiFiSSID;
//                                String scanResultCapabilities = mScanResult.capabilities;


                                // 设备配网
                                Log.d(TAG, "onLogin: start set network");
                                NetworkConfigInfo networkConfigInfo = DeviceNetworkSetting.setNetworkConfig(loginHandle,
                                        activity.mDeviceInfo, 1002, scanResultSSID, mWifiPwd);
                                Log.d(TAG, "onLogin: networkConfigInfo= [" + networkConfigInfo + ']' + activity.mConfigHotspotToWifiID + " " + mThreadID);
                                if (networkConfigInfo != null && networkConfigInfo.getnResult() == ResultCode.RESULT_CODE_SUCCESS
                                        && activity.mConfigHotspotToWifiID == mThreadID) {// 设备配网成功

                                    // 手机连接设备需要配网的WiFi
                                    activity.connectToSpecifiedWifi(scanResultSSID, mWifiPwd, 3);

                                    boolean connectWifiResult = false;

                                    // 获取当前手机连接的wifi是否为指定WiFi
                                    while (activity.mConfigHotspotToWifiID == mThreadID) {
                                        if (activity.mWiFiManager == null) break;
                                        WifiInfo wifiInfo = activity.mWiFiManager.getConnectionInfo();
                                        if (wifiInfo != null) {

                                            if (("\"" + scanResultSSID + "\"").equals(wifiInfo.getSSID())
                                                    && SupplicantState.COMPLETED.equals(wifiInfo.getSupplicantState())) {

                                                connectWifiResult = true;
                                                break;
                                            }
                                        }

                                        try {
                                            Thread.sleep(300);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    Log.d(TAG, "onLogin: start lan search");
                                    // 手机连接指定WiFi成功后进行局域网搜索设备
                                    if (connectWifiResult && activity.mConfigHotspotToWifiID == mThreadID) {

                                        boolean searchResult = false;
                                        DeviceInfo searchDevice = null;

                                        OUT:
                                        while (activity.mConfigHotspotToWifiID == mThreadID) {
                                            ArrayList<DeviceInfo> list = DeviceScanner.getDeviceListFromLan();
                                            Log.d(TAG, "onLogin: searchDeviceList = [" + list + "]");
                                            if (list != null && list.size() > 0) {
                                                for (DeviceInfo deviceInfo : list) {
                                                    Log.d(TAG, "onLogin: searchDevice = [" + deviceInfo + "]");
                                                    if (deviceInfo != null && activity.mDeviceInfo != null) {
                                                        if (deviceInfo.getnDevID() == activity.mDeviceInfo.getnDevID()) {
                                                            activity.deviceList = list;
                                                            searchResult = true;
                                                            searchDevice = deviceInfo;
                                                            break OUT;
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // 成功搜索到设备
                                        if (searchResult && activity.mConfigHotspotToWifiID == mThreadID) {

                                            Message msg = activity.handler.obtainMessage();
                                            msg.arg1 = LocalDefines.DEVICE_SEARCH_RESULT;
                                            msg.arg2 = LocalDefines.DEVICE_SEARCH_RESULT_OK;
                                            activity.handler.sendMessage(msg);
                                        } else {

                                            // 搜索超时
                                        }
                                    } else {
                                        // 密码错误或者直接连不上WiFi导致超时

                                    }
                                }
                            } else if (activity.mConfigHotspotToWifiID == mThreadID) {
                                // 设备登录失败

                            }
                        }
                    });
                }
            }
        }
    }
}