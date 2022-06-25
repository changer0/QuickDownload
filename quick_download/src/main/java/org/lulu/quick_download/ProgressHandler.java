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

    /**
     * 下载信息
     */
    private final DownloadInfo downloadInfo;

    public ProgressHandler(Looper looper, DownloadParams downloadParams, DownloadInfo downloadInfo) {
        super(looper);
        this.downloadParams = downloadParams;
        this.downloadInfo = downloadInfo;
    }

    @Override
    public boolean sendMessageAtTime(@NonNull Message msg, long uptimeMillis) {
        return super.sendMessageAtTime(msg, uptimeMillis);
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        int tempProgress = 0;
        DownloadSegment[] segments = downloadInfo.getSegments();
        for (DownloadSegment segment : segments) {
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

        int allProgress = tempProgress / segments.length;

        if (allProgress >= 100) {
            //see forceFinish
            LogUtil.i("downloading progress : finish");
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

    /**
     * 通知强制完成
     */
    public void forceFinish() {
        DownloadListener listener = downloadParams.getListener();
        if (listener != null) {
            listener.onProgress(100);
        }
    }

    public void terminate() {
        removeCallbacksAndMessages(null);
    }
}
