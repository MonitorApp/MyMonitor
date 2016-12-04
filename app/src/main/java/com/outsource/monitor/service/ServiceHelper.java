package com.outsource.monitor.service;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

/**
 * Created by Administrator on 2016/10/2.
 */
public class ServiceHelper {

    public interface OnServiceConnectListener {
        void onServiceConnected(DataProviderService.SocketBinder service);
    }

    private OnServiceConnectListener mListener;
    private DataProviderService.SocketBinder mService;
    private SoftReference<Activity> mActivity = new SoftReference<Activity>(null);

    public void setOnServiceConnectListener(OnServiceConnectListener listener) {
        mListener = listener;
    }

    public void bindService(Activity activity) {
        Intent intent = new Intent(activity, DataProviderService.class);
        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mService = (DataProviderService.SocketBinder) service;
                if (mListener != null) {
                    mListener.onServiceConnected(mService);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        activity.bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);
        mActivity = new SoftReference<Activity>(activity);
    }

    public DataProviderService.SocketBinder getService() {
        return mService;
    }

    public void fetchService(final OnServiceConnectListener listener) {
        if (listener == null) return;
        if (mService == null) {
            Activity activity = mActivity.get();
            if (activity != null) {
                ServiceConnection serviceConnection = new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName name, IBinder service) {
                        mService = (DataProviderService.SocketBinder) service;
                        listener.onServiceConnected(mService);
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name) {

                    }
                };
                activity.bindService(new Intent(activity, DataProviderService.class), serviceConnection, Service.BIND_AUTO_CREATE);
            }
        } else {
            listener.onServiceConnected(mService);
        }
    }
}
