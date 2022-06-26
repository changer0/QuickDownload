package org.lulu.quick_download;

import static java.util.Collections.emptyList;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.startup.Initializer;

import java.util.List;

/**
 * author: changer0
 * date: 2022/6/26
 */
public class QuickDownloadInitializer implements Initializer<QuickDownload> {
    /**
     * App Context
     */
    @SuppressLint("StaticFieldLeak")
    public static Context sContext;

    @NonNull
    @Override
    public QuickDownload create(@NonNull Context context) {
        sContext = context.getApplicationContext();
        return QuickDownload.getInstance();
    }

    @NonNull
    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {
        return emptyList();
    }
}
