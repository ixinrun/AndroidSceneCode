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
 * ע��--�ֻ�����ע��
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
		  // ע����ű仯����
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
				if(showPassword){//��ʾ����   
					showPassword = !showPassword;
					show_password.setText("����");
					edit_pwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
					edit_pwd.setSelection(edit_pwd.getText().toString().length());
				}else{//��������
					showPassword = !showPassword;
					show_password.setText("��ʾ");
					edit_pwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
					edit_pwd.setSelection(edit_pwd.getText().toString().length());
				}
				
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_reister_getphone_yzm: // ������֤��
			if(checkPhone() && checkPassword()){ 
				new Send_YzmMessage().execute();
			}
			break;
		case R.id.register_phone:// ע�ᰴť�¼�
			 
			break;
		}

	}

	
	private Boolean checkPhone() {
		String phone = edit_user.getText().toString();
		if (null == phone || "".equals(phone) || "".equals(phone.trim())) {
			Toast.makeText(getApplicationContext(), "�������ֻ�����", Toast.LENGTH_SHORT).show();
			return false;
		} else if (phone.length() != 11 || !phone.startsWith("1")) {
			Toast.makeText(getApplicationContext(), "�ֻ������ʽ����", Toast.LENGTH_SHORT).show();
			return false;
		}

		return true;
	}

	private boolean checkPassword() {
		String password = edit_pwd.getText().toString();
		if (null == password || "".equals(password) || "".equals(password.trim())) {
			Toast.makeText(getApplicationContext(), "����������", Toast.LENGTH_SHORT).show();
			return false;
		} else if (password.contains(" ")) {
			Toast.makeText(getApplicationContext(), "���벻�ܰ����ո�", Toast.LENGTH_SHORT).show();
			return false;
		} else if (password.length() < 6 || password.length() > 20) {
			Toast.makeText(getApplicationContext(), "���볤��Ϊ6-20�ַ�", Toast.LENGTH_SHORT).show();
			return false;
		} else if (edit_user.getText().toString().equals(password.trim())) {
			Toast.makeText(getApplicationContext(), "���벻�ܸ��˺�һ��,����������", Toast.LENGTH_SHORT).show();
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
			Toast.makeText(getApplicationContext(), "�����ѷ���һ����֤���ŵ������ֻ�,��ע�����", Toast.LENGTH_SHORT).show();
			btn_getYzm.setEnabled(false);
			btn_getYzm.setBackgroundResource(R.drawable.daojishi);
			task = new TimerTask() {
				@Override
				public void run() {
					runOnUiThread(new Runnable() { // UI thread
						@Override
						public void run() {
							if (time <= 0) {
								// ������ʱС��=0ʱ�ǵû�ԭͼƬ�����Ե��
								btn_getYzm.setEnabled(true);
								btn_getYzm.setBackgroundResource(R.drawable.btn_yangzhengma_selector);
								btn_getYzm.setTextColor(Color.parseColor("#454545"));
								btn_getYzm.setText("��ȡ��֤��");
								task.cancel();
							} else {
								btn_getYzm.setText(time + "�������");
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

	//ģ�����һ�����ݣ��൱�ڽӵ�һ����Ϣ��ֻ�������ﲻ������
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
		/* �ֻ��� */
		values.put(ADDRESS, "400888666");//������
		/* ʱ�� */
		values.put(DATE, "1281403142857");
		values.put(READ, 0);//δ��
		values.put(STATUS, -1);
		/* ����1Ϊ�ռ��䣬2Ϊ������ */
		values.put(TYPE, 1);
		/* ���������� */
		values.put(BODY, "�����β�������֤���ǣ�[234456],������֤���֪����[�Ա���]");
		/* �������ݿ���� */
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
