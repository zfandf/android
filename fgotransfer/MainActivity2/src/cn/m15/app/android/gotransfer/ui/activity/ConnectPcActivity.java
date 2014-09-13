package cn.m15.app.android.gotransfer.ui.activity;

import java.io.IOException;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import cn.m15.app.android.gotransfer.R;
import cn.m15.app.android.gotransfer.net.wifi.WifiApManager;
import cn.m15.app.android.gotransfer.net.httpserver.GowebNanoHTTPD;

public class ConnectPcActivity extends BaseActivity2 implements OnClickListener {

	private TextView mTvIpAddress;
	private TextView mTvVerifiyCode;
	private Button mTvSettingWifi;
	private GowebNanoHTTPD httpServer;
	private String ipAddress = "";
	private static final int SUCCESS = 1;
	private static final int FAILED = 0;
	private int overTime = 50;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == SUCCESS) {
				mTvIpAddress.setText(ipAddress + ":" + GowebNanoHTTPD.DEFAULT_HTTP_PORT);
				mTvSettingWifi.setVisibility(View.INVISIBLE);
			} else if (msg.what == FAILED) {
				mTvIpAddress.setText(R.string.connect_pc_fail);
				mTvSettingWifi.setVisibility(View.VISIBLE);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connect_pc);
		
		mTvIpAddress = (TextView) findViewById(R.id.cp_ipaddress);
		mTvVerifiyCode = (TextView) findViewById(R.id.cp_verifycode);
		mTvSettingWifi = (Button) findViewById(R.id.cp_setting);
		
		mTvIpAddress.setText(R.string.connect_pc_startwifi);
		showIpAddress();
		String verifyCode = GowebNanoHTTPD.generateVerifyCode();
		mTvVerifiyCode.setText(verifyCode);
		
		mTvSettingWifi.setOnClickListener(this);
		this.startGowebNanoHttpd();
	}

	private void  showIpAddress() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					int count = 0;
					while (TextUtils.isEmpty(ipAddress)) {
						ipAddress = WifiApManager.getInstance().getWifiIpAddress();
						Thread.sleep(200);
						if (++count >= overTime) {
							break;
						}
					}
					Message msg = new Message();
					msg.what = TextUtils.isEmpty(ipAddress) ? FAILED : SUCCESS;
					ConnectPcActivity.this.handler.sendMessage(msg);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.stopGowebNanoHttpd();
	}

	public void startGowebNanoHttpd() {
		if (this.httpServer == null) {
			httpServer = new GowebNanoHTTPD(this);
		}
		try {
			httpServer.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void stopGowebNanoHttpd() {
		this.httpServer.stop();
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		intent.setAction(Settings.ACTION_WIFI_SETTINGS);
		startActivity(intent);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		showIpAddress();						
	}
}
