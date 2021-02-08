package com.example.updateversion2;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 1.下载之前清空版本库文件夹
 * 2.下次获取版本信息的后检测本地时候已经下载了新版本。
 * 3.每次下载成功前取下载地址的MD5值命名，下载成功重命名。
 *
 * Created by BigRun on 2016/6/12.
 */
public class UpdateVersionService extends Service {
    private NotificationManager notificationManager;
    private Notification.Builder builder;
    private Notification notification;

    private static final int DOWN_LOAD = 0;           //正在下载
    private static final int DOWN_SUCCEED = 1;        //下载成功
    private static final int DOWN_FINISH = 2;         //下载结束
    private static final int NOTIFICATION_ID = 100;   //通知栏唯一标示
    public static final String INTENT_DOWNLOAD_URL = "INTENT_DOWNLOAD_URL";

    private String mDownLoadUrl;
    public static final String FILESAVEPATH = Environment.getExternalStorageDirectory() + "/UpdateVersion/LastVersion";
    private String mDownloadAppName, mDownSucceedAppName;

    private static final int PROGRESS_MEX = 100;
    private int mNowProgress;

    public static boolean isDownLoading;

    //线程+Handler刷新UI
    private Handler handler = new Handler() {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch ((Integer) msg.obj) {
                case DOWN_LOAD:
                    builder.setProgress(PROGRESS_MEX, mNowProgress, false)
                            .setContentText(mNowProgress + "%");
                    notification = builder.build();
                    notificationManager.notify(NOTIFICATION_ID, notification);
                    break;
                case DOWN_SUCCEED:
                    File downloadAppFile = new File(FILESAVEPATH, mDownloadAppName);
                    if (downloadAppFile.exists()) {
                        File downSucceedAppFile = new File(FILESAVEPATH, mDownSucceedAppName);
                        downloadAppFile.renameTo(downSucceedAppFile);
                        UpdateVersionUtil.installAPK(UpdateVersionService.this, downSucceedAppFile);
                    }
                    break;
                case DOWN_FINISH:
                    notificationManager.cancel(NOTIFICATION_ID);
                    stopSelf();
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        isDownLoading = true;
    }

    /**
     * 当第一次启动service是走的是oncreat(),onStartCommand(),onBind(),当service启动之后再次进入的时候不在走oncreat(),而是直接走后两个，另外onStartCommeand()还可以用于接收intent的数据。
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mDownLoadUrl = intent.getStringExtra(INTENT_DOWNLOAD_URL);
        if (!TextUtils.isEmpty(mDownLoadUrl)) {
            mDownloadAppName = UpdateVersionUtil.MD5(mDownLoadUrl);
            mDownSucceedAppName = mDownloadAppName + ".apk";
            if (buildFolder(this, FILESAVEPATH)) {
                setNotification();
                new DownLoadThread().start();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //更新状态栏
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setNotification() {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        builder = new Notification.Builder(this);
        builder.setAutoCancel(true)
                .setContentTitle("正在下载：")
                .setContentText("0%")
                .setLargeIcon(bm)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setProgress(PROGRESS_MEX, 0, false)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(false)  //不能自动取消
                .setOngoing(true);     //不能滑动删除
        notification = builder.build();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private class DownLoadThread extends Thread {
        @Override
        public void run() {
            super.run();
            HttpURLConnection conn = null;
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                URL url = new URL(mDownLoadUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10 * 1000);    //设置访问网络超时时间
                conn.setReadTimeout(5 * 1000);       //设置读取数据超时时间
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept-Encoding", "identity");  //获取文件长度限定条件
                is = conn.getInputStream();
                fos = new FileOutputStream(new File(FILESAVEPATH, mDownloadAppName));
                byte[] buffer = new byte[8 * 1024];
                int mHasRead = 0;
                int len;
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                    mHasRead += len;
                    mNowProgress = PROGRESS_MEX * mHasRead / conn.getContentLength();
                    Message msg = new Message();
                    msg.obj = DOWN_LOAD;
                    handler.sendMessage(msg);
                }
                //下载成功,安装app
                if (mHasRead > 0 && mHasRead == conn.getContentLength()) {
                    Message msg = new Message();
                    msg.obj = DOWN_SUCCEED;
                    handler.sendMessage(msg);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (conn != null) {
                    conn.disconnect();  //关闭连接
                }

                Message msg = new Message();
                msg.obj = DOWN_FINISH;
                handler.sendMessage(msg);
            }
        }
    }

    //判断文件夹能否进行创建,如果可以创建则清空并创建
    private boolean buildFolder(Context context, String dirFilePath) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dirFile = new File(dirFilePath);
            if (dirFile.exists()) {
                UpdateVersionUtil.deleteFileOrDir(dirFile);
            }
            dirFile.mkdirs(); //创建多级文件夹（一级的话mkdir）
            return true;
        } else {
            Toast.makeText(context, "SD卡不存在，请检查你的设备", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isDownLoading = false;
    }
}
