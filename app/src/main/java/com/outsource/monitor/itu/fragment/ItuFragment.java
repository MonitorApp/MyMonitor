package com.outsource.monitor.itu.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.outsource.monitor.activity.MonitorCenterActivity;
import com.outsource.monitor.base.Tab;
import com.outsource.monitor.config.PreferenceKey;
import com.outsource.monitor.fragment.BaseMonitorFragment;
import com.outsource.monitor.parser.Command;
import com.outsource.monitor.service.ConnectCallback;
import com.outsource.monitor.service.DataProviderService;
import com.outsource.monitor.itu.ItuDataReceiver;
import com.outsource.monitor.service.ServiceHelper;
import com.outsource.monitor.itu.event.ItuParamChangeEvent;
import com.outsource.monitor.itu.model.SingleFrequencyParam;
import com.outsource.monitor.utils.PreferenceUtils;
import com.outsource.monitor.utils.PromptUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by xionghao on 2016/12/17.
 */

public class ItuFragment extends BaseMonitorFragment {

    private ServiceHelper mServiceHelper;
    public SingleFrequencyParam mItuParam;

    @Override
    public Tab tab() {
        return Tab.ITU;
    }

    @Override
    public Fragment createContentFragment() {
        return ContentFragmentItu.newInstance();
    }

    @Override
    public Fragment createMenuFragment() {
        return MenuFragmentItu.newInstance();
    }

    public ItuFragment() {
        super();
    }

    public static ItuFragment newInstance() {
        return new ItuFragment();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mItuParam = SingleFrequencyParam.loadFromCache();
        initService();
        EventBus.getDefault().register(this);
    }

    private  void initService() {
        mServiceHelper = new ServiceHelper(getActivity());
        mServiceHelper.fetchService(new ServiceHelper.OnServiceConnectedListener() {
            @Override
            public void onServiceConnected(final DataProviderService.SocketBinder service) {
                if (getActivity() != null) {
                    service.addItuDataReceiver(((MonitorCenterActivity) getActivity()).getMapFragment());
                }
                service.addItuDataReceiver((ItuDataReceiver) mContentFragment);
                service.addItuDataReceiver((ItuDataReceiver) mMenuFragment);
                String ip = PreferenceUtils.getString(PreferenceKey.DEVICE_IP);
                int port = PreferenceUtils.getInt(PreferenceKey.DEVICE_PORT);
                service.connect(ip, port, new ConnectCallback() {
                    @Override
                    public void onConnectSuccess() {
                        Command command = new Command(mItuParam.getCommand(), Command.Type.ITU);
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
    public void onItuParamChanged(ItuParamChangeEvent event) {
        mServiceHelper.release();
        mItuParam = event.param;
        initService();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        mServiceHelper.release();
    }

}
