package org.lulu.quick_download;

/**
 * author: changer0
 * date: 2022/6/25
 */
public interface DownloadListener {

    /**
     * 下载信息备好
     *
     * 如果是多线程下载此时已完成分片
     *
     */
    default void onReady(DownloadParams params, DownloadInfo info) {

    }

    /**
     * 下载块成功
     */
    default void onSegmentDownloadSuccess(DownloadSegment segment) {

    }

    /**
     * 下载块失败
     */
    default void onSegmentDownloadFailure(DownloadSegment segment, int errorCode, Throwable e) {

    }

    /**
     * 下载进度
     */
    default void onProgress(int progress) {

    }

    /**
     * 所有下载成功
     */
    void onDownloadSuccess();

    /**
     * 下载失败
     */
    void onDownloadFailure(int errorCode, Throwable e);
}
