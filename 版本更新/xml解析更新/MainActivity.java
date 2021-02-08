package com.updateversion.updateversion;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {
    private UpdateVersionService updateVersionService;
    private static final String UPDATEVERSIONXMLPATH = "http://192.168.1.165:8080/UpLoad/update/version.xml";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        updateVersionService = new UpdateVersionService(UPDATEVERSIONXMLPATH, MainActivity.this,true);// 创建更新业务对象
        updateVersionService.checkUpdate();
    }

}
