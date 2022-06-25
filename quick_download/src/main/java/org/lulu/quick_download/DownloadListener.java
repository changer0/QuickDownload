package org.lulu.quick_download;

import androidx.annotation.NonNull;

/**
 * author: changer0
 * date: 2022/6/25
 */
public interface DownloadListener {

    /**
     * 下载块已准备好
     *
     */
    void onReady(DownloadInfo info);

    /**
     * 下载完成的块, 包括失败的块
     */
    void onSegmentDownloadFinish(DownloadSegment segment);

    /**
     * 所有下载成功
     */
    void onDownloadSuccess();

    /**
     * 下载失败
     */
    void onFailure(@NonNull DownloadSegment segment, Throwable e);

    /**
     * 下载进度
     */
    void onProgress(int progress);
}
