package com.outsource.monitor.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.outsource.monitor.R;
import com.outsource.monitor.base.BaseActivity;
import com.outsource.monitor.config.PreferenceKey;
import com.outsource.monitor.utils.InputMethodUtils;
import com.outsource.monitor.utils.PreferenceUtils;
import com.outsource.monitor.utils.PromptUtils;

public class OptionActivity extends BaseActivity {

    private EditText mEtIp;
    private EditText mEtPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);
        initToolbar();
        initView();
        InputMethodUtils.detectInputMethodEvent(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, Menu.FIRST, Menu.FIRST, "保存");
        menu.getItem(0).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == Menu.FIRST) {
            saveSetting();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("设置");
        toolbar.setSubtitle("");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initView() {
        mEtIp = (EditText) findViewById(R.id.et_setting_device_ip);
        mEtPort = (EditText) findViewById(R.id.et_setting_device_port);
        mEtIp.setText(PreferenceUtils.getString(PreferenceKey.DEVICE_IP));
        int port = PreferenceUtils.getInt(PreferenceKey.DEVICE_PORT);
        if (port > 0) {
            mEtPort.setText(Integer.toString(port));
        }
    }

    @Override
    public void onBackPressed() {
        if (InputMethodUtils.isInputMethodShowing()) {
            super.onBackPressed();
        } else {
            if (isInputChanged()) {
                new AlertDialog.Builder(this)
                        .setMessage("设置已经修改，是否保存？")
                        .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                saveSetting();
                            }
                        })
                        .setNegativeButton("不保存", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .create()
                        .show();
            }
        }
    }

    private boolean isInputChanged() {
        String ip = getInputIp();
        String port = getInputPort();
        String oldIp = PreferenceUtils.getString(PreferenceKey.DEVICE_IP);
        String oldPort = Integer.toString(PreferenceUtils.getInt(PreferenceKey.DEVICE_PORT));
        return !ip.equals(oldIp) || !port.equals(oldPort);
    }

    private void saveSetting() {
        if (checkIp() && checkPort()) {
            PreferenceUtils.putString(PreferenceKey.DEVICE_IP, getInputIp());
            PreferenceUtils.putInt(PreferenceKey.DEVICE_PORT, Integer.valueOf(getInputPort()));
            PromptUtils.showToast("保存成功");
            finish();
        }

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
}
