package com.outsource.monitor.service;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.outsource.monitor.parser.Command;
import com.outsource.monitor.parser.IfpanParser48278;
import com.outsource.monitor.parser.ItuParser48278;
import com.outsource.monitor.singlefrequency.model.ItuHead;
import com.outsource.monitor.utils.PromptUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Administrator on 2016/10/2.
 */
public class SocketThread extends Thread {

    private static final int TIME_OUT = 20 * 1000;
    private List<DataReceiver> mDataReceivers = new ArrayList<>(0);
    private List<ItuDataReceiver> mItuDataReceivers = new ArrayList<>(0);
    private List<IfpanDataReceiver> mIfpanDataReceivers = new ArrayList<>(0);
    private float minFrequency;
    private float maxFrequency;
    private float frequencyRange;
    private float minLevel;
    private float maxLevel;
    private float levelRange;
    private Socket mSocket;
    private Handler mUiHandler;
    private AtomicReference<Command> mCommand = new AtomicReference<>();
    private String ip;
    private int port;

    public SocketThread() {
        mUiHandler = new Handler(Looper.getMainLooper());
    }

    public void addDataReceiver(DataReceiver receiver) {
        if (receiver != null) {
            mDataReceivers.add(receiver);
        }
    }

    public void addItuDataReceiver(ItuDataReceiver receiver) {
        if (receiver != null) {
            mItuDataReceivers.add(receiver);
        }
    }

    public void addIfpanDataReceiver(IfpanDataReceiver receiver) {
        if (receiver != null) {
            mIfpanDataReceivers.add(receiver);
        }
    }

    public void connect(final String ip, final int port, final ConnectCallback callback) {
        this.ip = ip;
        this.port = port;
        disconnect();
        mSocket = new Socket();
        AsyncTask<Void, Void, Void> connectTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                SocketAddress address = new InetSocketAddress(ip, port);
                try {
                    mSocket.connect(address, TIME_OUT);
                    mUiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) {
                                callback.onConnectSuccess();
                            }
                        }
                    });
                } catch (final IOException e) {
                    e.printStackTrace();
                    mUiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) {
                                callback.onConnectFail(e.getMessage());
                            }
                        }
                    });
                }
                return null;
            }
        };
        connectTask.execute();
    }

    public void disconnect() {
        if (isConnected()) {
            try {
                mSocket.close();
                mSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendCommand(Command command) {
        if (command == null || command.type == null || TextUtils.isEmpty(command.command)) return;
        mCommand.set(command);
        if (!isConnected()) {
            connect(ip, port, new ConnectCallback() {
                @Override
                public synchronized void onConnectSuccess() {
                    synchronized (SocketThread.this) {
                        notify();
                    }
                }

                @Override
                public void onConnectFail(String message) {
                    PromptUtils.showToast(message);
                }
            });
        } else {
            synchronized (this) {
                notify();
            }
        }
    }

    public boolean isConnected() {
        return mSocket != null && mSocket.isConnected();
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            if (!isConnected()) {
                try {
                    synchronized (this) {
                        wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Command command = mCommand.get();
            if (!TextUtils.isEmpty(command.command)) {
                byte[] bytes = command.command.getBytes(Charset.forName("UTF-8"));
                ByteArrayOutputStream bos = new ByteArrayOutputStream(bytes.length);
                OutputStream outputStream = null;
                InputStream inputStream = null;
                try {
                    //发送命令
                    outputStream = mSocket.getOutputStream();
                    bos.write(bytes);
                    outputStream.write(bos.toByteArray(), 0, bos.size());
                    outputStream.flush();

                    //等待服务器返回数据
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //接受数据
                    inputStream = mSocket.getInputStream();
                    byte[] buffer = new byte[4 * 1024];
                    int byteOffset = 0;
                    int readLen = 0;
                    while ((readLen = inputStream.read(buffer, byteOffset, buffer.length - byteOffset)) != 0) {
                        byteOffset += readLen;
                        switch (command.type) {
                            case ITU:
                            {
                                ItuParser48278 data = ItuParser48278.TryParse(buffer, byteOffset);
                                if (data != null) {
                                    System.arraycopy(buffer, data.byteLen, buffer, 0, buffer.length - data.byteLen);
                                    byteOffset -= data.byteLen;
                                    onItuDataReceived(data);
                                }
                            }
                                break;
                            case IFPAN:
                            {
                                IfpanParser48278 data = IfpanParser48278.TryParse(buffer, byteOffset);
                                if (data != null) {
                                    System.arraycopy(buffer, data.byteLen, buffer, 0, buffer.length - data.byteLen);
                                    byteOffset -= data.byteLen;
                                    onIfpanDataReceived(data);
                                }
                            }
                                break;
                            default:
                                break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            disconnect();
        }
    }

    private void onItuDataReceived(ItuParser48278 data) {
        switch (data.m_frameType) {
            case ItuParser48278.FRAME_TYPE_HEAD:
                if (data.m_dataHead != null) {
                    for (ItuDataReceiver receiver : mItuDataReceivers) {
                        receiver.onReceiveItuHead(data.m_dataHead);
                    }
                }
                break;
            case ItuParser48278.FRAME_TYPE_INFO:
                break;
            case ItuParser48278.FRAME_TYPE_DATA:
                if (data.m_dataValue != null) {
                    for (ItuDataReceiver provider : mItuDataReceivers) {
                        provider.onReceiveItuData(data.m_dataValue.valueList);
                    }
                }
                break;
            default:
                break;
        }
    }

    private void onIfpanDataReceived(IfpanParser48278 data) {
        switch (data.m_frameType) {
            case IfpanParser48278.FRAME_TYPE_HEAD:
                if (data.m_dataHead != null) {
                    for (IfpanDataReceiver receiver : mIfpanDataReceivers) {
                        receiver.onReceiveIfpanHead(data.m_dataHead);
                    }
                }
                break;
            case IfpanParser48278.FRAME_TYPE_INFO:
                break;
            case IfpanParser48278.FRAME_TYPE_DATA:
                if (data.m_dataValue != null) {
                    for (IfpanDataReceiver provider : mIfpanDataReceivers) {
                        provider.onReceiveIfpanData(data.m_dataValue);
                    }
                }
                break;
            default:
                break;
        }
    }

    private List<ItuHead> mockItuHeads() {
        List<ItuHead> heads = new ArrayList<>(3);
        heads.add(new ItuHead("电平", "dbUv", -40f));
        heads.add(new ItuHead("实时频率", "MHz", -20f));
        heads.add(new ItuHead("频差", "kHz", -30f));
        return heads;
    }

    private float[] mockItuData() {
        float level = minLevel + new Random().nextInt((int) levelRange);
        return new float[]{level, level, level};
    }

    private float randomLevel() {
        return minLevel + new Random().nextInt((int) levelRange);
    }

    private float randomFrequency() {
        return minFrequency + new Random().nextInt((int) frequencyRange);
    }

    public void setFrequencyRange(float min, float max) {
        this.minFrequency = min;
        this.maxFrequency = max;
        frequencyRange = max - min;
    }

    public void setLevelRange(float min, float max) {
        this.minLevel = min;
        this.maxLevel = max;
        levelRange = max - min;
    }
}
