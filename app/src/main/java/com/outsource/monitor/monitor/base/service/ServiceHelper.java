package com.outsource.monitor.monitor.base.service;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.lang.ref.SoftReference;

/**
 * Created by Administrator on 2016/10/2.
 */
public class ServiceHelper {

    public interface OnServiceConnectedListener {
        void onServiceConnected(DataProviderService.SocketBinder service);
    }

    private DataProviderService.SocketBinder mService;
    private SoftReference<Activity> mActivitySoftReference;
    private OnServiceConnectedListener mOnServiceConnectedListener;

    public ServiceHelper(Activity activity) {
        mActivitySoftReference = new SoftReference<>(activity);
    }

    private void bindService(Activity activity) {
        activity.bindService(new Intent(activity, DataProviderService.class), mServiceConnection, Service.BIND_AUTO_CREATE);
    }

    //在Activity的onDestroy方法里调用
    public void release() {
        if (mActivitySoftReference != null && mActivitySoftReference.get() != null && mService != null) {
            mService.disconnect();
            mActivitySoftReference.get().unbindService(mServiceConnection);
            mActivitySoftReference.clear();
            mService = null;
        }
        mActivitySoftReference = null;
    }

    public void fetchService(final OnServiceConnectedListener listener) {
        Activity activity = mActivitySoftReference.get();
        if (activity == null || activity.isFinishing()) return;
        DataProviderService.SocketBinder service = getService();
        if (service == null) {
            setOnServiceConnectedListener(new OnServiceConnectedListener() {
                @Override
                public void onServiceConnected(DataProviderService.SocketBinder service) {
                    if (listener != null) {
                        listener.onServiceConnected(service);
                    }
                }
            });
            bindService(activity);
        } else {
            listener.onServiceConnected(service);
        }
    }

    public DataProviderService.SocketBinder getService() {
        return mService;
    }

    public void setOnServiceConnectedListener(OnServiceConnectedListener listener) {
        mOnServiceConnectedListener = listener;
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = (DataProviderService.SocketBinder) service;
            if (mOnServiceConnectedListener != null) {
                mOnServiceConnectedListener.onServiceConnected(mService);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
}
