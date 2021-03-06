package com.outsource.monitor.monitor.base.service;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.outsource.monitor.monitor.base.parser.DScanParser48278;
import com.outsource.monitor.monitor.base.parser.MScanParser48278;
import com.outsource.monitor.monitor.df.DfDataReceiver;
import com.outsource.monitor.monitor.digit.DigitDataReceiver;
import com.outsource.monitor.monitor.discrete.DiscreteDataReceiver;
import com.outsource.monitor.monitor.fscan.FscanDataReceiver;
import com.outsource.monitor.monitor.ifpan.IfpanDataReceiver;
import com.outsource.monitor.monitor.itu.ItuDataReceiver;
import com.outsource.monitor.monitor.base.parser.Command;
import com.outsource.monitor.monitor.base.parser.DFParser48278;
import com.outsource.monitor.monitor.base.parser.FscanParser48278;
import com.outsource.monitor.monitor.base.parser.IfpanParser48278;
import com.outsource.monitor.monitor.base.parser.ItuParser48278;
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
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Administrator on 2016/10/2.
 */
public class SocketThread extends Thread {

    private static final int TIME_OUT = 20 * 1000;
    private List<ItuDataReceiver> mItuDataReceivers = new ArrayList<>(0);
    private List<IfpanDataReceiver> mIfpanDataReceivers = new ArrayList<>(0);
    private List<FscanDataReceiver> mFScanDataReceivers = new ArrayList<>(0);
    private List<DfDataReceiver> mDfDataReceivers = new ArrayList<>(0);
    private List<DiscreteDataReceiver> mDiscreteDataReceivers = new ArrayList<>(0);
    private List<DigitDataReceiver> mDigitDataReceivers = new ArrayList<>(0);
    private Socket mSocket;
    private Handler mUiHandler;
    private AtomicReference<Command> mCommand = new AtomicReference<>();
    private String ip;
    private int port;

    public SocketThread() {
        mUiHandler = new Handler(Looper.getMainLooper());
    }

    public void addItuDataReceiver(ItuDataReceiver receiver) {
        if (receiver != null) {
            if (!mItuDataReceivers.contains(receiver)) {
                mItuDataReceivers.add(receiver);
            }
        }
    }

    public void addIfpanDataReceiver(IfpanDataReceiver receiver) {
        if (receiver != null) {
            mIfpanDataReceivers.add(receiver);
        }
    }

    public void addFscanDataReceiver(FscanDataReceiver receiver) {
        if (receiver != null) {
            if (!mFScanDataReceivers.contains(receiver)) {
                mFScanDataReceivers.add(receiver);
            }
        }
    }

    public void addDfDataReceiver(DfDataReceiver receiver) {
        if (receiver != null) {
            if (!mDfDataReceivers.contains(receiver)) {
                mDfDataReceivers.add(receiver);
            }
        }
    }

    public void addDiscreteDataReceiver(DiscreteDataReceiver receiver) {
        if (receiver != null) {
            if (!mDiscreteDataReceivers.contains(receiver)) {
                mDiscreteDataReceivers.add(receiver);
            }
        }
    }

