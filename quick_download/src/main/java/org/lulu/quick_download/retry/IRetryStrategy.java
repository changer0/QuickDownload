package org.lulu.quick_download.retry;

import org.lulu.quick_download.DownloadSegment;
import org.lulu.quick_download.DownloadTaskDispatcher;

/**
 * 重试策略
 *
 * author: changer0
 * date: 2022/6/28
 */
public interface IRetryStrategy {
    /**
     * @param taskDispatcher 任务分发器
     * @param segment 正在下载的任务
     * @return 是否消费处理! 返回 true 则表示已经处理, 下载失败不再通知
     */
    boolean  handle(DownloadTaskDispatcher taskDispatcher, DownloadSegment segment);
}
