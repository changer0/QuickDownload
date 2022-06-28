package org.lulu.quick_download;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import org.lulu.quick_download.log.LogUtil;

/**
 * author: changer0
 * date: 2022/6/26
 */
public class SingleThreadHandler extends Handler {
    /**
     * 下载参数
     */
    private final DownloadParams downloadParams;

    /**
     * 下载信息
     */
    private final DownloadInfo downloadInfo;

    public SingleThreadHandler(@NonNull Looper looper, DownloadParams downloadParams, DownloadInfo downloadInfo) {
        super(looper);
        this.downloadParams = downloadParams;
        this.downloadInfo = downloadInfo;
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);

        int progress = downloadInfo.getProgress();
        if (progress >= 100) {
            //see forceFinish
            LogUtil.i("SingleThreadHandler | downloading progress : finish");
            getLooper().quit();
            return;
        }
        DownloadListener listener = downloadParams.getListener();
        if (listener != null) {
            listener.onProgress(progress);
        }
        LogUtil.i("SingleThreadHandler | downloading allProgress : " + progress);
        sendEmptyMessageDelayed(0, 300);
    }
}
