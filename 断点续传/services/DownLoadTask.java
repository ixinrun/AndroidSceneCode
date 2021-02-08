package com.example.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpStatus;

import com.example.db.ThreadDAO;
import com.example.db.ThreadDAOImpl;
import com.example.entities.FileInfo;
import com.example.entities.ThreadInfo;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * ����������
 * 
 * @author Administrator
 * 
 */
public class DownLoadTask {
	private final String TAG = "test";
	private Context mContext = null;
	private FileInfo mFileInfo = null;
	private ThreadDAO mDAO = null;
	private int mFinished = 0;
	public boolean isPause = false;

	public DownLoadTask(Context mContext, FileInfo mFileInfo) {
		super();
		this.mContext = mContext;
		this.mFileInfo = mFileInfo;
		mDAO = new ThreadDAOImpl(mContext);
	}

	public void download() {
		// ��ȡ���ݿ���߳���Ϣ
		List<ThreadInfo> threadInfos = mDAO.getThreads(mFileInfo.getUrl());
		ThreadInfo threadInfo = null;
		Log.e(TAG, "threadInfos:" + threadInfos);
		if (threadInfos.size() == 0) {
			// ��ʼ���߳���Ϣ����
			threadInfo = new ThreadInfo(0, mFileInfo.getUrl(), 0, mFileInfo.getLength(), 0);
		} else {
			threadInfo = threadInfos.get(0);
		}
		Log.e(TAG, "threadInfo:" + threadInfo);
		// �������߳̽�������
		new DownloadThread(threadInfo).start();

	}

	/**
	 * �����߳�
	 */
	class DownloadThread extends Thread {
		private ThreadInfo mThreadInfo = null;

		public DownloadThread(ThreadInfo mThreadInfo) {
			super();
			this.mThreadInfo = mThreadInfo;
		}

		@Override
		public void run() {
			// �����ݿ��в����߳���Ϣ
			if (!mDAO.isExists(mThreadInfo.getUrl(), mThreadInfo.getId())) {
				mDAO.insertThread(mThreadInfo);
			}

			HttpURLConnection conn = null;
			RandomAccessFile raf = null;
			InputStream input = null;
			try {
				URL url = new URL(mThreadInfo.getUrl());
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(5000);
				conn.setRequestMethod("GET");
				// ��������λ��
				int start = mThreadInfo.getStart() + mThreadInfo.getFinished();
				// �������صķ�Χ
				conn.setRequestProperty("Range", "bytes=" + start + "-" + mThreadInfo.getEnd());
				// �����ļ�д��λ��
				File file = new File(DownloadService.DOWNLOAD_PATH,mFileInfo.getFileName());
				raf = new RandomAccessFile(file, "rwd");
				raf.seek(start); // ���ϴν����ĵط���ʼ
				Intent intent = new Intent(DownloadService.ACTION_UPDATE);
				mFinished = mThreadInfo.getFinished();
				// ��ʼ����(�����Ƕϵ������������±߷����벻�ǳ����SC_OK,���Ǵ�������һ���֡���״̬��)
				if (conn.getResponseCode() == HttpStatus.SC_PARTIAL_CONTENT) {
					// ��ȡ����
					input = conn.getInputStream();
					byte[] buffer = new byte[1024 * 4]; // ���û�����
					int len = 0; // ��־��ȡ���ȣ�������ʱ����-1
					long time = System.currentTimeMillis();
					while ((len = input.read(buffer)) != -1) {
						// д���ļ�
						raf.write(buffer, 0, len);
						// �������ؽ���
						mFinished += len;
						// ÿ��500��������Activity����һ�ι㲥
						if (System.currentTimeMillis() - time > 500) {
							time = System.currentTimeMillis();
							intent.putExtra("finished", mFinished * 100 / mFileInfo.getLength()); // �ٷֱ���ʽ
							mContext.sendBroadcast(intent);
						}
						// ��������ͣʱ���������ؽ���
						if (isPause) {
							mDAO.updateThread(mThreadInfo.getUrl(),mThreadInfo.getId(), mFinished);
							return;
						}
					}
					Log.e(TAG, "len:" + len);
					intent.putExtra("isFinished",true); //֪ͨ���ؽ���
					mContext.sendBroadcast(intent);
					// //���ؽ�����ɾ���߳���Ϣ
					// mDAO.deleteThread(mThreadInfo.getUrl(),mThreadInfo.getId());

				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {

				try {
					conn.disconnect();
					raf.close();
					input.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}

	}

}
