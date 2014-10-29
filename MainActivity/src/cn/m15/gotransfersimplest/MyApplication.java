package cn.m15.gotransfersimplest;

import android.app.Application;
import android.os.Build;
import cn.m15.gotransfersimplest.net.wifi.WifiApConnector;

public class MyApplication extends Application {
	
	public static MyApplication sInstance;
	
	public String mMachineModel;
	
	@Override
	public void onCreate() {
		super.onCreate();
		sInstance = this;
		
		// 初始化 WifiApConnector
		WifiApConnector connector = WifiApConnector.getInstance();
		connector.initContext(this);

		// 获取机器型号
		String modelName = Build.MANUFACTURER + " " + Build.MODEL;
		mMachineModel = modelName.substring(0, modelName.length() >= 15 ? 15 : modelName.length());
	}
}
