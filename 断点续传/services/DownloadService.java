package com.example.services;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpStatus;

import com.example.entities.FileInfo;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class DownloadService extends Service {
	private final String TAG = "test";

	//获取系统根目录并指定一个文件夹（指定下载存储路径）
	public static final String DOWNLOAD_PATH =
			Environment.getExternalStorageDirectory().getAbsolutePath()+
			"/downloads/";
	public static final String ACTION_START = "ACTION_START";
	public static final String ACTION_STOP = "ACTION_STOP";
	public static final String ACTION_UPDATE = "ACTION_UPDATE";
    public static final int MSG_INIT = 0;
    private DownLoadTask mTask = null;
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// 获得Activity传来的参数
		if (ACTION_START.equals(intent.getAction())) {
			FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
			Log.e(TAG, "Start:" + fileInfo.toString());
			//启动初始化线程
			new InitThtead(fileInfo).start();
		} else if (ACTION_STOP.equals(intent.getAction())){
			if (mTask != null) {
				mTask.isPause = true;
			}
			Log.e(TAG, "isPause:" + mTask.isPause);
		}

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	//创建handler匿名内部类处理消息
	Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_INIT:
				FileInfo fileInfo = (FileInfo) msg.obj;
				Log.e(TAG,"init:"+fileInfo);
				//启动下载任务
				mTask = new DownLoadTask(DownloadService.this,fileInfo);
				mTask.download();
				break;

			default:
				break;
			}
		};
		
	};
	
	
	
	/**
	 * 初始化子线程
	 * 
	 * Android4.0之后凡是请求网络数据都要用到多线程处理
	 * 
	 * 有关网络交互：获取(或者下载)数据用GET,其余用Post。
	 * 区别：get传送的数据量较小，不能大于2KB，执行效率高。post传送的数据量较大，一般被默认为不受限制，安全性高。
	 * 
	 */
	class InitThtead extends Thread{
		private FileInfo mfileInfo;
		
		public InitThtead(FileInfo mFileInfo) {
			this.mfileInfo = mFileInfo;
		}
		
		@Override
		public void run() {
			HttpURLConnection conn =null;
			RandomAccessFile raf = null;
			try {
				//链接网络文件
				URL url = new URL(mfileInfo.getUrl());
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(3000);
				conn.setRequestMethod("GET"); 
				//获得文件长度
				int length = -1;
				if (conn.getResponseCode()==HttpStatus.SC_OK) {
					length = conn.getContentLength();
				}
				if (length<=0) {
					return; //表示后台数据有问题，不在进行下载。
				}
				/**
				 * 本地创建文件夹和文件
				 * 
				 * RandomAccessFile 是随机访问文件(包括读/写)的类。它支持对文件随机访问的读取和写入，即我们可以从指定的位置读取/写入文件数据。
				 * “rwd”是RandomAccessFile的一种文件操作模式，“r”:--Read--指读取权限；“w”:--Write--指写入权限；“d”:--Delete--指删除权限
				 * 
				 */
				File dir = new File(DOWNLOAD_PATH);
				if (!dir.exists()) {
					dir.mkdir();
				}
				File file = new File(dir,mfileInfo.getFileName());
				//赋予file为RandomAccessFile类型,并设置其长度
				raf = new RandomAccessFile(file, "rwd");
				raf.setLength(length);
				//传递相关的下载信息或标志
				mfileInfo.setLength(length);
				mHandler.obtainMessage(MSG_INIT,mfileInfo).sendToTarget();//类似handler.sendMessage(message)方法
				
			} catch (Exception e) {
				e.printStackTrace();
			} finally{
				//关闭网络链接和文件流
				try {
					conn.disconnect();
					raf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
		
	}

}
