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

	//��ȡϵͳ��Ŀ¼��ָ��һ���ļ��У�ָ�����ش洢·����
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
		// ���Activity�����Ĳ���
		if (ACTION_START.equals(intent.getAction())) {
			FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
			Log.e(TAG, "Start:" + fileInfo.toString());
			//������ʼ���߳�
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
	
	//����handler�����ڲ��ദ����Ϣ
	Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_INIT:
				FileInfo fileInfo = (FileInfo) msg.obj;
				Log.e(TAG,"init:"+fileInfo);
				//������������
				mTask = new DownLoadTask(DownloadService.this,fileInfo);
				mTask.download();
				break;

			default:
				break;
			}
		};
		
	};
	
	
	
	/**
	 * ��ʼ�����߳�
	 * 
	 * Android4.0֮���������������ݶ�Ҫ�õ����̴߳���
	 * 
	 * �й����罻������ȡ(��������)������GET,������Post��
	 * ����get���͵���������С�����ܴ���2KB��ִ��Ч�ʸߡ�post���͵��������ϴ�һ�㱻Ĭ��Ϊ�������ƣ���ȫ�Ըߡ�
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
				//���������ļ�
				URL url = new URL(mfileInfo.getUrl());
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(3000);
				conn.setRequestMethod("GET"); 
				//����ļ�����
				int length = -1;
				if (conn.getResponseCode()==HttpStatus.SC_OK) {
					length = conn.getContentLength();
				}
				if (length<=0) {
					return; //��ʾ��̨���������⣬���ڽ������ء�
				}
				/**
				 * ���ش����ļ��к��ļ�
				 * 
				 * RandomAccessFile ����������ļ�(������/д)���ࡣ��֧�ֶ��ļ�������ʵĶ�ȡ��д�룬�����ǿ��Դ�ָ����λ�ö�ȡ/д���ļ����ݡ�
				 * ��rwd����RandomAccessFile��һ���ļ�����ģʽ����r��:--Read--ָ��ȡȨ�ޣ���w��:--Write--ָд��Ȩ�ޣ���d��:--Delete--ָɾ��Ȩ��
				 * 
				 */
				File dir = new File(DOWNLOAD_PATH);
				if (!dir.exists()) {
					dir.mkdir();
				}
				File file = new File(dir,mfileInfo.getFileName());
				//����fileΪRandomAccessFile����,�������䳤��
				raf = new RandomAccessFile(file, "rwd");
				raf.setLength(length);
				//������ص�������Ϣ���־
				mfileInfo.setLength(length);
				mHandler.obtainMessage(MSG_INIT,mfileInfo).sendToTarget();//����handler.sendMessage(message)����
				
			} catch (Exception e) {
				e.printStackTrace();
			} finally{
				//�ر��������Ӻ��ļ���
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
