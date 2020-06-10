package com.luming.appupdater.updater.network;

import java.io.File;

/**
 * 作者：Administrator on 2020/6/5 15:16
 * 邮箱：1055136621@qq.com
 * 描述：
 */
public interface INetworkManager {
    /**
     * get请求
     * @param url 请求url
     * @param callBack  回调
     */
    void get(String url,INetworkCallBack callBack,Object tag);

    /**
     * 下载文件
     * @param url  文件下载url
     * @param targetFile  下载的文件存放文件
     * @param callBack  回调
     */
    void download(String url, File targetFile,INetworkDownloadCallBack callBack,Object tag);

    /**
     * 终止请求
     */
    void cancel(Object tag);
}
