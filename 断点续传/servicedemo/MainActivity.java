package com.example.servicedemo;

import com.example.entities.FileInfo;
import com.example.services.DownloadService;

import android.support.v7.app.ActionBarActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
	private final String TAG = "test";
	private TextView mTvFileName;
	private ProgressBar mPbProgress;
	private Button mBtStart, mBtStop;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// 初始化组件
		mTvFileName = (TextView) findViewById(R.id.tvFileName);
		mPbProgress = (ProgressBar) findViewById(R.id.progressBar);
		mBtStart = (Button) findViewById(R.id.btStart);
		mBtStop = (Button) findViewById(R.id.btStop);
		mPbProgress.setMax(100);
		// 创建一个文件信息对象：
		final FileInfo fileInfo = new FileInfo(
				0,
				"http://111.7.131.65/cache/dlsw.baidu.com/sw-search-sp/soft/1a/11798/kugou_V7.6.95.17685_setup.1430383399.exe?ich_args=ddd28b1afd46fec66750586fd5a16af1_7734_0_0_7_fd0127945bb4acbe192464077809d0165f4eb9645f4c1b08c8aab0e3158f96f4_63ec3961ab28fcd7945ccde40910a2b4_1_0",
				"kugou.exe", 0, 0);
		mTvFileName.setText(fileInfo.getFileName());
		// 添加事件监听
		mBtStart.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 通过Intent传递参数给Service
				Intent intent = new Intent(MainActivity.this,DownloadService.class);
				intent.setAction(DownloadService.ACTION_START); // 传递动作命令标识（DownloadService中的ACTION_START为public）
				intent.putExtra("fileInfo", fileInfo);
				startService(intent);
			}
		});
		mBtStop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 通过Intent传递参数给Service
				Intent intent = new Intent(MainActivity.this,DownloadService.class);
				intent.setAction(DownloadService.ACTION_STOP); // 传递动作命令标识（DownloadService中的ACTION_START为public）
				intent.putExtra("fileInfo", fileInfo);
				startService(intent);
			}
		});

		// 注册广播接收器
		IntentFilter filter = new IntentFilter();
		filter.addAction(DownloadService.ACTION_UPDATE);
		registerReceiver(mReceiver, filter);
	}

	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
	};

	/**
	 * 更新UI的广播接收器
	 * 
	 */
	BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (DownloadService.ACTION_UPDATE.equals(intent.getAction())) {
				int finished = intent.getIntExtra("finished", 0);
				Log.e(TAG, "finished+++++++++:" + finished);
				mPbProgress.setProgress(finished);
				boolean isFinished = intent.getBooleanExtra("isFinished", false);
				if (isFinished) {
					Toast.makeText(getApplicationContext(), "下载结束", Toast.LENGTH_LONG).show();
				}
			}

		}

	};

	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
