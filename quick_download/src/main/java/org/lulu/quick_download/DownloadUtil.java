package org.lulu.quick_download;

import java.io.Closeable;
import java.io.IOException;

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
}
