package com.outsource.monitor.utils;

import android.os.AsyncTask;

/**
 * Created by Administrator on 2016/11/1.
 */
public class AsyncTaskUtils {

    public static void runAsyncTask(final Runnable backgroundJob) {
        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if (backgroundJob != null) {
                    backgroundJob.run();
                }
                return null;
            }
        };
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
