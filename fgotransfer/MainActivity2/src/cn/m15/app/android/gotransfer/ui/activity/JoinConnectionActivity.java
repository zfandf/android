package cn.m15.app.android.gotransfer.ui.activity;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import cn.m15.app.android.gotransfer.Const;
import cn.m15.app.android.gotransfer.R;
import cn.m15.app.android.gotransfer.net.wifi.WIFI_AP_STATE;
import cn.m15.app.android.gotransfer.net.wifi.WifiApConnector;
import cn.m15.app.android.gotransfer.net.wifi.WifiApConnector.ConnectApStatusListener;
import cn.m15.app.android.gotransfer.net.wifi.WifiApManager;

// TODO: 添加一个BroadcastReceiver还是回调接口
public class JoinConnectionActivity extends BaseActivity2 implements
		View.OnClickListener, AdapterView.OnItemClickListener,
		ConnectApStatusListener {

	private ListView mConnectionList;
	private ArrayList<String> mAPList = new ArrayList<String>();
	// private ArrayAdapter<String> mConnectionAdapter;
	private TextView mConnectingTv;
	private TextView mSearchingTv;
	private TextView mNotFindConnectionTv;
	private RefreshListReceiver refreshListReceiver;
	private Button mSearchbtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_join_connection);

		refreshListReceiver = new RefreshListReceiver();
		mConnectionList = (ListView) findViewById(R.id.lv_connections);
		mConnectingTv = (TextView) findViewById(R.id.tv_connecting);
		mNotFindConnectionTv = (TextView) findViewById(R.id.tv_not_find_connection);
		mSearchingTv=(TextView)findViewById(R.id.tv_searching);

		mConnectionList.setOnItemClickListener(this);
		mNotFindConnectionTv.setOnClickListener(this);
		mSearchbtn = (Button) findViewById(R.id.btn_searchAp);
		mSearchbtn.setEnabled(false);
		mSearchbtn.setOnClickListener(this);
		mApConnector.addConnectApStatusListener(this);
		refreshList();
		// mConnectionAdapter = new ArrayAdapter<String>(this,
		// android.R.layout.simple_list_item_1, new ArrayList<String>());
		// mConnectionList.setAdapter(mConnectionAdapter);
	}

	@Override
	protected void onStart() {
		super.onStart();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Const.BROADCAST_ACTION_REFRESH_AP_LIST);
		intentFilter.addAction(Const.BROADCAST_ACTION_AP_CONNECTED);
		registerReceiver(refreshListReceiver, intentFilter);
	}

	@Override
	protected void onStop() {
		super.onStop();
		try {
			unregisterReceiver(refreshListReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		mConnectionList.setVisibility(View.GONE);
		mConnectingTv.setVisibility(View.VISIBLE);

		// mConnectingTv
		// .setText(getString(R.string.connecting_with_somebody, name));

		if (position >= 0 && mAPList.size() > 0) {
			mConnectionList.setItemChecked(position, true);
			Log.d("zhpusize", mAPList.size() + "  ");
			String name = (String) mConnectionList.getAdapter().getItem(
					position);
			Log.d("zhpusize", mAPList.size() + "  " + name);
			mConnectingTv.setText(getString(R.string.connecting_with_somebody,
					name));
			mSearchingTv.setVisibility(View.GONE);
			mApConnector.connectToAP(mAPList.get(position));
			mConnectionList.setEnabled(false);
		}

		// // TODO:
		// GoTransferApplication.getInstance().connectToAP(mAPList.get(position),
		// Const.DEFAULTPASS);
		// WifiApManager.getInstance().connectToAP(mAPList.get(position),
		// Const.DEFAULTPASS);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_searchAp) {
			mConnectionList.setVisibility(View.VISIBLE);
			mConnectingTv.setVisibility(View.GONE);
			mSearchingTv.setVisibility(View.VISIBLE);
			mNotFindConnectionTv.setVisibility(View.GONE);
			mSearchbtn.setEnabled(false);
			refreshList();
		}
	}

	public void refreshList() {
		if (WifiApManager.getInstance().getWifiApState() == WIFI_AP_STATE.WIFI_AP_STATE_ENABLED) {
			mApManager.destroyWifiAp();
		}
		mApConnector.refreshAPList();
	}

	public void startMainActivity() {
		Intent intent = new Intent(JoinConnectionActivity.this,
				MainActivity2.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);

	}

	private void setWifiSpotdataToList(ArrayList<String> spots) {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.item_wifi_spot_list, R.id.tv_wifi_spot_name, spots);
		mConnectionList.setAdapter(adapter);
	}

	private final class RefreshListReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					Const.BROADCAST_ACTION_REFRESH_AP_LIST)) {
				mSearchbtn.setEnabled(true);
				mAPList = intent
						.getStringArrayListExtra(Const.INTENT_EXTRA_AP_LIST);
				
				if (mAPList.size() == 0) {
					mNotFindConnectionTv.setVisibility(View.VISIBLE);
					mSearchingTv.setVisibility(View.GONE);
					setWifiSpotdataToList(mAPList);

				} else {
					try {
						ArrayList<String> nameList = new ArrayList<String>();
						for (String apName : mAPList) {
							String base64Name = apName.substring(Const.APPREFIX
									.length());
							byte[] nameArr = android.util.Base64.decode(
									base64Name, Base64.DEFAULT);
							String userName = new String(nameArr);
							userName = userName.trim();
							nameList.add(userName);
						}
						setWifiSpotdataToList(nameList);
						mSearchingTv.setVisibility(View.GONE);

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

	}

	@Override
	public void connectApSuccess() {
		netThreadHelper.connectSocket();
		startMainActivity();
	}
}
