package org.lulu.quick_download;

/**
 * author: changer0
 * date: 2022/6/25
 */
public class LogUtil {
    public static void i(String msg) {
        QuickDownload.getInstance().getConfig().getLogger().i(msg);
    }

    public static void e(String msg) {
        QuickDownload.getInstance().getConfig().getLogger().e(msg, null);
    }
}
