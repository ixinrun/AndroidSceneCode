package com.example.updateversion2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by BigRun on 2016/6/12.
 */
public class UpdateVersionManager {
    private static final String UPDATEVERSIONPATH = "http://121.42.208.228:1234/api/v1/customer/get/android/version";
    private Context mContext;
    private boolean mIsAutoUpdate;

    private String mVersionName;
    private String mVersionDetial;
    private String mDownLoadUrl;
    private String mSpecialNote;

    private static final int UPDATE_FALSE = 0;        //没有更新
    private static final int UPDATE_TRUE = 1;         //有更新

    // 线程+Handler刷新UI
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch ((Integer) msg.obj) {
                case UPDATE_FALSE:
                    File dirFile = new File(UpdateVersionService.FILESAVEPATH);
                    if (dirFile.exists()){
                        UpdateVersionUtil.deleteFileOrDir(dirFile);
                    }
                    if (!mIsAutoUpdate) {
                        Toast.makeText(mContext, "当前已是最新版本", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case UPDATE_TRUE:
                    if (!TextUtils.isEmpty(mDownLoadUrl)) {
                        String appName = UpdateVersionUtil.MD5(mDownLoadUrl)+".apk";
                        File file = new File(UpdateVersionService.FILESAVEPATH, appName);
                        showUpdateDialog(file);// 显示更新对话框
                    }
                    break;
            }
        }
    };

    /**
     * 构造器，初始化上下文，参数。
     *
     * @param context
     * @param isAutoUpdate
     */
    public UpdateVersionManager(Context context, boolean isAutoUpdate) {
        this.mContext = context;
        this.mIsAutoUpdate = isAutoUpdate;
    }

    public void checkUpdate() {
        if (!UpdateVersionService.isDownLoading) {
            new CheckUpdateThread().start();
        }
    }

    /**
     * 检查更新子线程
     *
     * @return
     */
    private class CheckUpdateThread extends Thread {
        @Override
        public void run() {
            super.run();
            int nowVersionCode = getVersionCode(mContext);
            HttpURLConnection conn = null;
            InputStream is = null;
            ByteArrayOutputStream baos = null;
            try {
                URL url = new URL(UPDATEVERSIONPATH);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10 * 1000);    //设置访问网络超时时间
                conn.setReadTimeout(5 * 1000);       //设置读取数据超时时间
                conn.setRequestMethod("GET");       //设置访问方式
                //开始连接,调用此方法就不必再使用conn.connect()方法
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    is = conn.getInputStream();              //获取一个输入流来读取网络二进制流信息
                    baos = new ByteArrayOutputStream();      //得到一个字节输出流。
                    byte[] buffer = new byte[2 * 1024];      //创建一个字节缓冲区。
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        baos.write(buffer, 0, len);
                    }
                    String response = baos.toString();
                    JSONObject jsonObject = new JSONObject(response);
                    int code = jsonObject.getInt("code");
                    String message = jsonObject.getString("message");
                    if (200 == code) {
                        JSONObject data = jsonObject.getJSONObject("data");
                        int versionCode = data.getInt("version_code");
                        mVersionName = data.getString("version_name");
                        mVersionDetial = data.getString("version_detail");
                        mDownLoadUrl = data.getString("download_url");
                        mSpecialNote = data.getString("special_note");
                        Message msg = new Message();
                        if (nowVersionCode < versionCode) {
                            msg.obj = UPDATE_TRUE;
                        } else {
                            msg.obj = UPDATE_FALSE;
                        }
                        handler.sendMessage(msg);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                //从里到外，从上到下，逐个关闭
                try {
                    if (baos != null)
                        baos.close(); //关闭字节输出流
                    if (is != null)
                        is.close();  //关闭输入流
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (conn != null) {
                    conn.disconnect();  //关闭连接
                }
            }
        }
    }

    /**
     * 获取当前程序的版本号
     *
     * @param context
     * @return
     */
    private int getVersionCode(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    //显示更新对话框
    private void showUpdateDialog(final File apkFile) {
        View updateDialogView = LayoutInflater.from(mContext).inflate(R.layout.updateversion_dialog, null);
        final Dialog updateDialog = new AlertDialog.Builder(mContext)
                .setView(updateDialogView)
                .show();
        TextView versionDetial = (TextView) updateDialogView.findViewById(R.id.version_detial);
        versionDetial.setText(mVersionDetial);
        Button updateBt = (Button) updateDialogView.findViewById(R.id.update_bt);
        if (apkFile.exists()) {
            updateBt.setText("立即安装");
        } else {
            updateBt.setText("立即下载");
        }
        updateBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDialog.dismiss();
                if (apkFile.exists()) {
                    UpdateVersionUtil.installAPK(mContext, apkFile);
                } else {
                    Intent intent = new Intent(mContext, UpdateVersionService.class);
                    intent.putExtra(UpdateVersionService.INTENT_DOWNLOAD_URL, mDownLoadUrl);
                    mContext.startService(intent);
                }
            }
        });
    }
}
