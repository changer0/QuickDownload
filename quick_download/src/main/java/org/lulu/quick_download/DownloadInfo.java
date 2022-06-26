package org.lulu.quick_download;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

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
    private long totalLength;

    /**
     * 当前总进度
     */
    @IntRange(from = 0, to = 100)
    private volatile int progress;

    /**
     * 是否支持断点续传
     */
    private boolean supportBreakPointTrans;

    /**
     * 下载块
     */
    private volatile DownloadSegment[] segments;


    public long getTotalLength() {
        return totalLength;
    }

    public void setTotalLength(long totalLength) {
        this.totalLength = totalLength;
    }

    public boolean isSupportBreakPointTrans() {
        return supportBreakPointTrans;
    }

    public void setSupportBreakPointTrans(boolean supportBreakPointTrans) {
        this.supportBreakPointTrans = supportBreakPointTrans;
    }

    public DownloadSegment[] getSegments() {
        return segments;
    }

    public void setSegments(DownloadSegment[] segments) {
        this.segments = segments;
    }

    /**
     * 下载块是否可用
     */
    boolean isSegmentsEnable() {
        return segments != null && segments.length > 0;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    /**
     * 下载是否完成
     */
    boolean isAllSegmentDownloadFinish() {
        for (DownloadSegment segment : segments) {
            if (segment.getState() != DownloadSegment.State.SUCCESS) {
                return false;
            }
        }
        return true;
    }

    @NonNull
    @Override
    public String toString() {
        return "DownloadInfo{" +
                "totalLength=" + totalLength +
                ", supportBreakPointTrans=" + supportBreakPointTrans +
                ", segments=" + Arrays.toString(segments) +
                '}';
    }
}
