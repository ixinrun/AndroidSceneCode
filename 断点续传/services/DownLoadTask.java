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
 * 下载任务类
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
		// 读取数据库的线程信息
		List<ThreadInfo> threadInfos = mDAO.getThreads(mFileInfo.getUrl());
		ThreadInfo threadInfo = null;
		Log.e(TAG, "threadInfos:" + threadInfos);
		if (threadInfos.size() == 0) {
			// 初始化线程信息对象
			threadInfo = new ThreadInfo(0, mFileInfo.getUrl(), 0, mFileInfo.getLength(), 0);
		} else {
			threadInfo = threadInfos.get(0);
		}
		Log.e(TAG, "threadInfo:" + threadInfo);
		// 创建子线程进行下载
		new DownloadThread(threadInfo).start();

	}

	/**
	 * 下载线程
	 */
	class DownloadThread extends Thread {
		private ThreadInfo mThreadInfo = null;

		public DownloadThread(ThreadInfo mThreadInfo) {
			super();
			this.mThreadInfo = mThreadInfo;
		}

		@Override
		public void run() {
			// 向数据库中插入线程信息
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
				// 设置下载位置
				int start = mThreadInfo.getStart() + mThreadInfo.getFinished();
				// 设置下载的范围
				conn.setRequestProperty("Range", "bytes=" + start + "-" + mThreadInfo.getEnd());
				// 设置文件写入位置
				File file = new File(DownloadService.DOWNLOAD_PATH,mFileInfo.getFileName());
				raf = new RandomAccessFile(file, "rwd");
				raf.seek(start); // 从上次结束的地方开始
				Intent intent = new Intent(DownloadService.ACTION_UPDATE);
				mFinished = mThreadInfo.getFinished();
				// 开始下载(这里是断点续传，所以下边返回码不是常规的SC_OK,而是代表“访问一部分”的状态码)
				if (conn.getResponseCode() == HttpStatus.SC_PARTIAL_CONTENT) {
					// 读取数据
					input = conn.getInputStream();
					byte[] buffer = new byte[1024 * 4]; // 设置缓冲区
					int len = 0; // 标志读取进度，当读完时返回-1
					long time = System.currentTimeMillis();
					while ((len = input.read(buffer)) != -1) {
						// 写入文件
						raf.write(buffer, 0, len);
						// 计算下载进度
						mFinished += len;
						// 每隔500毫秒向主Activity发送一次广播
						if (System.currentTimeMillis() - time > 500) {
							time = System.currentTimeMillis();
							intent.putExtra("finished", mFinished * 100 / mFileInfo.getLength()); // 百分比形式
							mContext.sendBroadcast(intent);
						}
						// 在下载暂停时，保存下载进度
						if (isPause) {
							mDAO.updateThread(mThreadInfo.getUrl(),mThreadInfo.getId(), mFinished);
							return;
						}
					}
					Log.e(TAG, "len:" + len);
					intent.putExtra("isFinished",true); //通知下载结束
					mContext.sendBroadcast(intent);
					// //下载结束后删除线程信息
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
