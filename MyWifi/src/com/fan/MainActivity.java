package com.fan;

import tools.ToolWifiManage;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.widget.TextView;

import com.fan.app1.R;

public class MainActivity extends ActionBarActivity {
	
	private static TextView tvIpAddress;
	
//	private static Handler handler = new Handler() {
//		public void handleMessage(android.os.Message msg) {
//			
//		};
//	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tvIpAddress = (TextView)findViewById(R.id.ipaddress);
		String ipAddress = ToolWifiManage.getIpAddress(this);

		if (TextUtils.isEmpty(ipAddress)) {
			tvIpAddress.setText(R.string.ip_error);
		} else {
			tvIpAddress.setText(ipAddress);
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

}
