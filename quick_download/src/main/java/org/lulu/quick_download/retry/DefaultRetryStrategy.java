package org.lulu.quick_download.retry;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import org.lulu.quick_download.DownloadSegment;
import org.lulu.quick_download.DownloadTaskDispatcher;
import org.lulu.quick_download.log.LogUtil;

/**
 * author: changer0
 * date: 2022/6/28
 */
public class DefaultRetryStrategy implements IRetryStrategy {
    public static final int RETRY_COUNT = 3;

    /**
     * Segment 下载失败时回调
     *
     * @param taskDispatcher 任务分发器
     * @param segment 正在下载的任务
     * @return 是否消费处理! 返回 true 则表示已经处理, 下载失败不再通知
     */
    @Override
    public boolean handle(DownloadTaskDispatcher taskDispatcher, DownloadSegment segment) {
        int retryCount = segment.getRetryCount();
        retryCount++;
        segment.setRetryCount(retryCount);
        if (retryCount > RETRY_COUNT) {
            return false;
        }
        //重置状态
        segment.setState(DownloadSegment.State.DEFAULT);
        LogUtil.i("segment is retrying retry count:" + retryCount + " segment: " + segment);

        LogUtil.i("retry: segment: " + segment);
        taskDispatcher.startSegmentDownload(segment);

        return true;
    }
}
