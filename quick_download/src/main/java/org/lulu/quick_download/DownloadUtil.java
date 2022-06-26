package org.lulu.quick_download;

import android.content.Context;
import android.content.pm.PackageInfo;

import androidx.annotation.Nullable;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.security.MessageDigest;

import okhttp3.ResponseBody;

/**
 * author: changer0
 * date: 2022/6/25
 */
public class DownloadUtil {

    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 准备下载文件
     */
    @Nullable
    public static synchronized RandomAccessFile prepareDownloadFile(File saveFile, long seekPos) {
        boolean createOK = true;
        RandomAccessFile raFile = null;
        try {
            File dirFile = saveFile.getParentFile();
            if (dirFile != null && !dirFile.exists()) {
                createOK = dirFile.mkdirs();
            }
            if (!saveFile.exists()) {
                createOK = saveFile.createNewFile();
            }
            if (createOK) {
                raFile = new RandomAccessFile(saveFile, "rwd");
                raFile.seek(seekPos);
                //raFile.setLength(downloadInfo.totalLength);
            }
        } catch (Exception e) {
            raFile = null;
            e.printStackTrace();
        }
        return raFile;
    }

    /**
     * 直接下载, 不开启线程
     */
    public static void directDownload(ResponseBody body, DownloadParams params, DownloadInfo downloadInfo) throws IOException {
        RandomAccessFile raFile = DownloadUtil.prepareDownloadFile(params.getDescFile(), 0);
        if (raFile == null) {
            throw new IOException("Create RandomAccessFile Failure");
        }
        if (body != null) {
            InputStream in = body.byteStream();
            byte[] buffer = new byte[DownloadConstants.BUFFER_SIZE];
            int n;
            int downloadLen = 0;
            while ((n = in.read(buffer, 0, DownloadConstants.BUFFER_SIZE)) > 0) {
                raFile.write(buffer, 0, n);
                downloadLen += n;
                downloadInfo.setProgress((int) (downloadLen * 100.0f / downloadInfo.getTotalLength()));
            }
        }
        DownloadUtil.close(raFile);
        DownloadUtil.close(body);
    }

    public static int getVersionCode(Context context){
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }
}
