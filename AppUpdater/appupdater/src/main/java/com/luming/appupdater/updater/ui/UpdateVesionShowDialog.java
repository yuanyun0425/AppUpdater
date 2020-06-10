package com.luming.appupdater.updater.ui;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.luming.appupdater.R;
import com.luming.appupdater.updater.AppUpdater;
import com.luming.appupdater.updater.network.INetworkDownloadCallBack;
import com.luming.appupdater.updater.util.AppUtils;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

/**
 * 作者：Administrator on 2020/6/5 21:13
 * 邮箱：1055136621@qq.com
 * 描述：版本更新的弹框
 */
public class UpdateVesionShowDialog extends DialogFragment {
    private static final String TAG="UpdateVesionShowDialog";
   private static final String KEY_VERSION_INFO="RemoteVersion";
   private JsonObject vInfoObj;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments=getArguments();
        if(arguments!=null){
            String versionInfo=arguments.getString(KEY_VERSION_INFO);
            vInfoObj=AppUtils.getGlobalGson().fromJson(versionInfo,JsonObject.class);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View dialogView=inflater.inflate(R.layout.dialog_update_version,container,false);
        TextView vesionTitle=dialogView.findViewById(R.id.tv_version_title);
        TextView vesionTips=dialogView.findViewById(R.id.tv_version_tips);
        final Button btnUpdate=dialogView.findViewById(R.id.btn_update);
        vesionTitle.setText(vInfoObj.get("title").getAsString());
        vesionTips.setText(vInfoObj.get("content").getAsString());

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                view.setEnabled(false);
                //4、下载
                String apkUrl=vInfoObj.get("url").getAsString();
                String apkName="target.apk";
                if(apkUrl!=null && apkUrl.contains("/")){
                    apkName=apkUrl.substring(apkUrl.lastIndexOf("/"));
                }
                File installApk=new File(getActivity().getCacheDir(),apkName);
                //判断该apk是否存在，如存在，比对MD5；MD5不一致时，才去下载文件
                if(installApk.exists()){
                    if(AppUtils.getMD5FromFile(installApk).equals(vInfoObj.get("md5").getAsString())){
                        Log.d(TAG,String.format("%s 文件已存在，无需下载",installApk.getAbsoluteFile()));
                        dismiss();
                        //安装apk
                        if(getActivity()!=null){
                            AppUtils.installApk(getActivity(),installApk);
                        }
                        return;
                    }
                }

                AppUpdater.newInstance().getNetworkManager().download(apkUrl,installApk, new INetworkDownloadCallBack() {
                    @Override
                    public void success(File targetFile) {
                        view.setEnabled(true);
                        Log.d(TAG,String.format("%s 文件下载成功",targetFile.getAbsoluteFile()));
                        dismiss();
                        //对比MD5
                        String targetFile_MD5=AppUtils.getMD5FromFile(targetFile);
                        Log.d(TAG,"下载的文件的MD5："+targetFile_MD5);
                        if(!targetFile_MD5.equals(vInfoObj.get("md5").getAsString())){
                            Log.d(TAG,"文件被修改了！");
                            return;
                        }
                        //安装apk
                        if(getActivity()!=null){
                            AppUtils.installApk(getActivity(),targetFile);
                        }
                    }

                    @Override
                    public void progress(int progress) {
                        //下载进度
                        Log.d(TAG,String.format("下载中... %s %%",progress));
                        btnUpdate.setText(progress+"%");
                    }

                    @Override
                    public void fail(Throwable throwable) {
                        view.setEnabled(true);
                        //下载失败
                        throwable.printStackTrace();
                        Toast.makeText(getActivity(),"下载失败",Toast.LENGTH_LONG).show();
                        dismiss();
                    }
                },UpdateVesionShowDialog.this);
            }
        });
        return dialogView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d(TAG,"onDismiss");
        AppUpdater.newInstance().getNetworkManager().cancel(this);
    }


    /*
                        对外提供显示弹框的方法
                         */
    public static void show(FragmentActivity activity, JsonObject jsonObject){
        Bundle bundle=new Bundle();
        bundle.putString(KEY_VERSION_INFO, AppUtils.getGlobalGson().toJson(jsonObject));
        UpdateVesionShowDialog dialog=new UpdateVesionShowDialog();
        dialog.setArguments(bundle);
        dialog.show(activity.getSupportFragmentManager(),"updateVersionShowDialog");
    }
}
