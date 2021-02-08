package com.smsyzm;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.ContentValues;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 注册--手机号码注册
 * 
 * @author lengguoxing
 * 
 */
public class RegisterByPhone extends Activity implements OnClickListener {
	public EditText edit_user;
	private EditText edit_pwd;
	private EditText edit_yzm;
	private Button btn_getYzm;
	private Button show_password;
	private CheckBox chek_xianyi;
	private Button register_commit;

	String yzm = null;
	private int time = 60;
	private Timer timer = new Timer();
	TimerTask task;


	GetSmsContent content;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		InitUI();
		
		content = new GetSmsContent(RegisterByPhone.this, new Handler(), edit_yzm);
		  // 注册短信变化监听
		this.getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, content);
	}

	private boolean showPassword = true;
	private void InitUI() {
		edit_user = (EditText) findViewById(R.id.edit_phone_user);
		edit_pwd = (EditText) findViewById(R.id.edit_phone_pwd);
		edit_yzm = (EditText) findViewById(R.id.edit_reister_phone_yzm);
		btn_getYzm = (Button) findViewById(R.id.btn_reister_getphone_yzm);
		show_password = (Button) findViewById(R.id.btn_reister_show_password);
		register_commit = (Button) findViewById(R.id.register_phone);
		chek_xianyi = (CheckBox) findViewById(R.id.register_chkphone_enoughAge);


		register_commit.setOnClickListener(this);
		btn_getYzm.setOnClickListener(this);

		show_password.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(showPassword){//显示密码   
					showPassword = !showPassword;
					show_password.setText("隐藏");
					edit_pwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
					edit_pwd.setSelection(edit_pwd.getText().toString().length());
				}else{//隐藏密码
					showPassword = !showPassword;
					show_password.setText("显示");
					edit_pwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
					edit_pwd.setSelection(edit_pwd.getText().toString().length());
				}
				
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_reister_getphone_yzm: // 发送验证码
			if(checkPhone() && checkPassword()){ 
				new Send_YzmMessage().execute();
			}
			break;
		case R.id.register_phone:// 注册按钮事件
			 
			break;
		}

	}

	
	private Boolean checkPhone() {
		String phone = edit_user.getText().toString();
		if (null == phone || "".equals(phone) || "".equals(phone.trim())) {
			Toast.makeText(getApplicationContext(), "请输入手机号码", Toast.LENGTH_SHORT).show();
			return false;
		} else if (phone.length() != 11 || !phone.startsWith("1")) {
			Toast.makeText(getApplicationContext(), "手机号码格式不对", Toast.LENGTH_SHORT).show();
			return false;
		}

		return true;
	}

	private boolean checkPassword() {
		String password = edit_pwd.getText().toString();
		if (null == password || "".equals(password) || "".equals(password.trim())) {
			Toast.makeText(getApplicationContext(), "请输入密码", Toast.LENGTH_SHORT).show();
			return false;
		} else if (password.contains(" ")) {
			Toast.makeText(getApplicationContext(), "密码不能包含空格", Toast.LENGTH_SHORT).show();
			return false;
		} else if (password.length() < 6 || password.length() > 20) {
			Toast.makeText(getApplicationContext(), "密码长度为6-20字符", Toast.LENGTH_SHORT).show();
			return false;
		} else if (edit_user.getText().toString().equals(password.trim())) {
			Toast.makeText(getApplicationContext(), "密码不能跟账号一致,请重新输入", Toast.LENGTH_SHORT).show();
			return false;
		}

		return true;
	}

	 

	class Send_YzmMessage extends AsyncTask<Integer, Integer, Integer> {

		private String message;

		@Override
		protected Integer doInBackground(Integer... params) {
			insertSMS();

			return 1;
		}

		@Override
		protected void onPostExecute(Integer result) {
			Toast.makeText(getApplicationContext(), "我们已发送一条验证短信到您的手机,请注意查收", Toast.LENGTH_SHORT).show();
			btn_getYzm.setEnabled(false);
			btn_getYzm.setBackgroundResource(R.drawable.daojishi);
			task = new TimerTask() {
				@Override
				public void run() {
					runOnUiThread(new Runnable() { // UI thread
						@Override
						public void run() {
							if (time <= 0) {
								// 当倒计时小余=0时记得还原图片，可以点击
								btn_getYzm.setEnabled(true);
								btn_getYzm.setBackgroundResource(R.drawable.btn_yangzhengma_selector);
								btn_getYzm.setTextColor(Color.parseColor("#454545"));
								btn_getYzm.setText("获取验证码");
								task.cancel();
							} else {
								btn_getYzm.setText(time + "秒后重试");
								btn_getYzm.setTextColor(Color.rgb(125, 125, 125));
							}
							time--;
						}
					});
				}
			};
			
			time = 60;
			timer.schedule(task, 0, 1000);
		}
	}

	//模拟插入一条数据，相当于接到一条短息。只不过这里不会提醒
	private void insertSMS() {
		final String ADDRESS = "address";
		final String DATE = "date";
		final String READ = "read";
		final String STATUS = "status";
		final String TYPE = "type";
		final String BODY = "body";
		int MESSAGE_TYPE_INBOX = 1;
		int MESSAGE_TYPE_SENT = 2;
		ContentValues values = new ContentValues();
		/* 手机号 */
		values.put(ADDRESS, "400888666");//发送者
		/* 时间 */
		values.put(DATE, "1281403142857");
		values.put(READ, 0);//未读
		values.put(STATUS, -1);
		/* 类型1为收件箱，2为发件箱 */
		values.put(TYPE, 1);
		/* 短信体内容 */
		values.put(BODY, "您本次操作的验证码是：[234456],请勿将验证码告知他人[淘宝网]");
		/* 插入数据库操作 */
		Uri inserted = getContentResolver().insert(Uri.parse("content://sms"),
				values);
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	 
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.getContentResolver().unregisterContentObserver(content);
	}

}
