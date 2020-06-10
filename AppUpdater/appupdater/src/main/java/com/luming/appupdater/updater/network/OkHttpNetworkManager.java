package com.luming.appupdater.updater.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.luming.appupdater.updater.network.filter.LoggingInterceptor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 作者：Administrator on 2020/6/5 15:34
 * 邮箱：1055136621@qq.com
 * 描述：使用okhttp框架的请求管理者
 */
public class OkHttpNetworkManager implements INetworkManager {
    private static final String TAG=OkHttpNetworkManager.class.getName();
    private static OkHttpClient okHttpClient;
    static {
        OkHttpClient.Builder clientBuilder=new OkHttpClient.Builder();
        clientBuilder.addInterceptor(new LoggingInterceptor())
                     .connectTimeout(15, TimeUnit.SECONDS);
        okHttpClient=clientBuilder.build();
    }

    private Handler mHandler=new Handler(Looper.getMainLooper());
    @Override
    public void get(String url, final INetworkCallBack callBack,Object tag)  {
        if(url==null || "".equals((url))) return;

        Request request=new Request.Builder().url(url).get().tag(tag).build();
        Call call=okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call,final IOException e) {
                e.printStackTrace();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callBack.fail(e);
                    }
                });

            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            callBack.success(response.body().string());
                        }catch (Exception e){
                            e.printStackTrace();
                            callBack.fail(e);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void download(String url, final File targetFile, final INetworkDownloadCallBack callBack,Object tag) {
        if(url==null || "".equals((url))) return;

        if(!targetFile.exists()){
            targetFile.getParentFile().mkdirs();
        }

        Request request=new Request.Builder().url(url).get().tag(tag).build();
        Call call=okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                e.printStackTrace();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callBack.fail(e);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream inputStream=null;
                OutputStream outputStream=null;
                try {
                    final long fileTotalLong=response.body().contentLength();//文件总长度
                    inputStream=response.body().byteStream();
                    outputStream=new FileOutputStream(targetFile);

                    byte[] buffer=new byte[1024*10];
                    long readedLong=0L;//已读取的长度
                    int buffLong=0;//每次读取的长度

                    while(!call.isCanceled() && (buffLong=inputStream.read(buffer)) != -1){
                        outputStream.write(buffer,0,buffLong);
                        outputStream.flush();

                        readedLong +=buffLong;
                        final  long currentLong=readedLong;
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                int progress=(int)(currentLong * 1.0f / fileTotalLong * 100);
                                callBack.progress(progress);
                            }
                        });
                    }

                    if(call.isCanceled()){
                        return;
                    }
                    targetFile.setExecutable(true,false);
                    targetFile.setReadable(true,false);
                    targetFile.setWritable(true,false);

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callBack.success(targetFile);
                        }
                    });
                } catch (final IOException e) {
                    if(call.isCanceled()){
                        return;
                    }
                    e.printStackTrace();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callBack.fail(e);
                        }
                    });
                }finally {
                    if(inputStream!=null)
                        inputStream.close();
                    if(outputStream!=null)
                        outputStream.close();
                }
            }
        });
    }

    @Override
    public void cancel(Object tag) {
        List<Call> queuedCalls=okHttpClient.dispatcher().queuedCalls();//排队中的call
        if(queuedCalls!=null && !queuedCalls.isEmpty()){
            for (Call call:queuedCalls) {
                if(tag.equals(call.request().tag())){
                    Log.d(TAG,"在queue中查找到的call："+call);
                    call.cancel();
                }
            }
        }
        List<Call> runningCalls=okHttpClient.dispatcher().runningCalls();//运行中的call
        if(runningCalls!=null && !runningCalls.isEmpty()){
            for (Call call:runningCalls) {
                if(tag.equals(call.request().tag())){
                    Log.d(TAG,"在runningCall中查找到的call："+call);
                    call.cancel();
                }
            }
        }
    }

}
