package org.lulu.quick_download;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.io.File;

/**
 * 多线程进度管理
 * author: changer0
 * date: 2022/6/25
 */
public class MultiThreadProgressHandler extends Handler {
    /**
     * 下载参数
     */
    private final DownloadParams downloadParams;

    /**
     * 下载信息
     */
    private final DownloadInfo downloadInfo;

    public MultiThreadProgressHandler(Looper looper, DownloadParams downloadParams, DownloadInfo downloadInfo) {
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
                LogUtil.i("MultiThreadProgressHandler | downloading progress : failure looper quit");
                getLooper().quit();
                return;
            }
            int progress = segment.getProgress();

            //LogUtil.i("read " + segment.getIndex() + " progress: " + progress);
            tempProgress += progress;
            //LogUtil.i(segment.getIndex() + " current segment progress: " + progress);
        }

        int allProgress = tempProgress / segments.length;
        downloadInfo.setProgress(allProgress);
        if (allProgress >= 100) {
            //see forceFinish
            LogUtil.i("MultiThreadProgressHandler | downloading progress : finish");
            getLooper().quit();
            return;
        }
        DownloadListener listener = downloadParams.getListener();
        if (listener != null) {
            listener.onProgress(allProgress);
        }
        LogUtil.i("MultiThreadProgressHandler | downloading allProgress : " + allProgress + " tempProgress: " + tempProgress);
        sendEmptyMessageDelayed(0, 300);
    }

}
