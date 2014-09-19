package cn.m15.app.android.gotransfer.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import cn.m15.app.android.gotransfer.ui.fragment.dialog.CommonDialogFragment;
import cn.m15.gotransfer.sdk.SdkConst;
import cn.m15.gotransfer.sdk.entity.TransferMsgManager;
import cn.m15.gotransfer.sdk.net.ipmsg.UdpThreadManager;
import cn.m15.gotransfer.sdk.net.wifi.WifiApConnector;

import com.umeng.analytics.MobclickAgent;

public class BaseActivity extends ActionBarActivity implements
		CommonDialogFragment.DialogButtonClickListener {
	
	protected ActionBar mActionBar;

	protected WifiApConnector mApConnector;

	private static int sStartActivityNumber = 0;
	private ReceiveFileReceiver mReceiveFileReceiver;
	private IntentFilter mIntentFilter;

	public class ReceiveFileReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(SdkConst.BROADCAST_ACTION_RECEIVE_FILE)) {
				Intent newIntent = new Intent(context, ConversationActivity.class);
				newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				context.startActivity(newIntent);
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sStartActivityNumber++;
		mActionBar = getSupportActionBar();
		mActionBar.setHomeButtonEnabled(true);
		mActionBar.setDisplayHomeAsUpEnabled(true);

		mApConnector = WifiApConnector.getInstance();

		mReceiveFileReceiver = new ReceiveFileReceiver();
		mIntentFilter = new IntentFilter(SdkConst.BROADCAST_ACTION_RECEIVE_FILE);
		registerReceiver(mReceiveFileReceiver, mIntentFilter);

		UdpThreadManager.getInstance().registerTcpFileTransferListener(
				TransferMsgManager.getInstance());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiveFileReceiver);
		sStartActivityNumber--;
		if (sStartActivityNumber == 0) {
			closeWifiConnection();
		}
	}

	public void closeWifiConnection() {
		UdpThreadManager.getInstance().disconnectSocket();
		mApConnector.closeWifiConnection();
		WifiApConnector.deleteUselessMyWifis(getApplicationContext());
	}

	@Override
	public void onDialogButtonClick(CommonDialogFragment dialog, int which, String tag) {
		dialog.dismiss();
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;
		}
		return true;
	}
}
