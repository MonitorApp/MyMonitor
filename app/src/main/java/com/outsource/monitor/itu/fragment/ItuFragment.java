package com.outsource.monitor.itu.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.View;

import com.outsource.monitor.activity.MonitorCenterActivity;
import com.outsource.monitor.base.Tab;
import com.outsource.monitor.config.PreferenceKey;
import com.outsource.monitor.event.PlayBallStateEvent;
import com.outsource.monitor.event.PlayPauseEvent;
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
    public SingleFrequencyParam mItuParam = SingleFrequencyParam.loadFromCache();

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
        mServiceHelper = new ServiceHelper(getActivity());
        if (((MonitorCenterActivity) getActivity()).isPlaying()) {
            if (mItuParam.frequecy == 0 || mItuParam.ifbw == 0 || mItuParam.span == 0) {
                EventBus.getDefault().post(new PlayBallStateEvent(false));
            } else {
                sendCommand();
            }
        }
        EventBus.getDefault().register(this);
    }

    private  void sendCommand() {
        mServiceHelper.fetchService(new ServiceHelper.OnServiceConnectedListener() {
            @Override
            public void onServiceConnected(final DataProviderService.SocketBinder service) {
                if (getActivity() != null) {
                    service.addItuDataReceiver(((MonitorCenterActivity) getActivity()).getMapFragment());
                }
                service.addItuDataReceiver((ItuDataReceiver) mContentFragment);
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
        mDrawerLayout.closeDrawer(Gravity.RIGHT);
        mItuParam = event.param;
        if (((MonitorCenterActivity) getActivity()).isPlaying()) {
            disconnect();
            sendCommand();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayPauseEvent(PlayPauseEvent playPauseEvent) {
        if (playPauseEvent.isPlay) {
            if (mItuParam == null) {
                PromptUtils.showToast("请先设置有效的单频测量参数再开始");
                EventBus.getDefault().post(new PlayBallStateEvent(false));
            } else {
                sendCommand();
            }
        } else {
            disconnect();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        mServiceHelper.release();
    }

    private void disconnect() {
        mServiceHelper.fetchService(new ServiceHelper.OnServiceConnectedListener() {
            @Override
            public void onServiceConnected(DataProviderService.SocketBinder service) {
                if (service != null) {
                    service.disconnect();
                }
            }
        });
    }

}
