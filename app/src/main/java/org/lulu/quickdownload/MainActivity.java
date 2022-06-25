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
import android.widget.Toast;

import org.lulu.quick_download.DownloadInfo;
import org.lulu.quick_download.DownloadListener;
import org.lulu.quick_download.DownloadParams;
import org.lulu.quick_download.DownloadSegment;
import org.lulu.quick_download.LogUtil;
import org.lulu.quick_download.QuickDownload;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private final String downloadUrl = "https://ucan.25pp.com/Wandoujia_web_seo_baidu_homepage.apk";
    private File descFile;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progress_bar);
        descFile = new File(getExternalCacheDir() + "/" + "test.apk");
        progressBar.setMax(100);
    }

    public void clickButton1(View view) {
        //Toast.makeText(this, "当前 CPU 个数:" + Runtime.getRuntime().availableProcessors(), Toast.LENGTH_SHORT).show();
        long startTime = System.currentTimeMillis();
        QuickDownload.getInstance().addTask(downloadUrl, descFile, new DownloadListener() {
            @Override
            public void onReady(DownloadParams params, DownloadInfo info) {
                LogUtil.i("MainActivity | onReady " + info);
            }

            @Override
            public void onSegmentDownloadFinish(DownloadSegment segment) {
                LogUtil.i("MainActivity | onSegmentDownloadFinish " + segment);
            }

            @Override
            public void onDownloadSuccess() {
                String time = (System.currentTimeMillis() - startTime) / 1000 + "s";
                LogUtil.i("MainActivity | onDownloadSuccess " + time);
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "下载完成: " + time, Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onDownloadFailure(Throwable e) {
                LogUtil.i("MainActivity | onFailure " + Log.getStackTraceString(e));
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "下载失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onProgress(int progress) {
                LogUtil.i("MainActivity | onProgress " + progress);
                runOnUiThread(() -> {
                    progressBar.setProgress(progress);
                });
            }
        });
    }

    public void clickButton2(View view) {
        installApk();
    }


    private void installApk() {
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

    public void clickButton3(View view) {
        Toast.makeText(this, "删除完成:" + descFile.delete(), Toast.LENGTH_SHORT).show();
    }
}