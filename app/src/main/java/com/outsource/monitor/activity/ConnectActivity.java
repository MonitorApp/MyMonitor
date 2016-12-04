package com.outsource.monitor.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.outsource.monitor.R;
import com.outsource.monitor.config.PreferenceKey;
import com.outsource.monitor.service.ConnectCallback;
import com.outsource.monitor.service.DataProviderService;
import com.outsource.monitor.service.ServiceHelper;
import com.outsource.monitor.utils.EnvironmentUtils;
import com.outsource.monitor.utils.PreferenceUtils;
import com.outsource.monitor.utils.PromptUtils;
import com.outsource.monitor.utils.Utils;

public class ConnectActivity extends AppCompatActivity {

    public static final String DEVICE_IP = "DEVICE_IP";
    public static final String DEVICE_PORT = "DEVICE_PORT";
    private TextView mTvIp;
    private TextView mTvSpace;
    private TextView mTvTime;
    private EditText mEtIp;
    private EditText mEtPort;
    private View mLLConnectInput;
    private Button mBtnConnect;
    private ProgressBar mPbConnecting;

    private ServiceHelper mServiceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        mTvIp = (TextView) findViewById(R.id.tv_ip);
        mTvSpace = (TextView) findViewById(R.id.tv_space);
        mTvTime = (TextView) findViewById(R.id.tv_time);

        fillInitInfo();

        mLLConnectInput = findViewById(R.id.ll_connect_input);
        mEtIp = (EditText) findViewById(R.id.et_input_device_ip);
        mEtPort = (EditText) findViewById(R.id.et_input_device_port);
        mEtIp.setText(PreferenceUtils.getString(PreferenceKey.DEVICE_IP));
        int port = PreferenceUtils.getInt(PreferenceKey.DEVICE_PORT);
        if (port > 0) {
            mEtPort.setText(Integer.toString(port));
        }
        mPbConnecting = (ProgressBar) findViewById(R.id.pb_connecting);
        mLLConnectInput = findViewById(R.id.ll_connect_input);
        mBtnConnect = (Button) findViewById(R.id.btn_connect);
        mBtnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!EnvironmentUtils.Network.isNetWorkAvailable()) {
                    PromptUtils.showToast(getString(R.string.network_off));
                    return;
                }
                if (checkIp() && checkPort()) {
                    PreferenceUtils.putString(PreferenceKey.DEVICE_IP, getInputIp());
                    PreferenceUtils.putInt(PreferenceKey.DEVICE_PORT, Integer.valueOf(getInputPort()));
                    mPbConnecting.setVisibility(View.VISIBLE);
                    mLLConnectInput.setVisibility(View.INVISIBLE);
                    mServiceHelper.fetchService(new ServiceHelper.OnServiceConnectListener() {
                        @Override
                        public void onServiceConnected(DataProviderService.SocketBinder service) {
                            String ip = getInputIp();
                            String port = getInputPort();
                            service.connect(ip, Integer.valueOf(port), new ConnectCallback() {
                                @Override
                                public void onConnectSuccess() {
                                    if (isFinishing()) return;
                                    PromptUtils.showToast("设备连接成功！");
                                    startActivity(new Intent(ConnectActivity.this, MainActivity.class));
                                    finish();
                                }

                                @Override
                                public void onConnectFail(String message) {
                                    if (isFinishing()) return;
                                    PromptUtils.showToast(message);
                                    mLLConnectInput.setVisibility(View.VISIBLE);
                                    mPbConnecting.setVisibility(View.INVISIBLE);
                                }
                            });
                        }
                    });
                }
            }
        });
        mServiceHelper = new ServiceHelper();
        mServiceHelper.bindService(this);
    }

    private boolean checkIp() {
        boolean isEmpty = TextUtils.isEmpty(getInputIp());
        if (isEmpty) {
            PromptUtils.showToast("请输入设备ip地址");
        }
        return !isEmpty;
    }

    private boolean checkPort() {
        boolean isEmpty = TextUtils.isEmpty(getInputPort());
        if (isEmpty) {
            PromptUtils.showToast("请输入设备端口号");
        }
        return !isEmpty;
    }

    private String getInputIp() {
        return mEtIp.getText().toString();
    }

    private String getInputPort() {
        return mEtPort.getText().toString();
    }

    private void fillInitInfo() {
        String myIpDesc = "本机IP:";
        SpannableString sstrIp = new SpannableString(myIpDesc + EnvironmentUtils.Network.ipv4());
        sstrIp.setSpan(new UnderlineSpan(), myIpDesc.length(), sstrIp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTvIp.setText(sstrIp);

        String mySpaceDesc = "剩余空间：";
        long memSizeLeft = Utils.getAvailableExternalMemorySize();
        String strSpace = Utils.formatFileSize(memSizeLeft, false);
        SpannableString builder = new SpannableString(mySpaceDesc + strSpace);
        builder.setSpan(new UnderlineSpan(), mySpaceDesc.length(), builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTvSpace.setText(builder);
    }
}
