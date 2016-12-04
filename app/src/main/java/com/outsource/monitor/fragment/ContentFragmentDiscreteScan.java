package com.outsource.monitor.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.outsource.monitor.R;
import com.outsource.monitor.activity.TemplateActivity;
import com.outsource.monitor.adapter.BandScanningTagAdapter;
import com.outsource.monitor.model.FrequencyLevel;
import com.outsource.monitor.model.TagLevel;
import com.outsource.monitor.service.DataProviderService;
import com.outsource.monitor.service.ServiceHelper;
import com.outsource.monitor.widget.BandScanningTextureView;
import com.outsource.monitor.widget.FallsLevelView;

/**
 * Created by Administrator on 2016/10/2.
 */
public class ContentFragmentDiscreteScan extends Fragment {

    private BandScanningTagAdapter mTagAdapter;
    private BandScanningTextureView mLevelTextureView;
    private FallsLevelView mFallsLevelView;
    private Button mBtnPlay;

    public static Fragment newInstance() {
        return new ContentFragmentDiscreteScan();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_discrete_scan, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv_discrete_scan_tag);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mTagAdapter = new BandScanningTagAdapter();
        recyclerView.setAdapter(mTagAdapter);
        view.findViewById(R.id.iv_discrete_scan_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TagLevel tag = mLevelTextureView.addTag();
                if (tag != null) {
                    mTagAdapter.addData(new FrequencyLevel(424.9f, tag.level));
                }
            }
        });
        view.findViewById(R.id.iv_discrete_scan_del).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLevelTextureView.deleteTag();
                mTagAdapter.deleteLastData();
            }
        });
        mLevelTextureView = (BandScanningTextureView) view.findViewById(R.id.sv_discrete_scan_level);
        mFallsLevelView = (FallsLevelView) view.findViewById(R.id.falls_view_discrete_scan);
        mBtnPlay = (Button) view.findViewById(R.id.btn_discrete_scan_start_or_pause);
        mBtnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLevelTextureView.isRunning()) {
                    mLevelTextureView.pause();
                    mFallsLevelView.pause();
                    mBtnPlay.setText("开始");
                } else {
                    if (mLevelTextureView.isPaused()) {
                        mLevelTextureView.resume();
                        mFallsLevelView.resume();
                    } else {
                        mLevelTextureView.start();
                        mFallsLevelView.start();
                    }
                    mBtnPlay.setText("暂停");
                }
            }
        });

        mLevelTextureView.start();
        mFallsLevelView.start();

        ServiceHelper helper = new ServiceHelper();
        DataProviderService.SocketBinder service = helper.getService();
        if (service == null) {
            helper.setOnServiceConnectListener(new ServiceHelper.OnServiceConnectListener() {
                @Override
                public void onServiceConnected(DataProviderService.SocketBinder service) {
                    service.addDataReceiver(mLevelTextureView);
                    service.addDataReceiver(mFallsLevelView);
                }
            });
        } else {
            service.addDataReceiver(mLevelTextureView);
            service.addDataReceiver(mFallsLevelView);
        }
        return view;
    }
}
