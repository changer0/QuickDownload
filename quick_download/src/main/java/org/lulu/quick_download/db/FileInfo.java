package org.lulu.quick_download.db;

/**
 * author: changer0
 * date: 2022/6/26
 */
public class FileInfo {
    private String id;
    private long length;
    /**
     * 1 完成
     */
    private int status;

    public FileInfo(String id, long length, int status) {
        this.id = id;
        this.length = length;
        this.status = status;
    }

    public FileInfo(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "id='" + id + '\'' +
                ", length=" + length +
                ", status=" + status +
                '}';
    }
}
