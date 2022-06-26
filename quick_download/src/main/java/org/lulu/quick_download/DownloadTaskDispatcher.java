package org.lulu.quick_download;

import android.os.Handler;
import android.os.HandlerThread;

import java.io.IOException;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 下载任务分发器
 *
 * author: changer0
 * date: 2022/6/25
 */
public class DownloadTaskDispatcher implements Runnable{

    private final DownloadConfig config = QuickDownload.getInstance().getConfig();

    private final OkHttpClient okHttpClient;

    /**
     * 是否正在运行
     */
    private volatile boolean running = true;

    /**
     * 下载参数
     */
    private final DownloadParams downloadParams;

    /**
     * 下载信息
     */
    private volatile DownloadInfo downloadInfo;

    /**
     * 进度更新 HandlerThread
     */
    private final HandlerThread progressHandlerThread = new HandlerThread("quick_download_progress_update");

    /**
     * 进度管理
     */
    private Handler progressHandler;

    /**
     * 失败标志
     */
    private volatile boolean downloadFailed = false;

    public DownloadTaskDispatcher(DownloadParams downloadParams) {
        this.downloadParams = downloadParams;
        okHttpClient = config.getOkHttpClient();
    }

    @Override
    public void run() {
        LogUtil.i("start download : "  + downloadParams);
        LogUtil.i("prepareDownloadInfo...");
        downloadInfo = new DownloadInfo();
        Request.Builder builder = new Request.Builder()
                .url(Objects.requireNonNull(downloadParams.getUrl()))
                .get();
        Response response = null;
        try {
            response = okHttpClient.newCall(builder.build()).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response == null) {
            notifyDownloadFailure(new RuntimeException("prepareDownloadInfo response == null"));
            return;
        }
        String acceptRanges = response.header("Accept-Ranges");
        if ("bytes".equalsIgnoreCase(acceptRanges)) {
            LogUtil.i("support split download");
            downloadInfo.setSupportBreakPointTrans(true);
        } else {
            downloadInfo.setSupportBreakPointTrans(false);
        }
        ResponseBody body = response.body();

        if (body == null) {
            notifyDownloadFailure(new RuntimeException("prepareDownloadInfo body == null"));
            return;
        }
        downloadInfo.setTotalLength(body.contentLength());

        if (downloadInfo.getTotalLength() <= 0) {
            //长度获取失败也是异常
            DownloadUtil.close(response);
            notifyDownloadFailure(new RuntimeException("prepareDownloadInfo totalLength <= 0"));
            LogUtil.e("obtain file total length failed!");
            return;
        }
        notifyReady();
        if (downloadInfo.isSupportBreakPointTrans() && downloadParams.isUseMultiThread()) {
            //先分片
            splitSegments();
            //再下载
            if (downloadInfo.isSegmentsEnable()) {
                launchMultiThreadDownload();
            } else {
                launchSingleThreadDownload(body);
            }
        } else {
            launchSingleThreadDownload(body);
        }
        DownloadUtil.close(response);

    }

    private void notifyReady() {
        DownloadListener listener = downloadParams.getListener();
        if (listener != null) {
            listener.onReady(downloadParams, downloadInfo);
        }
    }

    /**
     * 拆分下载块
     */
    private void splitSegments() {
        long len = downloadInfo.getTotalLength();
        int threadCount = config.getThreadCount();
        DownloadSegment[] segments = new DownloadSegment[threadCount];
        downloadInfo.setSegments(segments);
        //每个分块的大小
        long splitSize = len / threadCount;

        //根据线程数拆分
        for (int i = 0; i < segments.length; i++) {
            DownloadSegment segment = new DownloadSegment();
            //开始位置
            long startPos = i * splitSize;
            segment.setStartPos(startPos);
            long end;
            if (i == segments.length - 1) {
                //最后一块
                end = downloadInfo.getTotalLength() - 1;
            } else {
                end = startPos + splitSize - 1;
            }
            //结束位置
            segment.setEndPos(end);
            segment.setIndex(i);
            segments[i] = segment;
        }
    }

    /**
     * 启动单线程直接下载
     * 处理一些不支持多线程下载的 url
     */
    private void launchSingleThreadDownload(ResponseBody body) {
        try {
            LogUtil.i("launch single thread download...");
            startSingleThreadProgressLooper();
            DownloadUtil.directDownload(body, downloadParams, downloadInfo);
            notifyDownloadSuccess();
        } catch (IOException e) {
            progressHandlerThread.quit();
            notifyDownloadFailure(e);
            e.printStackTrace();
        }
    }

    /**
     * 启动多线程下载
     */
    private void launchMultiThreadDownload() {
        LogUtil.i("launch multi thread download...");
        DownloadSegment[] segments = downloadInfo.getSegments();
        for (DownloadSegment segment : segments) {
            startSegmentDownload(segment);
        }
        startMultiThreadProgressLooper();
    }

    private void startSegmentDownload(DownloadSegment segment) {
        if (segment == null) {
            return;
        }
        DownloadSegmentTask segmentTask = new DownloadSegmentTask(downloadParams, segment);
        segmentTask.setListener(new DownloadSegmentTask.DownloadSegmentListener() {
            @Override
            public void onSuccess(DownloadSegment segment) {
                notifyDownloadSegmentSuccess(segment);
            }

            @Override
            public void onFailure(DownloadSegment segment, Throwable e) {
                if (downloadFailed) {
                    return;
                }
                downloadFailed = true;
                //停止
                progressHandlerThread.quit();
                // TODO: 2022/6/25 此处做重试逻辑, 避免直接失败, 失败回调限制一次
                notifyDownloadFailure(e);
            }
        });
        config.getExecutor().execute(segmentTask);
    }

    private void notifyDownloadSegmentSuccess(DownloadSegment segment) {
        running = false;
        DownloadListener listener = downloadParams.getListener();
        if (listener == null) {
            return;
        }
        if (downloadInfo.isAllSegmentDownloadFinish()) {
            listener.onSegmentDownloadFinish(segment);
            notifyDownloadSuccess();
        } else {
            listener.onSegmentDownloadFinish(segment);
        }
    }

    private void notifyDownloadSuccess() {
        running = false;
        DownloadListener listener = downloadParams.getListener();
        if (listener == null) {
            return;
        }
        forceRefreshProgressFinish();
        listener.onDownloadSuccess();
    }

    private void notifyDownloadFailure(Throwable e) {
        running = false;
        DownloadListener listener = downloadParams.getListener();
        if (listener == null) {
            return;
        }
        listener.onDownloadFailure(e);
    }

    private void startMultiThreadProgressLooper() {
        progressHandlerThread.start();
        progressHandler = new MultiThreadProgressHandler(progressHandlerThread.getLooper(), downloadParams, downloadInfo);
        progressHandler.sendEmptyMessage(0);
    }

    private void startSingleThreadProgressLooper() {
        progressHandlerThread.start();
        progressHandler = new SingleThreadHandler(progressHandlerThread.getLooper(), downloadParams, downloadInfo);
        progressHandler.sendEmptyMessage(0);
    }

    /**
     * 通知强制完成
     */
    public void forceRefreshProgressFinish() {
        DownloadListener listener = downloadParams.getListener();
        if (listener != null) {
            listener.onProgress(100);
        }
    }

    public boolean isRunning() {
        return running;
    }

}
