package org.lulu.quick_download;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.lulu.quick_download.db.DownloadDBHandle;
import org.lulu.quick_download.db.SegmentInfo;
import org.lulu.quick_download.log.LogUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 下载块任务
 * author: changer0
 * date: 2022/6/25
 */
public class DownloadSegmentTask implements Runnable {

    @NonNull
    private final DownloadSegment segment;
    @NonNull
    private final DownloadParams params;
    @NonNull
    private final OkHttpClient client;
    @Nullable
    private DownloadSegmentListener listener;
    /**
     * 开关
     */
    private volatile boolean on = true;

    public DownloadSegmentTask(@NonNull DownloadParams downloadParams, @NonNull DownloadSegment segment) {
        this.segment = segment;
        this.params = downloadParams;
        this.client = QuickDownload.getInstance().getConfig().getOkHttpClient();
    }

    @Override
    public void run() {
        if (segment.getState() == DownloadSegment.State.DOWNLOADING
                || segment.getState() == DownloadSegment.State.SUCCESS) {
            LogUtil.i("segment " + segment.getIndex() + " " + "is downloading or succeed!");
            return;
        }
        LogUtil.i("download segment index=" + segment.getIndex() + " startPos=" + segment.getStartPos());
        //long startPos = segment.getStartPos();
        String splitRangeHeader = generateSplitRangeHeader(segment);
        //Log.d("当前的 Range: " + splitRangeHeader);
        Response response = null;
        try {
            segment.setState(DownloadSegment.State.DOWNLOADING);
            Request.Builder builder = new Request.Builder()
                    .url(Objects.requireNonNull(params.getUrl()))
                    .addHeader(DownloadConstants.HEADER_RANGE, splitRangeHeader)
                    .get();
            response = client.newCall(builder.build()).execute();
            ResponseBody body = response.body();
            if (body == null) {
                segment.setState(DownloadSegment.State.FAILURE);
                notifyFailure(DownloadConstants.ERROR_CODE_UNKNOWN, new Exception("DownloadSegmentTask body == null"));
                return;
            }
            InputStream in = body.byteStream();
            writeSegmentToFile(segment, in);
        } catch (IOException e) {
            segment.setState(DownloadSegment.State.FAILURE);
            notifyFailure(DownloadConstants.ERROR_CODE_UNKNOWN, e);
            e.printStackTrace();
        } finally {
            DownloadUtil.close(response);
        }
    }

    private String generateSplitRangeHeader(DownloadSegment segment) {
        //起始位置 + 已经下载的进度!
        return "bytes=" + getStartWithDownload(segment) + "-" + segment.getEndPos();
    }

    private void writeSegmentToFile(DownloadSegment segment, InputStream in) throws IOException {
        RandomAccessFile raFile = DownloadUtil.prepareDownloadFile(params.getDescFile(), getStartWithDownload(segment));
        if (raFile == null) {
            throw new IOException("Create RandomAccessFile Failure");
        }
        byte[] buffer = new byte[DownloadConstants.BUFFER_SIZE];
        long segmentLen = segment.getDownloadLength();
        int len;
        try {
            while ((len = in.read(buffer, 0, DownloadConstants.BUFFER_SIZE)) > 0) {
                if (!on) {
                    notifyFailure(DownloadConstants.ERROR_CODE_CANCEL, new RuntimeException("segment " + segment.getIndex() + " cancel!"));
                    return;
                }
                segmentLen += len;
                raFile.write(buffer, 0, len);
                segment.setDownloadLength(segmentLen);
            }
            LogUtil.i("writing segment: " + segment.getIndex() + " length" + segment.getLength() + " startPos:" + segment
                    .getStartPos());
            segment.setState(DownloadSegment.State.SUCCESS);
            notifySuccess();
        } finally {
            DownloadUtil.close(raFile);
        }
    }

    /**
     * 包含已经下载过的
     */
    private long getStartWithDownload(DownloadSegment segment) {
        return segment.getStartPos() + segment.getDownloadLength();
    }

    public void cancel() {
        on = false;
    }

    private void notifySuccess() {
        if (listener == null) {
            return;
        }
        listener.onSuccess(segment);
    }


    private void notifyFailure(int errorCode, Throwable e) {
        if (listener == null) {
            return;
        }
        listener.onFailure(segment, errorCode, e);
    }

    public void setListener(@Nullable DownloadSegmentListener listener) {
        this.listener = listener;
    }

    public interface DownloadSegmentListener {
        void onSuccess(DownloadSegment segment);
        void onFailure(DownloadSegment segment, int errorCode, Throwable e);
    }
}
