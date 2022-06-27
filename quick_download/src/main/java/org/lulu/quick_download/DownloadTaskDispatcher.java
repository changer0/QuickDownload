package org.lulu.quick_download;

import android.os.Handler;
import android.os.HandlerThread;

import org.lulu.quick_download.db.DownloadDBHandle;
import org.lulu.quick_download.db.FileInfo;
import org.lulu.quick_download.db.SegmentInfo;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

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
     * 多线程下载失败标志
     */
    private volatile boolean multiThreadDownloadFailed = false;
    /**
     * 多线程下载成功标志
     */
    private volatile boolean multiThreadDownloadSucceed = false;

    /**
     * Segment 集合
     */
    private final List<DownloadSegmentTask> downloadSegmentTasks = new CopyOnWriteArrayList<>();

    public DownloadTaskDispatcher(DownloadParams downloadParams) {
        this.downloadParams = downloadParams;
        okHttpClient = config.getOkHttpClient();
    }

    @Override
    public void run() {
        LogUtil.i("start download : "  + downloadParams);
        LogUtil.i("prepareDownloadInfo...");
        downloadInfo = new DownloadInfo(downloadParams.getUniqueId());
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
            notifyDownloadFailure(DownloadConstants.ERROR_CODE_UNKNOWN, new RuntimeException("prepareDownloadInfo response == null"));
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
            notifyDownloadFailure(DownloadConstants.ERROR_CODE_UNKNOWN, new RuntimeException("prepareDownloadInfo body == null"));
            return;
        }
        downloadInfo.setTotalLength(body.contentLength());

        if (downloadInfo.getTotalLength() <= 0) {
            //长度获取失败也是异常
            DownloadUtil.close(response);
            notifyDownloadFailure(DownloadConstants.ERROR_CODE_UNKNOWN, new RuntimeException("prepareDownloadInfo totalLength <= 0"));
            LogUtil.e("obtain file total length failed!");
            return;
        }
        if (downloadInfo.isSupportBreakPointTrans()) {
            //检查本地Info
            checkLocalFileInfo();
            //先分片
            splitSegments();
            //通知 Ready
            notifyReady();
            //再下载
            if (downloadInfo.isSegmentsEnable()) {
                launchMultiThreadDownload();
            } else {
                launchSingleThreadDownload(body);
            }
        } else {
            notifyReady();
            launchSingleThreadDownload(body);
        }
        DownloadUtil.close(response);
    }

    /**
     * 检查本地文件信息
     */
    private void checkLocalFileInfo(){
        FileInfo fileInfo = DownloadDBHandle.getInstance().getFileInfo(downloadParams.getUniqueId());
        File descFile = downloadParams.getDescFile();

        LogUtil.i("file info: " + fileInfo);
        if (fileInfo == null) {
            LogUtil.i("no download record !");
            if (descFile.exists()) {
                LogUtil.i("fileInfo == null delete file: " + descFile.delete());
            }
            int count = DownloadDBHandle.getInstance().deleteDownloadSegment(downloadParams.getUniqueId());
            LogUtil.i("delete saved segment: " + count);
            return;
        }
        if (!descFile.exists()) {
            int count = DownloadDBHandle.getInstance().deleteDownloadSegment(downloadParams.getUniqueId());
            LogUtil.e("file no exists ! delete saved segment: " + count);
            return;
        }
        //如果 DB 中存在, 文件也存在
        //看需要清除该下载下所有脏数据!
        List<SegmentInfo> segmentInfoList = DownloadDBHandle.getInstance().getSegmentInfo(downloadParams.getUniqueId());
        boolean removeDBInfo =
                //文件长度是否一致
                fileInfo.getLength() != downloadInfo.getTotalLength()
                        // 分片个数是否与线程个数一致
                        || segmentInfoList.size() != config.getThreadCount();
        if (removeDBInfo) {
            LogUtil.i("need clear local dirty data! ");
            LogUtil.i("delete file: " + descFile.delete());
            int fileInfoCount = DownloadDBHandle.getInstance().deleteFileInfo(downloadParams.getUniqueId());
            LogUtil.i("delete saved file info: " + fileInfoCount);
            int segmentCount = DownloadDBHandle.getInstance().deleteDownloadSegment(downloadParams.getUniqueId());
            LogUtil.i("delete saved segment: " + segmentCount);
        }
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
        //获取所有下载块
        List<SegmentInfo> segmentInfoList = DownloadDBHandle.getInstance().getSegmentInfo(downloadParams.getUniqueId());

        LogUtil.i("saved downloadSegments: " + segmentInfoList);

        //根据线程数拆分
        for (int i = 0; i < segments.length; i++) {
            DownloadSegment segment = new DownloadSegment(downloadParams.getUniqueId(), i);

            //读取已保存的进度
            if (!segmentInfoList.isEmpty()) {
                for (int j = 0; j < segmentInfoList.size(); j++) {
                    SegmentInfo savedSegment = segmentInfoList.get(j);
                    if (savedSegment.getIndex() == i ) {
                        //只检查成功状态
                        if (savedSegment.getFinished() == 1) {
                            segment.setState(DownloadSegment.State.SUCCESS);
                        }
                        segment.setDownloadLength(savedSegment.getDownloadPos());
                    }
                }
            }

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
            File descFile = downloadParams.getDescFile();
            if (descFile.exists()) {
                LogUtil.i("delete file before download");
            }
            startSingleThreadProgressLooper();
            DownloadUtil.directDownload(body, downloadParams, downloadInfo);
            notifyDownloadSuccess();
        } catch (IOException e) {
            progressHandlerThread.quit();
            notifyDownloadFailure(DownloadConstants.ERROR_CODE_UNKNOWN, e);
            e.printStackTrace();
        }
    }

    /**
     * 启动多线程下载
     */
    private void launchMultiThreadDownload() {
        LogUtil.i("launch multi thread download...");
        DownloadSegment[] segments = downloadInfo.getSegments();
        downloadSegmentTasks.clear();
        for (DownloadSegment segment : segments) {
            startSegmentDownload(segment);
        }
        startMultiThreadProgressLooper();
    }

    private void startSegmentDownload(DownloadSegment segment) {
        if (segment == null) {
            return;
        }
        //可能已经下载过了
        if (segment.getState() == DownloadSegment.State.SUCCESS) {
            notifyDownloadSegmentSuccess(segment);
        }
        DownloadSegmentTask segmentTask = new DownloadSegmentTask(downloadParams, segment);
        downloadSegmentTasks.add(segmentTask);
        segmentTask.setListener(new DownloadSegmentTask.DownloadSegmentListener() {
            @Override
            public void onSuccess(DownloadSegment segment) {
                notifyDownloadSegmentSuccess(segment);
            }

            @Override
            public void onFailure(DownloadSegment segment, int errorCode, Throwable e) {

                // TODO: 2022/6/25 此处做重试逻辑, 避免直接失败, 失败回调限制一次
                notifyDownloadFailure(errorCode, e);
            }
        });
        config.getExecutor().execute(segmentTask);
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

    /**
     * 取消所有任务
     */
    public void cancel() {
        if (downloadSegmentTasks.isEmpty()) {
            return;
        }
        for (DownloadSegmentTask downloadSegmentTask : downloadSegmentTasks) {
            downloadSegmentTask.cancel();
        }
    }

    private void notifyDownloadSegmentSuccess(DownloadSegment segment) {
        DownloadListener listener = downloadParams.getListener();
        if (listener != null) {
            listener.onSegmentDownloadFinish(segment);
        }
        if (downloadInfo.isAllSegmentDownloadFinish()) {
            notifyDownloadSuccess();
        }
    }

    private void notifyDownloadSuccess() {
        running = false;
        if (multiThreadDownloadSucceed) {
            return;
        }
        multiThreadDownloadSucceed = true;

        saveFileInfoToDB(downloadInfo, true);
        QuickDownload.getInstance().removeTask(downloadParams.getUniqueId());
        DownloadListener listener = downloadParams.getListener();
        if (listener == null) {
            return;
        }
        forceRefreshProgressFinish();
        listener.onDownloadSuccess();
    }

    private synchronized void notifyDownloadFailure(int errorCode, Throwable e) {
        running = false;
        if (multiThreadDownloadFailed) {
            return;
        }
        multiThreadDownloadFailed = true;

        //停止
        progressHandlerThread.quit();
        saveFileInfoToDB(downloadInfo, false);
        QuickDownload.getInstance().removeTask(downloadParams.getUniqueId());
        LogUtil.e("download failed | errorCode: " + errorCode + " msg: " + e.getMessage());
        DownloadListener listener = downloadParams.getListener();
        if (listener == null) {
            return;
        }
        listener.onDownloadFailure(errorCode, e);
    }

    private void saveFileInfoToDB(DownloadInfo downloadInfo, boolean isFinished) {
        DownloadDBHandle.getInstance().saveFileInfo(
                new FileInfo(downloadInfo.getId(), downloadInfo.getTotalLength(), isFinished ? 1 : 0)
        );
    }
}
