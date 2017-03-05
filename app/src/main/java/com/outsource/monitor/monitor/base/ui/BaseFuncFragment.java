package com.outsource.monitor.monitor.base.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.View;

import com.outsource.monitor.base.ParamChangeEvent;
import com.outsource.monitor.base.ParamSettingFragment;
import com.outsource.monitor.config.ConfigManager;
import com.outsource.monitor.config.PreferenceKey;
import com.outsource.monitor.monitor.df.DfDataReceiver;
import com.outsource.monitor.monitor.digit.DigitDataReceiver;
import com.outsource.monitor.monitor.discrete.DiscreteDataReceiver;
import com.outsource.monitor.monitor.base.event.UpdatePlayUIEvent;
import com.outsource.monitor.monitor.base.event.PlayPauseEvent;
import com.outsource.monitor.monitor.fscan.FscanDataReceiver;
import com.outsource.monitor.monitor.ifpan.IfpanDataReceiver;
import com.outsource.monitor.monitor.itu.ItuDataReceiver;
import com.outsource.monitor.monitor.base.parser.Command;
import com.outsource.monitor.monitor.base.service.ConnectCallback;
import com.outsource.monitor.monitor.base.service.DataProviderService;
import com.outsource.monitor.monitor.base.service.ServiceHelper;
import com.outsource.monitor.utils.PreferenceUtils;
import com.outsource.monitor.utils.PromptUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by xionghao on 2017/3/5.
 */

public abstract class BaseFuncFragment extends BaseMonitorFragment {

    private ServiceHelper mServiceHelper;

    @Override
    public Fragment createMenuFragment() {
        return ParamSettingFragment.newInstance(getFuncType());
    }

    public BaseFuncFragment() {
        super();
    }

    private ConfigManager.FuncType getFuncType() {
        switch (tab()) {
            case IFPAN:
                return ConfigManager.FuncType.IFPAN;
            case FSCAN:
                return ConfigManager.FuncType.FSCAN;
            case DF:
                return ConfigManager.FuncType.DF;
            case DISCRETE:
                return ConfigManager.FuncType.DISCRETE;
            case DIGIT:
                return ConfigManager.FuncType.DIGIT;
        }
        return ConfigManager.FuncType.ITU;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mServiceHelper = new ServiceHelper(getActivity());
        if (((MonitorCenterActivity) getActivity()).isPlaying()) {
            if (!ConfigManager.getInstance().isParamsValid(getFuncType())) {
                EventBus.getDefault().post(new UpdatePlayUIEvent(false));
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
                switch (tab()) {
                    case IFPAN:
                        if (getActivity() != null) {
                            service.addIfpanDataReceiver(((MonitorCenterActivity) getActivity()).getMapFragment());
                        }
                        service.addIfpanDataReceiver((IfpanDataReceiver) mContentFragment);
                        break;
                    case FSCAN:
                        if (getActivity() != null) {
                            service.addFscanDataReceiver(((MonitorCenterActivity) getActivity()).getMapFragment());
                        }
                        service.addFscanDataReceiver((FscanDataReceiver) mContentFragment);
                        break;
                    case DF:
                        if (getActivity() != null) {
                            service.addDfDataReceiver(((MonitorCenterActivity) getActivity()).getMapFragment());
                        }
                        service.addDfDataReceiver((DfDataReceiver) mContentFragment);
                        break;
                    case DISCRETE:
                        if (getActivity() != null) {
                            service.addDiscreteDataReceiver(((MonitorCenterActivity) getActivity()).getMapFragment());
                        }
                        service.addDiscreteDataReceiver((DiscreteDataReceiver) mContentFragment);
                        break;
                    case DIGIT:
                        if (getActivity() != null) {
                            service.addDigitDataReceiver(((MonitorCenterActivity) getActivity()).getMapFragment());
                        }
                        service.addDigitDataReceiver((DigitDataReceiver) mContentFragment);
                        break;
                    default:
                        if (getActivity() != null) {
                            service.addItuDataReceiver(((MonitorCenterActivity) getActivity()).getMapFragment());
                        }
                        service.addItuDataReceiver((ItuDataReceiver) mContentFragment);
                        break;
                }
                String ip = PreferenceUtils.getString(PreferenceKey.DEVICE_IP);
                int port = PreferenceUtils.getInt(PreferenceKey.DEVICE_PORT);
                service.connect(ip, port, new ConnectCallback() {
                    @Override
                    public void onConnectSuccess() {
                        Command command = ConfigManager.getInstance().getCommand(getFuncType());
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
    public void onParamChanged(ParamChangeEvent event) {
        if (event.funcType == getFuncType()) {
            mDrawerLayout.closeDrawer(Gravity.RIGHT);
            if (((MonitorCenterActivity) getActivity()).isPlaying()) {
                disconnect();
                sendCommand();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayPauseEvent(PlayPauseEvent playPauseEvent) {
        if (playPauseEvent.isPlay) {
            if (!ConfigManager.getInstance().isParamsValid(getFuncType())) {
                PromptUtils.showToast("请先设置有效的参数再开始");
                EventBus.getDefault().post(new UpdatePlayUIEvent(false));
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
