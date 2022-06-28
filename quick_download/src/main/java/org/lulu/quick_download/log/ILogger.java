package org.lulu.quick_download.log;

import androidx.annotation.Nullable;

/**
 * author: changer0
 * date: 2022/6/25
 */
public interface ILogger {
    void i(String msg);
    void e(String msg, @Nullable Throwable throwable);
}
