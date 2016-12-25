package com.outsource.monitor.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.outsource.monitor.parser.Command;

/**
 * Created by Administrator on 2016/10/2.
 */
public class DataProviderService extends Service {

    private SocketBinder mBinder;
    private boolean isStart;

    @Override
    public void onCreate() {
        super.onCreate();
        mBinder = new SocketBinder();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (!isStart) {
            try {
                startService(new Intent(this, DataProviderService.class));
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
        return mBinder;
    }

    //每次调用startService此方法都会执行
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return Service.START_STICKY_COMPATIBILITY;
        isStart = true;
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isStart = false;
    }

    public class SocketBinder extends Binder {

        private SocketThread mThread;

        public SocketBinder() {
            mThread = new SocketThread();
            mThread.start();
        }

        public void addDataReceiver(DataReceiver receiver) {
            mThread.addDataReceiver(receiver);
        }

        public void addItuDataReceiver(ItuDataReceiver receiver) {
            mThread.addItuDataReceiver(receiver);
        }

        public void addIfpanDataReceiver(IfpanDataReceiver receiver) {
            mThread.addIfpanDataReceiver(receiver);
        }

        public void addFscanDataReceiver(FscanDataReceiver receiver) {
            mThread.addFscanDataReceiver(receiver);
        }

        public void addDfDataReceiver(DfDataReceiver receiver) {
            mThread.addDfDataReceiver(receiver);
        }

        public void connect(String ip, int port, ConnectCallback callback) {
            mThread.connect(ip, port, callback);
        }

        public void disconnect() {
            mThread.disconnect();
        }

        public void sendCommand(Command cmd) {
            mThread.sendCommand(cmd);
        }
    }
}
