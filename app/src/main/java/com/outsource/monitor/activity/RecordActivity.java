package com.outsource.monitor.activity;

import android.content.DialogInterface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.outsource.monitor.R;
import com.outsource.monitor.adapter.RecordAdapter;
import com.outsource.monitor.base.BaseActivity;

public class RecordActivity extends BaseActivity {

    private static String TAG = "RecordActivity";

    private RecordAdapter mAdtRec;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        initViews();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview_record);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdtRec = new RecordAdapter(this);
        recyclerView.setAdapter(mAdtRec);
    }

    private void initViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("记录文件");
        toolbar.setSubtitle("");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_record_delete, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.action_menu_delete:

                new AlertDialog.Builder(this).setTitle("确认删除选中记录吗？")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 点击“确认”后的操作
                                Log.d(TAG, "delete ok");
                                mAdtRec.doDeleteSelectItem();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 点击“返回”后的操作,这里不设置没有任何操作
                                Log.d(TAG, "delete cancel");
                            }
                        }).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
