package org.lulu.quick_download;

import java.io.File;

/**
 * author: changer0
 * date: 2022/6/25
 */
public class QuickDownload {

    private DownloadConfig config = new DownloadConfig();

    private static volatile QuickDownload sInstance;

    private QuickDownload() {
    }

    public static QuickDownload getInstance() {
        if (sInstance == null) {
            synchronized (QuickDownload.class) {
                if (sInstance == null) {
                    sInstance = new QuickDownload();
                }
            }
        }
        return sInstance;
    }

    public DownloadConfig getConfig() {
        return config;
    }

    /**
     * 参数可配
     * @param config 可通过 {@link DownloadConfig#newBuilder()} 构建
     *              eg: QuickDownload.getInstance().getConfig().newBuilder();
     */
    public void setConfig(DownloadConfig config) {
        this.config = config;
    }

    /**
     * 添加下载任务
     */
    public void addTask(String url, File desFile, DownloadListener listener) {
        getConfig().getExecutor().execute(new DownloadTaskDispatcher(
                new DownloadParams(url, desFile, listener)
        ));
    }

}
