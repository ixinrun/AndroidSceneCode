public class Main extends Activity implements OnClickListener {

	private Button btn1,btn2;
	//AIDL,此处用于bindService
	private String TAG = getClass().getName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		btn1 = (Button) findViewById(R.id.button1);
		btn2 = (Button) findViewById(R.id.button2);
		
		btn1.setOnClickListener(this);
		btn2.setOnClickListener(this);
	
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		
		case R.id.button1:
			Intent i1 = new Intent(Main.this,Service1.class);
			startService(i1);

			Intent i2 = new Intent(Main.this,Service2.class);
			startService(i2);
			break;

		case R.id.button2:
			//关闭Activity
			this.finish();
			break;
		}
	}

}
