package cn.m15.app.android.gotransfer.ui.activity;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.m15.app.android.gotransfer.R;
import cn.m15.app.android.gotransfer.ui.widget.animation.MyView;
import cn.m15.app.android.gotransfer.ui.widget.animation.Reciver;
import cn.m15.gotransfer.sdk.SdkConst;
import cn.m15.gotransfer.sdk.net.ipmsg.UdpThreadManager;
import cn.m15.gotransfer.sdk.net.wifi.WifiApConnector.ConnectApStatusListener;

public class JoinConnectionActivity extends BaseActivity implements
		View.OnClickListener, ConnectApStatusListener {

	private ArrayList<String> mAPList = new ArrayList<String>();
	// private ArrayAdapter<String> mConnectionAdapter;
	// private TextView mConnectingTv;
	private TextView mSearchingTv;
	private TextView mNotFindConnectionTv;
	private RefreshListReceiver refreshListReceiver;
	private ImageButton mSearchbtn;
	private ImageView mIvRadar;
	private Animation mAnimation;

	private int mPointX, mPointY;
	private Bitmap mBitmap;
	private int mScreenWidth, mScreenHeight, mRadius, mBmWidth, mBmHeight;
	private RelativeLayout fl;
	private MyView mMyView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_join_connection);
		setTitle(R.string.join_connection);
		fl = (RelativeLayout) findViewById(R.id.rl_join);

		DisplayMetrics dm = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(dm);
		mScreenWidth = dm.widthPixels;
		mScreenHeight = dm.heightPixels;

		mBitmap = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.favicon50);

		// 头像宽高
		mBmWidth = mBitmap.getWidth();
		mBmHeight = mBitmap.getHeight();
		// 圆心坐标
		mPointX = mScreenWidth / 2 - mBmWidth / 2;
		mPointY = mScreenHeight / 2 - mBmHeight;
		// 半径
		mRadius = mScreenWidth / 2 - mBmWidth;
		// getSupportActionBar().hide();

		refreshListReceiver = new RefreshListReceiver();
		// mConnectingTv = (TextView) findViewById(R.id.tv_connecting);
		mNotFindConnectionTv = (TextView) findViewById(R.id.tv_not_find_connection);
		mSearchingTv = (TextView) findViewById(R.id.tv_searching);

		mIvRadar = (ImageView) findViewById(R.id.iv_searchAp);

		mAnimation = AnimationUtils.loadAnimation(this, R.anim.search);
		mAnimation.setInterpolator(new LinearInterpolator());

		mIvRadar.setAnimation(mAnimation);

		mNotFindConnectionTv.setOnClickListener(this);
		mSearchbtn = (ImageButton) findViewById(R.id.ib_searchAp);
		// mSearchbtn.setEnabled(false);
		mSearchbtn.setOnClickListener(this);
		refreshList();
		// mConnectionAdapter = new ArrayAdapter<String>(this,
		// android.R.layout.simple_list_item_1, new ArrayList<String>());
		// mConnectionList.setAdapter(mConnectionAdapter);

	}

	@Override
	protected void onStart() {
		super.onStart();
		mApConnector.addConnectApStatusListener(this);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(SdkConst.BROADCAST_ACTION_REFRESH_AP_LIST);
		registerReceiver(refreshListReceiver, intentFilter);
	}

	@Override
	protected void onStop() {
		super.onStop();
		mApConnector.removeConnectApStatusListener(this);
		try {
			unregisterReceiver(refreshListReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.ib_searchAp:
			if (mMyView != null) {
				fl.removeView(mMyView);
			}
			Log.d("zhpuJoin", "onClick");
			mSearchingTv.setVisibility(View.VISIBLE);
			mNotFindConnectionTv.setVisibility(View.GONE);
			mSearchbtn.setEnabled(false);
			mSearchbtn.setImageResource(0);
			mIvRadar.setVisibility(View.VISIBLE);
			mIvRadar.setAnimation(mAnimation);
			refreshList();
			break;
		}
	}

	public void refreshList() {
		if (mApConnector.isWifiApEnabled()) {
			mApConnector.destroyWifiAp();
		}
		mApConnector.refreshAPList();
	}

	public void startMainActivity() {
		Intent intent = new Intent(JoinConnectionActivity.this,
				MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);

	}

	private void StopAnim() {
		mIvRadar.clearAnimation();
		mIvRadar.setVisibility(View.GONE);
		fl.removeView(mMyView);
	}

	private final class RefreshListReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					SdkConst.BROADCAST_ACTION_REFRESH_AP_LIST)) {
				mSearchbtn.setEnabled(true);
				mAPList = intent
						.getStringArrayListExtra(SdkConst.INTENT_EXTRA_AP_LIST);
				if (mAPList.size() == 0) {
					if (mMyView != null) {
						fl.removeView(mMyView);
					}
					mNotFindConnectionTv.setVisibility(View.VISIBLE);
					mSearchingTv.setVisibility(View.GONE);
					StopAnim();
					mSearchbtn.setImageResource(R.drawable.bg_search);
				} else {
					try {
						ArrayList<String> nameList = new ArrayList<String>();
						for (String apName : mAPList) {
							String base64Name = apName
									.substring(SdkConst.AP_PREFIX.length());
							byte[] nameArr = android.util.Base64.decode(
									base64Name, Base64.DEFAULT);
							String userName = new String(nameArr);
							userName = userName.trim();
							nameList.add(userName);
							Log.d("zhpuapname", apName);
						}
						StopAnim();
						mSearchingTv.setVisibility(View.GONE);

						// int angle = getAngle(mAPList.size());
						int angle = getAngle(8);

						ArrayList<Reciver> list = new ArrayList<Reciver>();
						int j = 0;
						for (int i = -150; i < 210;) {
							Log.i("i", i + "");
							// 避免第一个和最后一个重合
							// if (210 - i < angle) {
							// break;
							// }
							int x = (int) (mPointX + (mRadius * Math.cos(i
									* Math.PI / 180)));
							int y = (int) (mPointY + (float) (mRadius * Math
									.sin(i * Math.PI / 180)));
							// Reciver对象
							Reciver recivera = new Reciver(mBitmap, "test", x,
									y);
							recivera.setName(nameList.get(j));
							recivera.setDecodename(mAPList.get(j));
							// Log.d("zhpuaff", mAPList.get(i));
							list.add(recivera);
							i = i + angle;
							j++;
							if (j == mAPList.size()) {
								break;
							}
						}
						Log.d("zhpuaff", list.size() + "");

						mMyView = new MyView(JoinConnectionActivity.this, list);
						fl.addView(mMyView);

					} catch (Exception e) {
						/*
						 * use try-catch for the following bug:
						 * 
						 * java.lang.RuntimeException:Error receiving broadcast
						 * Intent{ act =
						 * cn.m15.app.android.gotransfer.refreshaplist
						 * (hasextras) } in
						 * cn.m15.app.android.gotransfer.ui.activity.
						 * SearchWifiConnectionActivity$RefreshListReceiver
						 * 
						 * @4051f698 ... Caused
						 * by:java.lang.IllegalArgumentException:bad base-64
						 */
					}
				}
			}

			// else if (intent.getAction().equals(
			// Const.BROADCAST_ACTION_AP_CONNECTED)) { // int ipAddress =
			// // //
			// intent.getIntExtra(Const.INTENT_EXTRA_IP_ADDRESS, 0);
			// Log.d("zhpuconnect", "zhpuconnect");
			// netThreadHelper.connectSocket(); // 开始监听数据 startMainActivity();
			// }

			// }
		}
	}

	@Override
	public void connectApStarted() {
		finish();
	}

	@Override
	public void connectApSuccess() {
		UdpThreadManager.getInstance().connectSocket();
		startMainActivity();
	}

	/**
	 * 
	 * @param size
	 * @return 头像和圆心之间的角度
	 */
	private int getAngle(int size) {
		int angle = (int) Math.round(360 / size);
		return angle;
	}
}
