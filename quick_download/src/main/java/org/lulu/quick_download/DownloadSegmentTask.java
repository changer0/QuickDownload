package org.lulu.quick_download;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
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

    private static final int BUFFER_SIZE = 20 * 1024;

    private static final String RANGE = "Range";
    @NonNull
    private final DownloadSegment segment;
    @NonNull
    private final DownloadParams params;
    @NonNull
    private final OkHttpClient client;

    @Nullable
    private DownloadSegmentListener listener;

    public DownloadSegmentTask(@NonNull DownloadParams downloadParams, @NonNull DownloadSegment segment) {
        this.segment = segment;
        this.params = downloadParams;
        this.client = QuickDownload.getInstance().getConfig().getOkHttpClient();
    }

    @Override
    public void run() {
        if (segment.getState() == DownloadSegment.State.DOWNLOADING
                || segment.getState() == DownloadSegment.State.SUCCESS) {
            LogUtil.i("downloading or succeed!");
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
                    .addHeader(RANGE, splitRangeHeader)
                    .get();
            response = client.newCall(builder.build()).execute();
            ResponseBody body = response.body();
            if (body == null) {
                segment.setState(DownloadSegment.State.FAILURE);
                notifyFailure(new Exception("DownloadSegmentTask body == null"));
                return;
            }
            InputStream in = body.byteStream();
            writeSegmentToFile(segment, in);
        } catch (IOException e) {
            segment.setState(DownloadSegment.State.FAILURE);
            notifyFailure(e);
            e.printStackTrace();
        } finally {
            if (response != null) {
                response.close();
            }
        }

    }

    private String generateSplitRangeHeader(DownloadSegment segment) {
        return "bytes=" + segment.getStartPos() + "-" + segment.getEndPos();
    }

    private void writeSegmentToFile(DownloadSegment segment, InputStream in) throws IOException {
        RandomAccessFile raFile = prepareDownloadFile(segment.getStartPos());
        if (raFile == null) {
            throw new IOException("Create RandomAccessFile Failure");
        }
        byte[] buffer = new byte[BUFFER_SIZE];
        long segmentLen = 0;
        int len;
        while ((len = in.read(buffer, 0, BUFFER_SIZE)) > 0) {
            segmentLen += len;
            raFile.write(buffer, 0, len);
            int progress = (int) (segmentLen * 100.0F / segment.getLength()) + 1;
            if (progress > 100) {
                progress = 100;
            }
            //LogUtil.i("writing segment progress: " + progress);
            segment.setProgress(progress);
        }
        DownloadUtil.close(raFile);
        LogUtil.i("writing segment: " + segment.getIndex() + " length" + segment.getLength() + " startPos:" + segment
                .getStartPos());
        segment.setState(DownloadSegment.State.SUCCESS);
        notifySuccess();
    }



    /**
     * 准备下载文件
     */
    @Nullable
    private synchronized RandomAccessFile prepareDownloadFile(long pos) {
        boolean createOK = true;
        RandomAccessFile raFile = null;
        try {
            File saveFile = params.getDescFile();
            File dirFile = saveFile.getParentFile();
            if (dirFile != null && !dirFile.exists()) {
                createOK = dirFile.mkdirs();
            }
            if (!saveFile.exists()) {
                createOK = saveFile.createNewFile();
            }
            if (createOK) {
                raFile = new RandomAccessFile(saveFile, "rwd");
                raFile.seek(pos);
                //raFile.setLength(downloadInfo.totalLength);
            }
        } catch (Exception e) {
            raFile = null;
            e.printStackTrace();
        }
        return raFile;
    }

    private void notifySuccess() {
        if (listener == null) {
            return;
        }
        listener.onSuccess(segment);
    }


    private void notifyFailure(Throwable e) {
        if (listener == null) {
            return;
        }
        listener.onFailure(segment, e);
    }

    public void setListener(@Nullable DownloadSegmentListener listener) {
        this.listener = listener;
    }

    public interface DownloadSegmentListener {
        void onSuccess(DownloadSegment segment);
        void onFailure(DownloadSegment segment, Throwable e);
    }

}
