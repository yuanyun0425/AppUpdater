package com.luming.appupdater.updater.network;

/**
 * 作者：Administrator on 2020/6/5 15:23
 * 邮箱：1055136621@qq.com
 * 描述：网络请求的回调接口
 */
public interface INetworkCallBack {
    void success(String response);
    void fail(Throwable throwable);
}
