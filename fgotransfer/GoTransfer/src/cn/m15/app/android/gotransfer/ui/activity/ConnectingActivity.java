package cn.m15.app.android.gotransfer.ui.activity;

import java.util.Map;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.m15.app.android.gotransfer.R;
import cn.m15.gotransfer.sdk.ConfigManager;
import cn.m15.gotransfer.sdk.net.ipmsg.TUser;
import cn.m15.gotransfer.sdk.net.ipmsg.UdpThreadManager;
import cn.m15.gotransfer.sdk.net.ipmsg.UdpThreadManager.ConnectedUserChangedListener;
import cn.m15.gotransfer.sdk.net.wifi.WifiApConnector;
import cn.m15.gotransfer.sdk.net.wifi.WifiApConnector.ConnectApStatusListener;
import cn.m15.gotransfer.sdk.net.wifi.WifiApConnector.ConnectWifiListener;

public class ConnectingActivity extends BaseActivity implements
		ConnectApStatusListener, ConnectWifiListener, ConnectedUserChangedListener {

	private TextView mTvConnectName;
	private TextView mTvDeviceName;
	private TextView mTvConnecting;
	private ImageView mIvPoint;
	private AnimationDrawable rocketAnimation;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connectting);
		setTitle(R.string.connectting);

		mIvPoint = (ImageView) findViewById(R.id.iv_point);
		mTvConnectName = (TextView) findViewById(R.id.tv_connect_name);
		mTvDeviceName = (TextView) findViewById(R.id.tv_device_name);
		mTvConnecting = (TextView) findViewById(R.id.tv_connecting);

		mTvConnectName.setText(getIntent().getStringExtra("ConnectName"));
		mTvDeviceName.setText(ConfigManager.getInstance().getSelfName());
		mTvConnecting.setText(getString(R.string.connecting_with_somebody,
				getIntent().getStringExtra("ConnectName")));

		mIvPoint.setBackgroundResource(R.drawable.point_anim);
		rocketAnimation = (AnimationDrawable) mIvPoint.getBackground();
		mIvPoint.post(new Runnable(){
		    public void run(){
		    	rocketAnimation.start();
		    }
		});
		mApConnector.addConnectApStatusListener(this);
		mApConnector.addConnectWifiListener(this);
		UdpThreadManager.getInstance().addConnectedUserChangedListener(this);
	}
	
	@Override
	protected void onDestroy() {
		UdpThreadManager.getInstance().removeConnectedUserChangedListener(this);
		mApConnector.removeConnectApStatusListener(this);
		mApConnector.removeConnectWifiListener(this);
		Log.e("WifiApConnector", "connecting activity destory : " + mApConnector.getConnectStatus());
		if (mApConnector.getConnectStatus() != WifiApConnector.CONNECT_AP_CONNECTED) {
			closeWifiConnection();
		}
		super.onDestroy();
	}

	@Override
	public void connectApStarted() {
	}

	@Override
	public void connectApSuccess() {
		UdpThreadManager.getInstance().connectSocket();
	}
	
	@Override
	public void connectedUserChanged(Map<String, TUser> user) {
		if (user != null && user.size() > 0) {
			startMainActivity();
		}
	}

//	@Override
//	public void connectApTimeout() {
//		Toast.makeText(this, R.string.join_time_out, Toast.LENGTH_SHORT).show();
//		startMainActivity();
//	}
	
	@Override
	public void wifiDisConnected() {
		Toast.makeText(this, R.string.wifi_connect_failed, Toast.LENGTH_SHORT).show();
		startMainActivity();
	}

	public void startMainActivity() {
		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}
}
