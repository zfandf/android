package cn.m15.gotransfersimplest.ui.activity;

import cn.m15.gotransfersimplest.net.wifi.WifiApConnector;
import android.app.Activity;
import android.os.Bundle;

public class BaseActivity extends Activity {
	protected static int sStartedNumber = 0;
	protected WifiApConnector mApConnector;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sStartedNumber++;
		mApConnector = WifiApConnector.getInstance();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		sStartedNumber--;
		if (sStartedNumber == 0) {
			mApConnector.destroyWifiAp();
		}
	}
	
}
