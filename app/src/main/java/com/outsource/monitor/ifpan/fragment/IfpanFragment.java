package com.outsource.monitor.ifpan.fragment;

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
import com.outsource.monitor.ifpan.model.IfpanParam;
import com.outsource.monitor.ifpan.event.IfpanParamsChangeEvent;
import com.outsource.monitor.parser.Command;
import com.outsource.monitor.service.ConnectCallback;
import com.outsource.monitor.service.DataProviderService;
import com.outsource.monitor.ifpan.IfpanDataReceiver;
import com.outsource.monitor.service.ServiceHelper;
import com.outsource.monitor.utils.LogUtils;
import com.outsource.monitor.utils.PreferenceUtils;
import com.outsource.monitor.utils.PromptUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by xionghao on 2016/12/17.
 */

public class IfpanFragment extends BaseMonitorFragment {

    private ServiceHelper mServiceHelper;
    public IfpanParam mIfpanParam = IfpanParam.loadFromCache();

    @Override
    public Fragment createContentFragment() {
        return ContentFragmentIfpan.newInstance();
    }

    @Override
    public Fragment createMenuFragment() {
        return MenuFragmentIfpan.newInstance();
    }

    @Override
    public Tab tab() {
        return Tab.IFPAN;
    }

    public IfpanFragment() {
        super();
    }

    public static IfpanFragment newInstance() {
        return new IfpanFragment();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mServiceHelper = new ServiceHelper(getActivity());
        if (((MonitorCenterActivity) getActivity()).isPlaying()) {
            if (mIfpanParam.frequency == 0 || mIfpanParam.band == 0 || mIfpanParam.span == 0) {
                EventBus.getDefault().post(new PlayBallStateEvent(false));
            } else {
                sendCommand();
            }
        }
        EventBus.getDefault().register(this);
    }

    private void sendCommand() {
        mServiceHelper.fetchService(new ServiceHelper.OnServiceConnectedListener() {
            @Override
            public void onServiceConnected(final DataProviderService.SocketBinder service) {
                if (getActivity() != null) {
                    service.addIfpanDataReceiver(((MonitorCenterActivity) getActivity()).getMapFragment());
                }
                service.addIfpanDataReceiver((IfpanDataReceiver) mContentFragment);
                service.addIfpanDataReceiver((IfpanDataReceiver) mMenuFragment);
                String ip = PreferenceUtils.getString(PreferenceKey.DEVICE_IP);
                int port = PreferenceUtils.getInt(PreferenceKey.DEVICE_PORT);
                service.connect(ip, port, new ConnectCallback() {
                    @Override
                    public void onConnectSuccess() {
                        String format = "RMTP:IFANALYSIS:4403000100113:frequency:%.1fMHz\nifbw:%dkHz\nspan:%dkHz\nrecordthreshold:=40\ndemodmode:FM\n#";
                        String cmd = String.format(format, mIfpanParam.frequency, mIfpanParam.band, mIfpanParam.span);
                        Command command = new Command(cmd, Command.Type.IFPAN);
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
    public void onIfpanParamChanged(IfpanParamsChangeEvent event) {
        mDrawerLayout.closeDrawer(Gravity.RIGHT);
        mIfpanParam = event.param;
        if (((MonitorCenterActivity) getActivity()).isPlaying()) {
            disconnect();
            sendCommand();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayPauseEvent(PlayPauseEvent event) {
        if (event.isPlay) {
            if (mIfpanParam.frequency == 0 || mIfpanParam.band == 0 || mIfpanParam.span == 0) {
                PromptUtils.showToast("请先设置有效的中频分析参数再开始");
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
        mServiceHelper.release();
        EventBus.getDefault().unregister(this);
        LogUtils.e("onDestroyView " + this);
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
