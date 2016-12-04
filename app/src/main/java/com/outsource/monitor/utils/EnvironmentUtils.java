package com.outsource.monitor.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Environment utility class
 *
 * @author hao.xiong
 * @version 1.0.0
 */
public class EnvironmentUtils {
    private static final String TAG = "EnvironmentUtils";

    private static String mPackageName;
    private static String mSDCardPath;
    private static String mSecondSDCardPath;
    private static String mSecondSDCardValidFolder;

    static {
        String path = "sdcard";
        File pathFile = new File(path);
        if (!pathFile.exists() || !pathFile.canWrite()) {
            pathFile = Environment.getExternalStorageDirectory();
        }
        try {
            path = pathFile.getCanonicalPath();
        } catch (Exception ignored) {
            path = pathFile.getAbsolutePath();
        }
        mSDCardPath = path;
    }

    /**
     * 初始化系统环境
     *
     * @param context 系统环境上下文
     */
    public static void init(Context context) {
        Network.init(context);

        mPackageName = context.getPackageName();
        resetAsyncTaskDefaultExecutor();
    }

    /**
     * 判断手机系统是否是魅族
     * @return true - 是
     */
    public static boolean isFlymeOs() {
        String displayId = android.os.Build.DISPLAY;
        return displayId.contains("Flyme");
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void resetAsyncTaskDefaultExecutor() {
        if (SDKVersionUtils.hasHoneycomb()) {
            try {
                Method setDefaultExecutorMethod = AsyncTask.class.getMethod("setDefaultExecutor", Executor.class);
                ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
                setDefaultExecutorMethod.invoke(null, threadPoolExecutor);

                final Field defaultHandler = ThreadPoolExecutor.class.getDeclaredField("defaultHandler");
                defaultHandler.setAccessible(true);
                defaultHandler.set(null, new ThreadPoolExecutor.DiscardOldestPolicy());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取包名
     *
     * @return 包名
     */
    public static String getPackageName() {
        return mPackageName;
    }

    /**
     * 存储信息
     */
    public static class Storage {

        /**
         * 外部存储是否可读写
         *
         * @return 可读写返回true, 否则返回false
         */
        public static boolean isExternalStorageWritable() {
            String state = Environment.getExternalStorageState();
            return Environment.MEDIA_MOUNTED.equals(state);
        }

        /**
         * 外部存储是否可读
         *
         * @return 可读返回true, 否则返回false
         */
        public static boolean isExternalStorageReadable() {
            String state = Environment.getExternalStorageState();
            return Environment.MEDIA_MOUNTED.equals(state)
                    || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
        }

        /**
         * 获取目录可用字节数，目录不存在返回0
         *
         * @param path 目录文件
         * @return 字节数
         */
        public static long getUsableSpace(File path) {
            if (SDKVersionUtils.hasGingerbread()) {
                return path.getUsableSpace();
            }
            final StatFs stats = new StatFs(path.getPath());
            return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
        }

        /**
         * 获取外部目录缓存路径
         *
         * @param context context
         * @return 外部存储换成路径
         */
        public static File getExternalCacheDir(Context context) {
            File file = null;
            if (SDKVersionUtils.hasFroyo()) {
                file = context.getExternalCacheDir();
            }

            if (file == null) {
                final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
                file = new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
            }

            file.mkdirs();

            if (file.isDirectory()) {
                return file;
            }

            return null;
        }

        /**
         * 获取SDCard路径
         *
         * @return sdcard路径，不为空
         */
        public static String getSDCardPath() {
            return mSDCardPath;
        }

        /**
         * 获取缓存路径
         *
         * @param context context
         * @return 存储路径
         */
        public static String getCachePath(Context context) {
            File file = null;
            if (isExternalStorageWritable()) {
                file = getExternalCacheDir(context);
            }
            return file != null ? file.getAbsolutePath() : context.getCacheDir().getAbsolutePath();
        }

        /**
         * 判断是否路径是否可写
         * @param path path
         * @param context context
         * @return writable
         */
        public static boolean isWritablePath(Context context, String path) {
            if (!new File(path).canWrite()) {
                return false;
            }
            String filePathPrefix = path + File.separator;
            int i = 0;
            while (FileUtils.fileExists(filePathPrefix + i)) {
                i++;
            }
            File testFile = FileUtils.createFile(filePathPrefix + i);

            if (testFile != null) {
                testFile.delete();
                return true;
            }

            return false;
        }

        /**
         * 得到可用路径
         * @param context context
         * @return writablePath
         */
        public static String getWritablePath(Context context) {
            return mSDCardPath + File.separator + "monitor";
        }

    }


    /**
     * 网络信息
     */
    public static class Network {
        /**
         * 无网络
         */
        public static final int NETWORK_INVALID = -1;
        /**
         * 2G网络
         */
        public static final int NETWORK_2G = 0;
        /**
         * wap网络
         */
        public static final int NETWORK_WAP = 1;
        /**
         * wifi网络
         */
        public static final int NETWORK_WIFI = 2;
        /**
         * 3G和3G以上网络，或统称为快速网络
         */
        public static final int NETWORK_3G = 3;

        private static final int[] NETWORK_MATCH_TABLE = {NETWORK_2G // NETWORK_TYPE_UNKNOWN
                , NETWORK_2G // NETWORK_TYPE_GPRS
                , NETWORK_2G // NETWORK_TYPE_EDGE
                , NETWORK_3G // NETWORK_TYPE_UMTS
                , NETWORK_2G // NETWORK_TYPE_CDMA
                , NETWORK_3G // NETWORK_TYPE_EVDO_O
                , NETWORK_3G // NETWORK_TYPE_EVDO_A
                , NETWORK_2G // NETWORK_TYPE_1xRTT
                , NETWORK_3G // NETWORK_TYPE_HSDPA
                , NETWORK_3G // NETWORK_TYPE_HSUPA
                , NETWORK_3G // NETWORK_TYPE_HSPA
                , NETWORK_2G // NETWORK_TYPE_IDEN
                , NETWORK_3G // NETWORK_TYPE_EVDO_B
                , NETWORK_3G // NETWORK_TYPE_LTE
                , NETWORK_3G // NETWORK_TYPE_EHRPD
                , NETWORK_3G // NETWORK_TYPE_HSPAP
        };

        private static String mIMEI = "";
        private static String mIMSI = "";
        private static String mWifiMac = "";

        private static NetworkInfo mNetworkInfo;
        private static int mDefaultNetworkType;
        private static ConnectivityManager mConnectManager;

        /**
         * 初始化默认网络参数
         *
         * @param context 上下文环境
         */
        public static void init(final Context context) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            mIMEI = telephonyManager.getDeviceId();
            if (mIMEI == null) {
                mIMEI = "";
            }

            mIMSI = telephonyManager.getSubscriberId();
            if (mIMSI == null) {
                mIMSI = "";
            }

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        mWifiMac = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getMacAddress();
                    } catch (Exception e) { //有些机型调用getConnectionInfo方法会报异常
                        e.printStackTrace();
                    }
                    if (mWifiMac == null) {
                        mWifiMac = "";
                    }
                    return null;
                }
            }.execute();

            mDefaultNetworkType = NETWORK_MATCH_TABLE[telephonyNetworkType(context)];
            mConnectManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            mNetworkInfo = mConnectManager.getActiveNetworkInfo();
        }


        /**
         * 获取IMEI串号
         *
         * @return IMEI串号。<b>有可能为空值</b>
         */
        public static String imei() {
            return mIMEI;
        }

        /**
         * 获取IMSI移动用户识别码
         *
         * @return IMSI移动用户识别码。<b>有可能为空值</b>
         */
        public static String imsi() {
            return mIMSI;
        }

        /**
         * 获取Wifi Mac地址
         *
         * @return Wifi Mac地址
         */
        public static String wifiMac() {
            return mWifiMac;
        }

        /**
         * 获取网络类型
         *
         * @return 网络类型
         */
        public static int type() {
            int networkType = mDefaultNetworkType;
            if (mConnectManager == null) {
                //当还未来得及初始化时，另一线程请求网络时通用参数中取此值时先运行到这儿，那么如不做处理将崩溃
                return NETWORK_WAP;
            }
            mNetworkInfo = mConnectManager.getActiveNetworkInfo();
            if (!networkConnected(mNetworkInfo)) {
                networkType = NETWORK_INVALID;
            } else if (isWifiNetwork(mNetworkInfo)) {
                networkType = NETWORK_WIFI;
            } else if (isWapNetwork(mNetworkInfo)) {
                networkType = NETWORK_WAP;
            }

            return networkType;
        }

        /**
         * 是否存在有效的网络连接.
         *
         * @return 存在有效的网络连接返回true，否则返回false
         */
        public static boolean isNetWorkAvailable() {
            return networkConnected(mConnectManager.getActiveNetworkInfo());
        }

        /**
         * 获取本机IPv4地址
         *
         * @return 本机IPv4地址
         */
        public static String ipv4() {
            return ipAddress(true);
        }

        /**
         * 获取本机IPv6地址
         *
         * @return 本机IPv6地址
         */
        public static String ipv6() {
            return ipAddress(false);
        }

        private static String ipAddress(boolean useIPv4) {
            try {
                for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                    NetworkInterface netInterface = en.nextElement();
                    for (Enumeration<InetAddress> iNetEnum = netInterface.getInetAddresses(); iNetEnum.hasMoreElements();) {
                        InetAddress inetAddress = iNetEnum.nextElement();
                        if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }

        /**
         * 直接从系统函数里得到的network type
         *
         * @param context context
         * @return net type
         */
        private static int telephonyNetworkType(Context context) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            int networkType = telephonyManager.getNetworkType();
            if (networkType < 0 || networkType >= NETWORK_MATCH_TABLE.length) {
                networkType = TelephonyManager.NETWORK_TYPE_UNKNOWN;
            }
            return networkType;
        }

        private static boolean networkConnected(NetworkInfo networkInfo) {
            return networkInfo != null && networkInfo.isConnected();
        }

        private static boolean isMobileNetwork(NetworkInfo networkInfo) {
            return networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        }

        @SuppressWarnings("deprecation")
        private static boolean isWapNetwork(NetworkInfo networkInfo) {
            return isMobileNetwork(networkInfo) && !TextUtils.isEmpty(android.net.Proxy.getDefaultHost());
        }

        private static boolean isWifiNetwork(NetworkInfo networkInfo) {
            return networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
        }
    }
}