    public void addDigitDataReceiver(DigitDataReceiver receiver) {
        if (receiver != null) {
            if (!mDigitDataReceivers.contains(receiver)) {
                mDigitDataReceivers.add(receiver);
            }
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
            } catch (Exception e) {
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
                byte[] bytes = command.command.getBytes(Charset.forName("GBK"));
                ByteArrayOutputStream bos = new ByteArrayOutputStream(bytes.length);
                OutputStream outputStream = null;
                InputStream inputStream = null;
                try {
                    //发送命令
                    if (!isConnected()) continue;
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
                    byte[] buffer = new byte[1024 * 1024];
                    int byteOffset = 0;
                    int readLen = 0;
                    while (isConnected() && (readLen = mSocket.getInputStream().read(buffer, byteOffset, buffer.length - byteOffset)) != 0) {
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
                            case FSCAN:
                            {
                                FscanParser48278 data = FscanParser48278.TryParse(buffer, byteOffset);
                                if (data != null) {
                                    System.arraycopy(buffer, data.byteLen, buffer, 0, buffer.length - data.byteLen);
                                    byteOffset -= data.byteLen;
                                    onFScanDataReceived(data);
                                }
                            }
                            break;
                            case DF:
                            {
                                DFParser48278 data = DFParser48278.TryParse(buffer, byteOffset);
                                if (data != null) {
                                    System.arraycopy(buffer, data.byteLen, buffer, 0, buffer.length - data.byteLen);
                                    byteOffset -= data.byteLen;
                                    onDfDataReceived(data);
                                }
                            }
                            break;
                            case DISCRETE:
                            {
                                //离散扫描
                                MScanParser48278 data = MScanParser48278.TryParse(buffer, byteOffset);
                                if (data != null) {
                                    System.arraycopy(buffer, data.byteLen, buffer, 0, buffer.length - data.byteLen);
                                    byteOffset -= data.byteLen;
                                    onDiscreteDataReceived(data);
                                }
                            }
                            break;
                            case DIGIT:
                            {
                                //数字扫描
                                DScanParser48278 data = DScanParser48278.TryParse(buffer, byteOffset);
                                if (data != null) {
                                    System.arraycopy(buffer, data.byteLen, buffer, 0, buffer.length - data.byteLen);
                                    byteOffset -= data.byteLen;
                                    onDigitDataReceived(data);
                                }
                            }
                            break;
                            default:
                                break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
//                    if (inputStream != null) {
//                        try {
//                            inputStream.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
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

    private void onFScanDataReceived(FscanParser48278 data) {
        switch (data.m_frameType) {
            case FscanParser48278.FRAME_TYPE_HEAD:
                if (data.m_dataHead != null) {
                    for (FscanDataReceiver receiver : mFScanDataReceivers) {
                        receiver.onReceiveFScanHead(data.m_dataHead);
                    }
                }
                break;
            case FscanParser48278.FRAME_TYPE_INFO:
                break;
            case FscanParser48278.FRAME_TYPE_DATA:
                if (data.m_dataValue != null) {
                    for (FscanDataReceiver receiver : mFScanDataReceivers) {
                        receiver.onReceiveFScanData(data.m_dataValue);
                    }
                }
                break;
            default:
                break;
        }
    }

    private void onDfDataReceived(DFParser48278 data) {
        switch (data.m_frameType) {
            case DFParser48278.FRAME_TYPE_HEAD:
                if (data.m_dataHead != null) {
                    for (DfDataReceiver receiver : mDfDataReceivers) {
                        receiver.onReceiveDfHead(data.m_dataHead);
                    }
                }
                break;
            case DFParser48278.FRAME_TYPE_INFO:
                break;
            case DFParser48278.FRAME_TYPE_DATA:
                if (data.m_dataValue != null) {
                    for (DfDataReceiver receiver : mDfDataReceivers) {
                        receiver.onReceiveDfData(data.m_dataValue);
                    }
                }
                break;
            default:
                break;
        }
    }

    //数字扫描
    private void onDigitDataReceived(DScanParser48278 data)
    {
        switch (data.m_frameType) {
            case DScanParser48278.FRAME_TYPE_HEAD:
                if (data.m_dataHead != null) {
                    for (DigitDataReceiver receiver : mDigitDataReceivers) {
                        receiver.onReceiveDigitHead(data.m_dataHead);
                    }
                }
                break;
            case DFParser48278.FRAME_TYPE_INFO:
                break;
            case DFParser48278.FRAME_TYPE_DATA:
                if (data.m_dataValue != null) {
                    for (DigitDataReceiver receiver : mDigitDataReceivers) {
                        receiver.onReceiveDigitData(data.m_dataValue);
                    }
                }
                break;
            default:
                break;
        }
    }

    //离散扫描
    private void onDiscreteDataReceived(MScanParser48278 data)
    {
        switch (data.m_frameType) {
            case DScanParser48278.FRAME_TYPE_HEAD:
                if (data.m_dataHead != null) {
                    for (DiscreteDataReceiver receiver : mDiscreteDataReceivers) {
                        receiver.onReceiveDiscreteHead(data.m_dataHead);
                    }
                }
                break;
            case DFParser48278.FRAME_TYPE_INFO:
                break;
            case DFParser48278.FRAME_TYPE_DATA:
                if (data.m_dataValue != null) {
                    for (DiscreteDataReceiver receiver : mDiscreteDataReceivers) {
                        receiver.onReceiveDiscreteData(data.m_dataValue);
                    }
                }
                break;
            default:
                break;
        }
    }
}
