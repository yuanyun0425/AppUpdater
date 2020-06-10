package com.luming.appupdater.updater.network;

import java.io.File;

/**
 * 作者：Administrator on 2020/6/5 15:23
 * 邮箱：1055136621@qq.com
 * 描述：文件下载的回调接口
 */
public interface INetworkDownloadCallBack {
    void success(File targetFile);
    void progress(int progress);
    void fail(Throwable throwable);
}
