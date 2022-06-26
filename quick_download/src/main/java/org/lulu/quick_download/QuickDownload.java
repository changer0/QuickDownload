package org.lulu.quick_download;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * author: changer0
 * date: 2022/6/25
 */
public class QuickDownload {

    private DownloadConfig config = new DownloadConfig();

    private static volatile QuickDownload sInstance;

    private final Map<String, DownloadTaskDispatcher> taskDispatcherMap = new HashMap<>();

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
    public String addTask(String url, File desFile, DownloadListener listener) {
        return addTask(new DownloadParams(url, desFile, listener));
    }

    public String addTask(DownloadParams downloadParams) {
        String uniqueId = downloadParams.getUniqueId();
        DownloadTaskDispatcher downloadTaskDispatcher = taskDispatcherMap.get(uniqueId);
        //避免重复添加任务
        if (downloadTaskDispatcher != null && downloadTaskDispatcher.isRunning()) {
            return uniqueId;
        }
        DownloadTaskDispatcher taskDispatcher = new DownloadTaskDispatcher(
                downloadParams
        );
        getConfig().getExecutor().execute(taskDispatcher);
        taskDispatcherMap.put(uniqueId, taskDispatcher);
        return uniqueId;
    }

    public boolean pauseTask(String url, File desFile) {
        return pauseTaskById(new DownloadParams(url, desFile).getUniqueId());
    }

    public boolean pauseTaskById(String id) {
        DownloadTaskDispatcher downloadTaskDispatcher = taskDispatcherMap.get(id);
        if (downloadTaskDispatcher == null) {
            return false;
        }
        downloadTaskDispatcher.cancel();
        removeTask(id);
        return true;
    }

    public void removeTask(String id) {
        taskDispatcherMap.remove(id);
    }
}
