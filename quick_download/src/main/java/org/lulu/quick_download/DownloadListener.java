package org.lulu.quick_download;

/**
 * author: changer0
 * date: 2022/6/25
 */
public interface DownloadListener {

    /**
     * 下载块已准备好
     *
     */
    default void onReady(DownloadParams params, DownloadInfo info) {

    }

    /**
     * 下载完成的块, 包括失败的块
     */
    default void onSegmentDownloadFinish(DownloadSegment segment) {

    }

    /**
     * 所有下载成功
     */
    void onDownloadSuccess();

    /**
     * 下载失败
     */
    void onDownloadFailure(Throwable e);

    /**
     * 下载进度
     */
    void onProgress(int progress);
}
