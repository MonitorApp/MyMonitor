package com.outsource.monitor.df.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.outsource.monitor.activity.MonitorCenterActivity;
import com.outsource.monitor.base.Tab;
import com.outsource.monitor.config.PreferenceKey;
import com.outsource.monitor.df.event.DfParamChangeEvent;
import com.outsource.monitor.df.model.DfParam;
import com.outsource.monitor.fragment.BaseMonitorFragment;
import com.outsource.monitor.parser.Command;
import com.outsource.monitor.service.ConnectCallback;
import com.outsource.monitor.service.DataProviderService;
import com.outsource.monitor.df.DfDataReceiver;
import com.outsource.monitor.service.ServiceHelper;
import com.outsource.monitor.utils.PreferenceUtils;
import com.outsource.monitor.utils.PromptUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by xionghao on 2016/12/17.
 */

public class DfFragment extends BaseMonitorFragment {

    private ServiceHelper mServiceHelper;
    public DfParam mParam;

    @Override
    public Fragment createContentFragment() {
        return ContentFragmentDf.newInstance();
    }

    @Override
    public Fragment createMenuFragment() {
        return MenuFragmentDf.newInstance();
    }

    @Override
    public Tab tab() {
        return Tab.IFPAN;
    }

    public DfFragment() {
        super();
    }

    public static DfFragment newInstance() {
        return new DfFragment();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mParam = DfParam.loadFromCache();
        initService();
        EventBus.getDefault().register(this);
    }

    private void initService() {
        mServiceHelper = new ServiceHelper(getActivity());
        mServiceHelper.fetchService(new ServiceHelper.OnServiceConnectedListener() {
            @Override
            public void onServiceConnected(final DataProviderService.SocketBinder service) {
                if (getActivity() != null) {
                    service.addDfDataReceiver(((MonitorCenterActivity) getActivity()).getMapFragment());
                }
                service.addDfDataReceiver((DfDataReceiver) mContentFragment);
                service.addDfDataReceiver((DfDataReceiver) mMenuFragment);
                String ip = PreferenceUtils.getString(PreferenceKey.DEVICE_IP);
                int port = PreferenceUtils.getInt(PreferenceKey.DEVICE_PORT);
                service.connect(ip, port, new ConnectCallback() {
                    @Override
                    public void onConnectSuccess() {
                        String format = "RMTP:DF:4403000100113:frequency:%fMHz\nifbw:30kHz\nspan:15kHz\nrecordthreshold:=40\ndemodmode:FM\n#";
                        String cmd = String.format(format, mParam.frequency);
                        Command command = new Command(cmd, Command.Type.DF);
                        service.sendCommand(command);
                    }

                    @Override
                    public void onConnectFail(String message) {
                        PromptUtils.showToast(message);
                    }
                });
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFscanParamChanged(DfParamChangeEvent event) {
        mServiceHelper.release();
        mParam = event.param;
        initService();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mServiceHelper.release();
        EventBus.getDefault().unregister(this);
    }

}
