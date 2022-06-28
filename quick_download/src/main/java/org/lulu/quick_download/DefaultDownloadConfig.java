package org.lulu.quick_download;

import android.util.Log;

import androidx.annotation.Nullable;

import org.lulu.quick_download.log.ILogger;
import org.lulu.quick_download.retry.DefaultRetryStrategy;
import org.lulu.quick_download.retry.IRetryStrategy;

import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;

/**
 * 下载线程池
 * author: changer0
 * date: 2022/6/25
 */
public class DefaultDownloadConfig {

    private static final String TAG = "quick_download";

    private static Executor sExecutorService;

    private static OkHttpClient sOkHttpClient;

    private static IRetryStrategy sRetryStrategy;

    private static ILogger sLogger;

    /**
     * CPU核心数
     */
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    /**
     * 开启线程数量
     */
    public static final int THREAD_SIZE = Math.max(3, Math.min(CPU_COUNT - 1, 6));


    /**
     * 默认连接超时时长
     */
    public static final long CON_TIME_OUT = 20_000;

    /**
     * 默认读取超时时长
     */
    public static final long READ_TIME_OUT = 25_000;

    public static Executor executorService() {
        if (sExecutorService == null) {
            sExecutorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                    new SynchronousQueue<>(), r -> new Thread(r, "quick_download"));
        }
        return sExecutorService;
    }

    public static OkHttpClient okHttpClient() {
        if (sOkHttpClient == null) {
            OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
            builder.protocols(Collections.singletonList(Protocol.HTTP_1_1));
            builder.connectTimeout(CON_TIME_OUT, TimeUnit.MILLISECONDS);
            builder.readTimeout(READ_TIME_OUT, TimeUnit.MILLISECONDS);
            sOkHttpClient = builder.build();
        }
        return sOkHttpClient;
    }

    public static ILogger logger() {
        if (sLogger == null) {
            sLogger = new ILogger() {
                @Override
                public void i(String msg) {
                    Log.i(TAG, msg);
                }

                @Override
                public void e(String msg, @Nullable Throwable throwable) {
                    Log.e(TAG, msg, throwable);
                }
            };
        }
        return sLogger;
    }

    public static IRetryStrategy retryStrategy() {
        if (sRetryStrategy == null) {
            sRetryStrategy = new DefaultRetryStrategy();
        }
        return sRetryStrategy;
    }
}
