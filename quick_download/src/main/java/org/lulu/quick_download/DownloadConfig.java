package org.lulu.quick_download;

import java.util.concurrent.Executor;

import okhttp3.OkHttpClient;

/**
 * 配置信息
 * author: changer0
 * date: 2022/6/25
 */
public class DownloadConfig {
    /**
     * 线程池
     */
    private Executor executor;

    /**
     * OkHttpClient
     */
    private OkHttpClient okHttpClient;

    /**
     * Log
     */
    private ILogger logger;

    /**
     * 线程数量
     */
    private int threadCount = -1;

    public synchronized Executor getExecutor() {
        if (executor == null) {
            return DefaultDownloadConfig.executorService();
        }
        return executor;
    }

    public synchronized OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            return DefaultDownloadConfig.okHttpClient();
        }
        return okHttpClient;
    }

    public ILogger getLogger() {
        if (logger == null) {
            return DefaultDownloadConfig.logger();
        }
        return logger;
    }

    public int getThreadCount() {
        if (threadCount <= 0) {
            return DefaultDownloadConfig.THREAD_SIZE;
        }
        return threadCount;
    }

    /**
     * 构建者
     */
    public Builder newBuilder() {
        return new Builder(this);
    }


    public static class Builder {
        private final DownloadConfig config;

        public Builder(DownloadConfig config) {
            this.config = config;
        }

        public Builder executor(Executor executor) {
            this.config.executor = executor;
            return this;
        }

        public Builder okHttpClient(OkHttpClient okHttpClient) {
            this.config.okHttpClient = okHttpClient;
            return this;
        }

        public Builder log(ILogger log) {
            this.config.logger = log;
            return this;
        }

        public Builder threadCount(int threadCount) {
            this.config.threadCount = threadCount;
            return this;
        }


        public DownloadConfig build() {
            return this.config;
        }
    }
}
