package com.updateversion.updateversion;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * 检测安装更新文件的助手类
 *
 * @author BigRun 2015/10/3
 */
public class UpdateVersionService {
    private static final int UPDATE_FALSE = 0;  //没有更新
    private static final int UPDATE_TRUE = 1;   //有更新
    private static final int DOWN = 2;// 用于区分正在下载
    private static final int DOWN_FINISH = 3;// 用于区分下载完成
    private HashMap<String, String> hashMap;// 存储跟心版本的xml信息
    private String fileSavePath;// 下载新apk的厨房地点
    private String updateVersionXMLPath;// 检测更新的xml文件
    private int progress;// 获取新apk的下载数据量,更新下载滚动条
    private boolean cancelUpdate = false;// 是否取消下载
    private Context context;
    private boolean isAutoUpdate;
    private ProgressBar progressBar;
    private Dialog downLoadDialog;
    private Handler handler = new Handler() {// 更新ui

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch ((Integer) msg.obj) {
                case UPDATE_FALSE:
                    if(!isAutoUpdate)
                    Toast.makeText(context, "当前已是最新版本", Toast.LENGTH_SHORT).show();
                    break;
                case UPDATE_TRUE:
                    showUpdateVersionDialog();// 显示提示对话框
                    break;
                case DOWN:
                    progressBar.setProgress(progress);
                    break;
                case DOWN_FINISH:
                    Toast.makeText(context, "文件下载完成,正在安装更新", Toast.LENGTH_SHORT).show();
                    installAPK();
                    break;
                default:
                    break;
            }
        }

    };

    /**
     * 构造方法
     *
     * @param updateVersionXMLPath 比较版本的xml文件地址(服务器上的)
     * @param context              上下文
     */
    public UpdateVersionService(String updateVersionXMLPath, Context context,boolean isAutoUpdate) {
        super();
        this.updateVersionXMLPath = updateVersionXMLPath;
        this.context = context;
        this.isAutoUpdate = isAutoUpdate;
    }

    /**
     * 检测是否可更新
     * 主线程不能直接进行网络请求
     */
    public void checkUpdate() {
        new CheckUpdateThread().start();
    }

    /**
     * 检查更新子线程
     *
     * @return
     */
    public class CheckUpdateThread extends Thread {
        @Override
        public void run() {
            super.run();
            int versionCode = getVersionCode(context);
            HttpURLConnection conn = null;
            InputStream inputStream = null;
            try {
                // 把version.xml放到网络上，然后获取文件信息
                URL url = new URL(updateVersionXMLPath);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10 * 1000);    //设置访问网络超时时间
                conn.setReadTimeout(5 * 1000);       //设置读取数据超时时间
                conn.setRequestMethod("GET");       //设置访问方式
                //开始连接,调用此方法就不必再使用conn.connect()方法
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    inputStream = conn.getInputStream();
                    // 解析XML文件。
                    ParseXmlService service = new ParseXmlService();
                    hashMap = service.parseXml(inputStream);
                    if (null != hashMap) {
                        Message message = new Message();
                        // 版本判断
                        int serverCode = Integer.valueOf(hashMap.get("versionCode"));
                        if (serverCode > versionCode) {
                            message.obj = UPDATE_TRUE;
                            handler.sendMessage(message);
                        } else {
                            message.obj = UPDATE_FALSE;
                            handler.sendMessage(message);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                //从里到外，从上到下，逐个关闭
                try {
                    if (inputStream != null)
                        inputStream.close();  //关闭输入流
                } catch (IOException e) {
                    e.printStackTrace();
                }
                conn.disconnect();  //关闭连接
            }
        }
    }


    /**
     * 更新提示框
     */
    private void showUpdateVersionDialog() {
        Log.e("versionDetial","versionDetial"+hashMap.get("versionDetial"));
        // 构造对话框
        Builder builder = new Builder(context);
        builder.setTitle("版本更新");
        builder.setMessage(hashMap.get("versionDetial"));
        builder.setPositiveButton("更新", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // 显示下载对话框
                showDownloadDialog();
            }
        });
        builder.setNegativeButton("稍后更新", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        Dialog noticeDialog = builder.create();
        noticeDialog.show();
    }

    /**
     * 下载的提示框
     */
    protected void showDownloadDialog() {
        {
            // 构造软件下载对话框
            Builder builder = new Builder(context);
            builder.setTitle("正在更新");
            // 给下载对话框增加进度条
            final LayoutInflater inflater = LayoutInflater.from(context);
            View v = inflater.inflate(R.layout.downloaddialog, null);
            progressBar = (ProgressBar) v.findViewById(R.id.updateProgress);
            builder.setView(v);
            // 取消更新
            builder.setNegativeButton("取消", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    // 设置取消状态
                    cancelUpdate = true;
                }
            });
            downLoadDialog = builder.create();
            downLoadDialog.show();
            downLoadDialog.setCanceledOnTouchOutside(false);
            // 现在文件
            downloadApk();
        }
    }

    /**
     * 下载apk,不能占用主线程.所以另开的线程
     */
    private void downloadApk() {
        new DownloadApkThread().start();
    }



    /**
     * 获取当前版本和服务器版本.如果服务器版本高于本地安装的版本.就更新
     *
     * @param context
     * @return
     */
    private int getVersionCode(Context context) {
        int versionCode = 0;
        try {
            // 获取软件版本号，对应AndroidManifest.xml下android:versionCode
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;

    }

    /**
     * 安装apk文件
     */
    private void installAPK() {
        File apkfile = new File(fileSavePath, hashMap.get("fileName") + ".apk");
        if (!apkfile.exists()) {
            return;
        }
        // 通过Intent安装APK文件
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
        context.startActivity(i);
        android.os.Process.killProcess(android.os.Process.myPid());// 如果不加上这句的话在apk安装完成之后点击单开会崩溃

    }

    /**
     * 卸载应用程序(没有用到)
     */
    public void uninstallAPK() {
        Uri packageURI = Uri.parse("package:com.example.updateversion");
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        context.startActivity(uninstallIntent);
    }

    /**
     * 下载apk的方法
     *
     * @author rongsheng
     */
    public class DownloadApkThread extends Thread {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            super.run();
            try {
                // 判断SD卡是否存在，并且是否具有读写权限
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    URL url = new URL(hashMap.get("loadUrl"));
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(10 * 1000);    //设置访问网络超时时间
                    conn.setReadTimeout(5 * 1000);       //设置读取数据超时时间
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Charser", "GBK,utf-8;q=0.7,*;q=0.3");
                    // 获取文件大小
                    int length = conn.getContentLength();
                    // 创建输入流
                    InputStream is = conn.getInputStream();
                    // 获得存储卡的路径
                    String sdpath = Environment.getExternalStorageDirectory() + "/";
                    fileSavePath = sdpath + "download";
                    File file = new File(fileSavePath);
                    // 判断文件目录是否存在
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    File apkFile = new File(fileSavePath, hashMap.get("fileName") + ".apk");
                    FileOutputStream fos = new FileOutputStream(apkFile);
                    int count = 0;
                    // 缓存
                    byte buf[] = new byte[1024];
                    // 写入到文件中
                    do {
                        int numread = is.read(buf);
                        count += numread;
                        // 计算进度条位置
                        progress = (int) (((float) count / length) * 100);
                        // 更新进度
                        Message message = new Message();
                        message.obj = DOWN;
                        handler.sendMessage(message);
                        if (numread <= 0) {
                            // 下载完成,取消下载对话框显示
                            downLoadDialog.dismiss();
                            Message message2 = new Message();
                            message2.obj = DOWN_FINISH;
                            handler.sendMessage(message2);
                            break;
                        }
                        // 写入文件
                        fos.write(buf, 0, numread);
                    } while (!cancelUpdate);// 点击取消就停止下载.
                    fos.close();
                    is.close();
                    conn.disconnect();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}
