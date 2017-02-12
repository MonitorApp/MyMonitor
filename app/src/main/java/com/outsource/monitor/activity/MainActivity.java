package com.outsource.monitor.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.outsource.monitor.R;
import com.outsource.monitor.base.Tab;

import java.util.ArrayList;

public class MainActivity extends FragmentActivity implements View.OnClickListener{

    private String permissionInfo;
    private final int SDK_PERMISSION_REQUEST = 127;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitComponent();
        getPersimmions();
    }

    private void InitComponent(){
        findViewById(R.id.tv_sgl_scan).setOnClickListener(this); //单频扫描
        findViewById(R.id.tv_midddle_analyse).setOnClickListener(this);
        findViewById(R.id.tv_band_scan).setOnClickListener(this);
        findViewById(R.id.tv_df).setOnClickListener(this);
        findViewById(R.id.tv_discrete_scan).setOnClickListener(this);
        findViewById(R.id.tv_digit_scan).setOnClickListener(this);
        findViewById(R.id.tv_map).setOnClickListener(this);
        findViewById(R.id.tv_record).setOnClickListener(this);
        findViewById(R.id.tv_option).setOnClickListener(this);
        findViewById(R.id.tv_about).setOnClickListener(this);
        findViewById(R.id.tv_location).setOnClickListener(this);
    }

    @TargetApi(23)
    private void getPersimmions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissions = new ArrayList<String>();
            /***
             * 定位权限为必须权限，用户如果禁止，则每次进入都会申请
             */
            // 定位精确位置
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
			/*
			 * 读写权限和电话状态权限非必要权限(建议授予)只会申请一次，用户同意或者禁止，只会弹一次
			 */
            // 读写权限
            if (addPermission(permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissionInfo += "Manifest.permission.WRITE_EXTERNAL_STORAGE Deny \n";
            }
            // 读取电话状态权限
            if (addPermission(permissions, Manifest.permission.READ_PHONE_STATE)) {
                permissionInfo += "Manifest.permission.READ_PHONE_STATE Deny \n";
            }

            if (permissions.size() > 0) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), SDK_PERMISSION_REQUEST);
            }
        }
    }

    @TargetApi(23)
    private boolean addPermission(ArrayList<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) { // 如果应用没有获得对应权限,则添加到列表中,准备批量申请
            if (shouldShowRequestPermissionRationale(permission)){
                return true;
            }else{
                permissionsList.add(permission);
                return false;
            }

        }else{
            return true;
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // TODO Auto-generated method stub
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(MainActivity.this, MonitorCenterActivity.class);
        switch (v.getId())
        {
            case R.id.tv_sgl_scan:
                intent.putExtra(MonitorCenterActivity.TAB, Tab.ITU);
                startActivity(intent);
                break;
            case R.id.tv_midddle_analyse:
                intent.putExtra(MonitorCenterActivity.TAB, Tab.IFPAN);
                startActivity(intent);
                break;
            case R.id.tv_band_scan:
                intent.putExtra(MonitorCenterActivity.TAB, Tab.BAND_SCAN);
                startActivity(intent);
                break;
            case R.id.tv_df:
                intent.putExtra(MonitorCenterActivity.TAB, Tab.DF);
                startActivity(intent);
                break;
            case R.id.tv_discrete_scan:
                intent.putExtra(MonitorCenterActivity.TAB, Tab.DISCRETE_SCAN);
                startActivity(intent);
                break;
            case R.id.tv_digit_scan:
                intent.putExtra(MonitorCenterActivity.TAB, Tab.DIGIT_SCAN);
                startActivity(intent);
                break;
            case R.id.tv_map:
                startActivity(new Intent(MainActivity.this, MapActivity.class));
                break;
            case R.id.tv_record:
                startActivity(new Intent(MainActivity.this, RecordActivity.class));
                break;
            case R.id.tv_option:
                startActivity(new Intent(MainActivity.this, OptionActivity.class));
                break;
            case R.id.tv_about:
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                break;
            case R.id.tv_location:
                startActivity(new Intent(MainActivity.this, LocationActivity.class));
                break;
        }
    }

}
