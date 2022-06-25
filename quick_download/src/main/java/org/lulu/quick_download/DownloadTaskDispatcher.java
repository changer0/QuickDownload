package org.lulu.quick_download;

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
     * CPU核心数
     */
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    /**
     * 开启线程数量
     */
//    public static final int THREAD_SIZE = Math.max(3, Math.min(CPU_COUNT - 1, 6));
    public static final int THREAD_SIZE = 8;

    /**
     * 准备
     */
    private boolean isReady = false;

    /**
     * 是否正在运行
     */
    private volatile boolean isRunning = false;

    /**
     * 下载参数
     */
    private final DownloadParams downloadParams;

    /**
     * 下载信息
     */
    private volatile DownloadInfo downloadInfo;

    /**
     * 进度管理
     */
    protected ProgressHandler progressHandler;


    public DownloadTaskDispatcher(DownloadParams downloadParams) {
        this.downloadParams = downloadParams;
        okHttpClient = config.getOkHttpClient();
    }

    @Override
    public void run() {
        isRunning = true;
        prepareDownloadInfo();
        launchDownload();
    }

    /**
     * 准备下载信息
     */
    private void prepareDownloadInfo() {
        LogUtil.i("start prepareDownloadInfo");
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
            notifyDownloadSegmentFailure(new RuntimeException("prepareDownloadInfo response == null"));
            return;
        }
        String acceptRanges = response.header("Accept-Ranges");
        if ("bytes".equalsIgnoreCase(acceptRanges)) {
            LogUtil.i("support split download");
            downloadInfo.isSupportSplit = true;
        } else {
            downloadInfo.isSupportSplit = false;
        }
        ResponseBody body = response.body();
        if (body != null) {
            downloadInfo.totalLength = body.contentLength();
        }
        response.close();
        if (downloadInfo.totalLength <= 0) {
            notifyDownloadSegmentFailure(new RuntimeException("prepareDownloadInfo totalLength <= 0"));
            LogUtil.e("obtain file total length failed!");
            return;
        }
        if (downloadInfo.isSupportSplit) {
            splitSegments();
        }
        isReady = true;
        DownloadListener listener = downloadParams.getListener();
        if (listener != null) {
            listener.onReady(downloadParams, downloadInfo);
        }
    }


    /**
     * 拆分下载块
     */
    void splitSegments() {
        long len = downloadInfo.totalLength;
        downloadInfo.segments = new DownloadSegment[THREAD_SIZE];
        //每个分块的大小
        long splitSize = len / THREAD_SIZE;

        //根据线程数拆分
        for (int i = 0; i < downloadInfo.segments.length; i++) {
            DownloadSegment segment = new DownloadSegment();
            //开始位置
            long startPos = i * splitSize;
            segment.setStartPos(startPos);
            long end;
            if (i == downloadInfo.segments.length - 1) {
                //最后一块
                end = downloadInfo.totalLength - 1;
            } else {
                end = startPos + splitSize - 1;
            }
            //结束位置
            segment.setEndPos(end);
            segment.setIndex(i);
            downloadInfo.segments[i] = segment;
        }
    }

    /**
     * 启动下载
     */
    private void launchDownload() {
        if (!isReady) {
            LogUtil.e("no ready!");
            return;
        }
        if (downloadInfo.isSegmentsEnable()) {
            LogUtil.i("launch split download...");
            for (int i = 0; i < downloadInfo.segments.length; i++) {
                startSegmentDownload(downloadInfo.segments[i]);
            }
            startProgressLooper();
        }
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
                progressHandler.terminate();
                notifyDownloadSegmentFailure(e);
            }
        });
        config.getExecutor().execute(segmentTask);
    }

    private void notifyDownloadSegmentSuccess(DownloadSegment segment) {
        isRunning = false;
        DownloadListener listener = downloadParams.getListener();
        if (listener == null) {
            return;
        }
        if (downloadInfo.isDownloadFinish()) {
            listener.onSegmentDownloadFinish(segment);
            listener.onDownloadSuccess();
        } else {
            listener.onSegmentDownloadFinish(segment);
        }
    }

    private void notifyDownloadSegmentFailure(Throwable e) {
        isRunning = false;
        DownloadListener listener = downloadParams.getListener();
        if (listener == null) {
            return;
        }
        listener.onDownloadFailure(e);
    }
    private void startProgressLooper() {
        HandlerThread handlerThread = new HandlerThread("quick_download_progress_update");
        handlerThread.start();
        progressHandler = new ProgressHandler(handlerThread.getLooper(), downloadParams, downloadInfo);
        progressHandler.sendEmptyMessage(0);
    }

    public boolean isRunning() {
        return isRunning;
    }

}
