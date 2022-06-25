package org.lulu.quick_download;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.io.File;

/**
 * author: changer0
 * date: 2022/6/25
 */
public class ProgressHandler extends Handler {
    /**
     * 下载参数
     */
    private final DownloadParams downloadParams;

    private final DownloadInfo downloadInfo;

    private volatile boolean isRunning = false;

    public ProgressHandler(Looper looper, DownloadParams downloadParams, DownloadInfo downloadInfo) {
        super(looper);
        this.downloadParams = downloadParams;
        this.downloadInfo = downloadInfo;
    }

    @Override
    public boolean sendMessageAtTime(@NonNull Message msg, long uptimeMillis) {
        isRunning = true;
        return super.sendMessageAtTime(msg, uptimeMillis);
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        if (!isRunning) {
            return;
        }
        int tempProgress = 0;
        for (DownloadSegment segment : downloadInfo.segments) {
            if (segment.getState() == DownloadSegment.State.FAILURE) {
                LogUtil.i("downloading progress : failure looper quit");
                getLooper().quit();
                return;
            }
            int progress = segment.getProgress();

            //LogUtil.i("read " + segment.getIndex() + " progress: " + progress);
            tempProgress += progress;
            //LogUtil.i(segment.getIndex() + " current segment progress: " + progress);
        }

        int allProgress = tempProgress / downloadInfo.segments.length;

        if (allProgress >= 100) {
            LogUtil.i("downloading progress : 100");
            getLooper().quit();
            return;
        }
        DownloadListener listener = downloadParams.getListener();
        if (listener != null) {
            listener.onProgress(allProgress);
        }
        LogUtil.i("downloading allProgress : " + allProgress + " tempProgress: " + tempProgress);
        sendEmptyMessageDelayed(0, 300);
    }

    public void terminate() {
        isRunning = false;
    }
}
