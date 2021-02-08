
public class Service1 extends Service {
	// 指定另一个service进程的名字
	private String Process_Name = "com.example.servicetest2:service2";

	/**
	 *启动Service2 
	 */
	private StrongService startS2 = new StrongService.Stub() {
		@Override
		public void stopService() throws RemoteException {
			Intent i = new Intent(getBaseContext(), Service2.class);
			getBaseContext().stopService(i);
		}

		@Override
		public void startService() throws RemoteException {
			Intent i = new Intent(getBaseContext(), Service2.class);
			getBaseContext().startService(i);
		}
	};

	@Override
	public void onCreate() {
		keepService2();
	}

	/**
	 * setvice被kill时的回调，level表示kill时的等级，由此可判断被什么原因kill掉的。
	 * @param level
	 */
	@Override
	public void onTrimMemory(int level){
		keepService2();//保持Service2一直运行
	}

	/**
	 * 判断Service2是否还在运行，如果不是则启动Service2
	 */
	private  void keepService2(){
		boolean isRun = Utils.isProessRunning(Service1.this, Process_Name);
		if (isRun == false) {
			try {
				startS2.startService();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return (IBinder) startS2;
	}
}
