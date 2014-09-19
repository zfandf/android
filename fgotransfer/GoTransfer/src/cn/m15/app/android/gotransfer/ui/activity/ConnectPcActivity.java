package cn.m15.app.android.gotransfer.ui.activity;

import java.io.IOException;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import cn.m15.app.android.gotransfer.R;
import cn.m15.app.android.gotransfer.net.httpserver.GowebNanoHTTPD;
import cn.m15.gotransfer.sdk.net.wifi.WifiApConnector;
import cn.m15.gotransfer.sdk.net.wifi.WifiApManager;
import cn.m15.gotransfer.sdk.net.wifi.WifiApState;

public class ConnectPcActivity extends BaseActivity implements OnClickListener {

	private TextView mTvNetName;
	private TextView mTvIpAddress;
	private TextView mTvVerifiyCode;
	private Button mTvSettingWifi;
	private View mTvConnecting;
	private GowebNanoHTTPD httpServer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connect_pc);

		mTvNetName = (TextView) findViewById(R.id.cp_current_net);
		mTvIpAddress = (TextView) findViewById(R.id.cp_current_ip);
		mTvVerifiyCode = (TextView) findViewById(R.id.cp_current_code);
		mTvSettingWifi = (Button) findViewById(R.id.cp_setting);
		mTvConnecting = findViewById(R.id.cp_connecting);

		setVerifyCode();
		mTvSettingWifi.setOnClickListener(this);
	}

	private void setNetWork() {
		WifiApConnector wifiConnector = WifiApConnector.getInstance();
		boolean isWifiConnected = wifiConnector.isWifiConnected();
		WifiApState apStatus = WifiApManager.getInstance().getWifiApState();
		boolean isAPConnected = mApConnector.getCreateStatus() == WifiApConnector.CREATE_WIFI_AP_SUCCESS
				&& (apStatus == WifiApState.WIFI_AP_STATE_ENABLED || apStatus == WifiApState.WIFI_AP_STATE_ENABLING);
		
		if (isWifiConnected || isAPConnected) {
			this.startGowebNanoHttpd();
			String ipAddress;
			String netWorkName;
			mTvSettingWifi.setVisibility(View.INVISIBLE);
			mTvConnecting.setVisibility(View.VISIBLE);
			if (isWifiConnected) {
				String[] networkInfo = WifiApManager.getInstance()
						.getWifiIpAddressAndName();
				ipAddress = networkInfo[0] + ":"
						+ GowebNanoHTTPD.DEFAULT_HTTP_PORT;
				netWorkName = networkInfo[1];
			} else {
				netWorkName = WifiApManager.encodeWifiApName();
				ipAddress = "http://192.168.43.1:"
						+ GowebNanoHTTPD.DEFAULT_HTTP_PORT;
			}
			mTvNetName.setText(netWorkName);
			mTvIpAddress.setText(ipAddress);
		} else {
			mTvSettingWifi.setVisibility(View.VISIBLE);
			mTvConnecting.setVisibility(View.INVISIBLE);
		}
	}

	private void setVerifyCode() {
		String verifyCode = GowebNanoHTTPD.generateVerifyCode();
		mTvVerifiyCode.setText(verifyCode);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.stopGowebNanoHttpd();
	}

	public void startGowebNanoHttpd() {
		if (this.httpServer == null) {
			this.httpServer = new GowebNanoHTTPD(this);
		}
		try {
			this.httpServer.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void stopGowebNanoHttpd() {
		if (this.httpServer != null) {
			this.httpServer.stop();
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.cp_setting:
			this.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
			break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(">>> resume >>>", "OK");
		setNetWork();
	}

}
