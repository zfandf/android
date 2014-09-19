package cn.m15.app.android.gotransfer.ui.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;
import cn.m15.app.android.gotransfer.R;
import cn.m15.gotransfer.sdk.net.ipmsg.UdpThreadManager;
import cn.m15.gotransfer.sdk.net.wifi.WifiApConnector;
import cn.m15.gotransfer.sdk.net.wifi.WifiApManager;

public class ConnectAppleActivity extends BaseActivity {

	private TextView mTvFirst, mTvSecond, mTvWifiAp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connect_apple);

		mTvFirst = (TextView) findViewById(R.id.tv_firststep);
		mTvSecond = (TextView) findViewById(R.id.tv_secondstep);
		mTvWifiAp = (TextView) findViewById(R.id.tv_wifiap);

		String wlan = WifiApManager.encodeWifiApName();

		mTvWifiAp.setText(getString(R.string.wifiap, wlan));

		SpannableString settingSb = new SpannableString(
				getString(R.string.connection_apple_step1));
		settingSb.setSpan(new AbsoluteSizeSpan(16, true), 0, 3,
				SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
		settingSb.setSpan(
				new ForegroundColorSpan(Color.parseColor(getResources()
						.getString(R.color.c3))), 14, 22,
				SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

		mTvFirst.setText(settingSb);

		SpannableString connectSb = new SpannableString(
				getString(R.string.connection_apple_step2));
		connectSb.setSpan(
				new ForegroundColorSpan(Color.parseColor(getResources()
						.getString(R.color.c3))), 21, 25,
				SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
		connectSb.setSpan(new AbsoluteSizeSpan(16, true), 0, 3,
				SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

		mTvSecond.setText(connectSb);
		
		// 没有创建WIFI热点的话就重新创建
		if (mApConnector.getCreateStatus() != WifiApConnector.CREATE_WIFI_AP_STARTED
				&& mApConnector.getCreateStatus() != WifiApConnector.CREATE_WIFI_AP_SUCCESS) {
			closeWifiConnection();
			mApConnector.createWifiAp();
			UdpThreadManager.getInstance().connectSocket();
		}
	}

}
