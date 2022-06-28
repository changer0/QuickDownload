package org.lulu.quick_download;


import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

/**
 * 下载块
 * author: changer0
 * date: 2022/6/25
 */
public class DownloadSegment {

    @Retention(value = RetentionPolicy.SOURCE)
    @IntDef({State.DEFAULT, State.DOWNLOADING, State.SUCCESS, State.FAILURE})
    public @interface State {
        int DEFAULT = 0;
        int DOWNLOADING = 1;
        int SUCCESS = 2;
        int FAILURE = 3;
    }

    /**
     * 下载 Id
     */
    private String downloadId;

    /**
     * 当前的下载状态
     */
    private int state;

    /**
     * 索引
     */
    private int index;

    /**
     * 当前块的起始点
     */
    private long startPos;

    /**
     * 当前块的结束位置
     */
    private long endPos;

    /**
     * 当前块下载长度
     */
    private volatile long downloadLength;

    /**
     * 重试次数
     */
    private int retryCount = 0;

    public DownloadSegment(String downloadId, int index) {
        this.downloadId = downloadId;
        this.index = index;
    }

    public long getStartPos() {
        return startPos;
    }

    public void setStartPos(long startPos) {
        this.startPos = startPos;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @State
    public int getState() {
        return state;
    }

    public void setState(@State int state) {
        this.state = state;
    }

    public long getEndPos() {
        return endPos;
    }

    public void setEndPos(long endPos) {
        this.endPos = endPos;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }


    public long getDownloadLength() {
        return downloadLength;
    }

    public void setDownloadLength(long downloadLength) {
        this.downloadLength = downloadLength;
    }

    public String getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(String downloadId) {
        this.downloadId = downloadId;
    }

    public String getSegmentId() {
        return getDownloadId() + "_" + getIndex();
    }

    @IntRange(from = 0, to = 100)
    public int getProgress() {
        int progress = (int) (getDownloadLength() * 100.0F / getLength()) + 1;
        if (progress > 100) {
            progress = 100;
        }
        return progress;
    }


    public long getLength() {
        return endPos - startPos + 1;
    }

    @NonNull
    @Override
    public String toString() {
        return "DownloadSegment{" +
                "state=" + state +
                ", index=" + index +
                ", startPos=" + startPos +
                ", endPos=" + endPos +
                ", downloadLength=" + downloadLength +
                ", retryCount=" + retryCount +
                '}';
    }
}
