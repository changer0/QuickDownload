package org.lulu.quickdownload;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import org.lulu.quick_download.DownloadInfo;
import org.lulu.quick_download.DownloadListener;
import org.lulu.quick_download.DownloadParams;
import org.lulu.quick_download.DownloadSegment;
import org.lulu.quick_download.QuickDownload;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private final String downloadUrl = "https://ucan.25pp.com/Wandoujia_web_seo_baidu_homepage.apk";
    private File descFile;
    private ProgressBar progressBar;
    private SeekBar seekBar;
    private String downloadId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progress_bar);
        seekBar = findViewById(R.id.thread_count);
        descFile = new File(getExternalCacheDir() + "/" + "test.apk");
        progressBar.setMax(100);
        seekBar.setMax(10);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Toast.makeText(MainActivity.this, "当前线程数: " + progress, Toast.LENGTH_SHORT).show();
                QuickDownload.getInstance().setConfig(
                        QuickDownload.getInstance().getConfig().newBuilder().threadCount(progress).build()
                );
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    public void multiThreadDownload(View view) {
        //Toast.makeText(this, "当前 CPU 个数:" + Runtime.getRuntime().availableProcessors(), Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "开始下载, 线程数: " + QuickDownload.getInstance().getConfig().getThreadCount(), Toast.LENGTH_SHORT).show();
        doDownload(true);
    }

    public void singleThreadDownload(View view) {
        doDownload(false);
    }

    private void doDownload(boolean useMultiThread) {
        long startTime = System.currentTimeMillis();
        downloadId = QuickDownload.getInstance().addTask(downloadUrl, descFile, useMultiThread, new DownloadListener() {
            @Override
            public void onReady(DownloadParams params, DownloadInfo info) {
                Log.i(TAG, "MainActivity | onReady " + info);
            }

            @Override
            public void onSegmentDownloadFinish(DownloadSegment segment) {
                Log.i(TAG,"MainActivity | onSegmentDownloadFinish " + segment);
            }

            @Override
            public void onDownloadSuccess() {
                String time = (System.currentTimeMillis() - startTime) / 1000 + "s";
                Log.i(TAG,"MainActivity | onDownloadSuccess " + time);
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "下载完成: " + time, Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onDownloadFailure(int error, Throwable e) {
                Log.i(TAG,"MainActivity | onFailure " + Log.getStackTraceString(e));
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "下载失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onProgress(int progress) {
                Log.i(TAG,"MainActivity | onProgress " + progress);
                runOnUiThread(() -> {
                    progressBar.setProgress(progress);
                });
            }
        });
    }

    public void testInstall(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // 7.0+以上版本
            Uri apkUri = FileProvider.getUriForFile(this, "org.lulu.quick_download.fileprovider", descFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(descFile), "application/vnd.android.package-archive");
        }
        this.startActivity(intent);
    }

    public void removeFile(View view) {
        Toast.makeText(this, "删除完成:" + descFile.delete(), Toast.LENGTH_SHORT).show();
    }

    public void pauseDownload(View view) {
        Toast.makeText(this, "暂停: " + QuickDownload.getInstance().pauseTaskById(downloadId), Toast.LENGTH_SHORT).show();
    }
}