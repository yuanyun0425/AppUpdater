package com.luming.appupdater.updater;

import android.os.Bundle;

import com.luming.appupdater.updater.network.INetworkManager;
import com.luming.appupdater.updater.network.OkHttpNetworkManager;

/**
 * 作者：Administrator on 2020/6/5 15:08
 * 邮箱：1055136621@qq.com
 * 描述：升级管理者
 */
public class AppUpdater {
    private static AppUpdater instance=new AppUpdater();
    private static  INetworkManager mNetworkManager;


    private AppUpdater(){
    }

    public static AppUpdater newInstance() {
        //设置默认的网络请求管理者为OkHttpNetworkManager
        mNetworkManager=new OkHttpNetworkManager();
        return instance;
    }

    public INetworkManager getNetworkManager() {
        return mNetworkManager;
    }

    public void setNetworkManager(INetworkManager mNetworkManager) {
        this.mNetworkManager = mNetworkManager;
    }
}
