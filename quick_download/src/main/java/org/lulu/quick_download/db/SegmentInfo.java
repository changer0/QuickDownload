package org.lulu.quick_download.db;

import org.lulu.quick_download.DownloadSegment;

/**
 * author: changer0
 * date: 2022/6/27
 */
public class SegmentInfo {
    private String id;
    private String downloadId;
    private long downloadPos;
    private int index;
    private int finished;

    public SegmentInfo(String id, String downloadId, long downloadPos, int index, int state) {
        this.id = id;
        this.downloadId = downloadId;
        this.downloadPos = downloadPos;
        this.index = index;
        this.finished = state;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(String downloadId) {
        this.downloadId = downloadId;
    }

    public long getDownloadPos() {
        return downloadPos;
    }

    public void setDownloadPos(long downloadPos) {
        this.downloadPos = downloadPos;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getFinished() {
        return finished;
    }

    public void setFinished(int finished) {
        this.finished = finished;
    }

    public static SegmentInfo newSegmentInfo(DownloadSegment downloadSegment) {
        return new SegmentInfo(
                downloadSegment.getSegmentId(),
                downloadSegment.getDownloadId(),
                downloadSegment.getDownloadLength(),
                downloadSegment.getIndex(),
                downloadSegment.getState() == DownloadSegment.State.SUCCESS ? 1 : 0
        );
    }
}
