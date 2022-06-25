package org.lulu.quick_download;


import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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
     * 进度
     */
    private volatile int progress;


    public long getLength() {
        return endPos - startPos + 1;
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

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    @NonNull
    @Override
    public String toString() {
        return "DownloadSegment{"
                + "state=" + state
                + ", index=" + index
                + ", startPos=" + startPos
                + ", length=" + getLength() + '}';
    }
}
