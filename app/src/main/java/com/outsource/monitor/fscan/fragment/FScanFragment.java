package com.outsource.monitor.fscan.fragment;

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
import com.outsource.monitor.fscan.event.FscanParamsChangeEvent;
import com.outsource.monitor.fscan.model.FscanParam;
import com.outsource.monitor.parser.Command;
import com.outsource.monitor.service.ConnectCallback;
import com.outsource.monitor.service.DataProviderService;
import com.outsource.monitor.fscan.FscanDataReceiver;
import com.outsource.monitor.service.ServiceHelper;
import com.outsource.monitor.utils.PreferenceUtils;
import com.outsource.monitor.utils.PromptUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by xionghao on 2016/12/17.
 */

public class FScanFragment extends BaseMonitorFragment {

    private ServiceHelper mServiceHelper;
    public FscanParam mParam = FscanParam.loadFromCache();

    @Override
    public Fragment createContentFragment() {
        return ContentFragmentFscan.newInstance();
    }

    @Override
    public Fragment createMenuFragment() {
        return MenuFragmentFscan.newInstance();
    }

    @Override
    public Tab tab() {
        return Tab.IFPAN;
    }

    public FScanFragment() {
        super();
    }

    public static FScanFragment newInstance() {
        return new FScanFragment();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mServiceHelper = new ServiceHelper(getActivity());
        if (((MonitorCenterActivity) getActivity()).isPlaying()) {
            if (mParam.startFrequency == 0 || mParam.endFrequency == 0 || mParam.step == 0) {
                EventBus.getDefault().post(new PlayBallStateEvent(false));
            } else {
                startService();
            }
        }
        EventBus.getDefault().register(this);
    }

    private void startService() {
        mServiceHelper.fetchService(new ServiceHelper.OnServiceConnectedListener() {
            @Override
            public void onServiceConnected(final DataProviderService.SocketBinder service) {
                if (getActivity() != null) {
                    service.addFscanDataReceiver(((MonitorCenterActivity) getActivity()).getMapFragment());
                }
                service.addFscanDataReceiver((FscanDataReceiver) mContentFragment);
                service.addFscanDataReceiver((FscanDataReceiver) mMenuFragment);
                String ip = PreferenceUtils.getString(PreferenceKey.DEVICE_IP);
                int port = PreferenceUtils.getInt(PreferenceKey.DEVICE_PORT);
                service.connect(ip, port, new ConnectCallback() {
                    @Override
                    public void onConnectSuccess() {
                        String cmd = "\"RMTP:FSCAN:4403000100113:frequency:98.1MHz\\nifbw:30kHz\\nspan:15kHz\\nrecordthreshold:=40\\ndemodmode:FM\\n#\";";
//                        String cmd = String.format(format, mIfpanParam.frequency, mIfpanParam.band, mIfpanParam.span);
                        Command command = new Command(cmd, Command.Type.FSCAN);
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
    public void onFscanParamChanged(FscanParamsChangeEvent event) {
        mDrawerLayout.closeDrawer(Gravity.RIGHT);
        mParam = event.param;
        if (((MonitorCenterActivity) getActivity()).isPlaying()) {
            mServiceHelper.release();
            startService();
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayPauseEvent(PlayPauseEvent playPauseEvent) {
        if (playPauseEvent.isPlay) {
            if (mParam.startFrequency == 0 || mParam.endFrequency == 0 || mParam.step == 0) {
                PromptUtils.showToast("请先设置有效的参数再开始");
                EventBus.getDefault().post(new PlayBallStateEvent(false));
            } else {
                startService();
            }
        } else {
            mServiceHelper.release();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mServiceHelper.release();
        EventBus.getDefault().unregister(this);
    }

}
