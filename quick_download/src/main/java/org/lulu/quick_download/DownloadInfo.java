package org.lulu.quick_download;

import java.util.Arrays;

/**
 * 下载信息
 * author: changer0
 * date: 2022/6/25
 */
public class DownloadInfo {
    /**
     * 文件长度
     */
    public long totalLength;
    /**
     * 是否支持断点续传
     */
    public boolean isSupportSplit;

    /**
     * 下载块
     */
    public volatile DownloadSegment[] segments;

    /**
     * 下载块是否可用
     */
    boolean isSegmentsEnable() {
        return segments != null && segments.length > 0;
    }

    /**
     * 下载是否完成
     */
    boolean isDownloadFinish() {
        for (DownloadSegment segment : segments) {
            if (segment.getState() != DownloadSegment.State.SUCCESS) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "DownloadInfo{" +
                "totalLength=" + totalLength +
                ", isSupportSplit=" + isSupportSplit +
                ", segments=" + Arrays.toString(segments) +
                '}';
    }
}
