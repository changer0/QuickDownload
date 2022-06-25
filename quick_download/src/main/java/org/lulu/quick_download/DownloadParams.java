package org.lulu.quick_download;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;

/**
 * 下载参数
 * author: changer0
 * date: 2022/6/25
 */
public class DownloadParams {
    @NonNull
    private final String url;
    @NonNull
    private final File descFile;
    @Nullable
    private DownloadListener listener;

    @NonNull
    public String getUrl() {
        return url;
    }

    @NonNull
    public File getDescFile() {
        return descFile;
    }

    @Nullable
    public DownloadListener getListener() {
        return listener;
    }

    public void setListener(@Nullable DownloadListener listener) {
        this.listener = listener;
    }

    public DownloadParams(@NonNull String url, @NonNull File descFile) {
        this.url = url;
        this.descFile = descFile;
    }

    public DownloadParams(@NonNull String url, @NonNull File descFile, @Nullable DownloadListener listener) {
        this.url = url;
        this.descFile = descFile;
        this.listener = listener;
    }

    public String getUniqueId() {
        return DownloadUtil.getSHA256(url + descFile.getAbsolutePath());
    }
}
