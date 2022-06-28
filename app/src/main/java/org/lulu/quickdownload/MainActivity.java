package org.lulu.quickdownload;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.lulu.quick_download.DownloadInfo;
import org.lulu.quick_download.DownloadListener;
import org.lulu.quick_download.DownloadParams;
import org.lulu.quick_download.DownloadSegment;
import org.lulu.quick_download.log.ILogger;
import org.lulu.quick_download.QuickDownload;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private OutPanelController outPanelController;
    private ButtonControllerLinerLayout bottomContainer;

    private final String downloadUrl = "https://ucan.25pp.com/Wandoujia_web_seo_baidu_homepage.apk";
    private File descFile;
    private ProgressBar progressBar;
    private String downloadId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_sample);
        descFile = new File(getExternalCacheDir() + "/" + "test.apk");
        outPanelController = new OutPanelController(findViewById(R.id.svOutPanel),
                findViewById(R.id.tvOutPanel));

        bottomContainer = findViewById(R.id.llControlPanel);

        bottomContainer.addText("下载进度:");
        progressBar = (ProgressBar) bottomContainer.addComponent(R.layout.progress_bar);
        TextView tvThreadCount = bottomContainer.addText("线程数: " + QuickDownload.getInstance().getConfig().getThreadCount());
        SeekBar seekBar = (SeekBar) bottomContainer.addComponent(R.layout.thread_count_seek_bar);

        QuickDownload.getInstance().setConfig(
                QuickDownload.getInstance().getConfig().newBuilder().log(new ILogger() {
                    @Override
                    public void i(String msg) {
                        Log.i(TAG, msg);
                        outPanelController.printlnI(msg);
                    }

                    @Override
                    public void e(String msg, @Nullable Throwable throwable) {
                        Log.e(TAG, msg, throwable);
                        outPanelController.printlnE(msg + " | " + (throwable != null ? throwable.getMessage() : ""));
                    }
                }).build()
        );

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //Toast.makeText(MainActivity.this, "当前线程数: " + progress, Toast.LENGTH_SHORT).show();
                QuickDownload.getInstance().setConfig(
                        QuickDownload.getInstance().getConfig().newBuilder().threadCount(progress).build()
                );
                int threadCount = QuickDownload.getInstance().getConfig().getThreadCount();
                CharSequence msg = "当前线程数: " + threadCount;
                tvThreadCount.setText("线程数: " + threadCount);
                outPanelController.printlnI(msg);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        bottomContainer.addButton("下载", v -> doDownload());
        bottomContainer.addButton("暂停", v -> {
            outPanelController.printlnI(downloadId + " 暂停: " + QuickDownload.getInstance().pauseTaskById(downloadId));
        });
        bottomContainer.addButton("删除文件", v -> {
            outPanelController.printlnI(descFile + " 删除: " + descFile.delete());
        });

        bottomContainer.addButton("测试安装(验证文件正确性)", v -> {
            testInstall();
        });

        bottomContainer.addButton("清除日志", v -> {
            outPanelController.clearPanel();
        });
    }

    private void doDownload() {
        long startTime = System.currentTimeMillis();
        downloadId = QuickDownload.getInstance().addTask(downloadUrl, descFile, new DownloadListener() {
            @Override
            public void onReady(DownloadParams params, DownloadInfo info) {
                outPanelController.printlnI("MainActivity | onReady " + info);
            }

            @Override
            public void onSegmentDownloadSuccess(DownloadSegment segment) {
                outPanelController.printlnI("MainActivity | onSegmentDownloadSuccess " + segment);
            }

            @Override
            public void onSegmentDownloadFailure(DownloadSegment segment, int errorCode, Throwable e) {
                outPanelController.printlnE("MainActivity | onSegmentDownloadFailure " + segment + " errorCode: " + errorCode + " e: " + e.getMessage());
            }

            @Override
            public void onDownloadSuccess() {
                String time = (System.currentTimeMillis() - startTime) / 1000 + "s";
                outPanelController.printlnI("MainActivity | onDownloadSuccess " + time + " size: " + descFile.length());
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "下载完成: " + time + " size: " + descFile.length(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onDownloadFailure(int error, Throwable e) {
                outPanelController.printlnE("MainActivity | onFailure " + e.getMessage());
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
        outPanelController.printlnI("开始下载: " + downloadId);
    }

    public void testInstall() {
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
}