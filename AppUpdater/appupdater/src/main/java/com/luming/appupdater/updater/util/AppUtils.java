package com.luming.appupdater.updater.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;



import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import androidx.core.content.FileProvider;

/**
 * 作者：Administrator on 2020/6/5 19:50
 * 邮箱：1055136621@qq.com
 * 描述：
 */
public class AppUtils {
    private static final String MEDIA_TYPE_APK="application/vnd.android.package-archive";
    private static Gson mGson;
    static {
        mGson=new Gson();
    }
    /**
     * 获取本app的版本号
     */
    public static long getAppVesionCode(Context context){
        PackageManager packageManager=context.getPackageManager();
        try {
            PackageInfo packageInfo=packageManager.getPackageInfo(context.getPackageName(),0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return packageInfo.getLongVersionCode();
            }else {
                return packageInfo.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 获取全局的Gson对象
     */
    public static Gson getGlobalGson(){
        if(mGson==null){
            mGson=new Gson();
        }
        return mGson;
    }

    /**
     * 安装apk
     * @param activity
     * @param apkFile
     */
    public static void installApk(Activity activity, File apkFile) {
        if(null==apkFile || !apkFile.exists())
            return;
        Intent intent=new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri=null;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ){
            uri= FileProvider.getUriForFile(activity,activity.getPackageName()+".fileprovider",apkFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }else {
            uri=Uri.fromFile(apkFile);
        }
        intent.setDataAndType(uri,MEDIA_TYPE_APK);
        activity.startActivity(intent);
    }

    /**
     * 获取文件的md5
     * @param targetFile
     * @return
     */
    public static String getMD5FromFile(File targetFile) {
        if(null==targetFile || !targetFile.exists() || !targetFile.isFile())
            return "";
        InputStream inputStream=null;
        try {
            MessageDigest messageDigest=MessageDigest.getInstance("MD5");
            inputStream=new FileInputStream(targetFile);
            byte[] buff=new byte[1024];
            int readingLength=-1;
            while ((readingLength=inputStream.read(buff)) != -1){
                messageDigest.update(buff,0,readingLength);
            }
            byte[] bytes=messageDigest.digest();
            BigInteger bigInteger=new BigInteger(bytes);
            return  bigInteger.toString(16);
        } catch (NoSuchAlgorithmException | FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(inputStream!=null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }
}
