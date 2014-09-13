package cn.m15.app.android.gotransfer.ui.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;
import cn.m15.app.android.gotransfer.R;
import cn.m15.app.android.gotransfer.net.wifi.WifiApManager;

public class ConnectAppleActivity extends BaseActivity2 {

	private TextView mTvFirst, mTvSecond, mTvWifiAp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connect_apple);
		// setCustomTitle(R.string.about);

		mTvFirst = (TextView) findViewById(R.id.tv_firststep);
		mTvSecond = (TextView) findViewById(R.id.tv_secondstep);
		mTvWifiAp = (TextView) findViewById(R.id.tv_wifiap);

		WifiApManager.getInstance().setWifiApEnabled(true);

		String wlan = WifiApManager.getInstance().getWifiApName();

		mTvWifiAp.setText(getString(R.string.wifiap, wlan));

		SpannableString settingSb = new SpannableString(
				getString(R.string.connection_apple_step1));
		settingSb.setSpan(
				new ForegroundColorSpan(Color.parseColor(getResources()
						.getString(R.color.c4))), 14, 22,
				SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

		mTvFirst.setText(settingSb);

		SpannableString connectSb = new SpannableString(
				getString(R.string.connection_apple_step2));
		connectSb.setSpan(
				new ForegroundColorSpan(Color.parseColor(getResources()
						.getString(R.color.c4))), 21, 25,
				SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

		mTvSecond.setText(connectSb);
	}

}
