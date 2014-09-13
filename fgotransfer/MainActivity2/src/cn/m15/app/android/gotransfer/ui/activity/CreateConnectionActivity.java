package cn.m15.app.android.gotransfer.ui.activity;

import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import cn.m15.app.android.gotransfer.Const;
import cn.m15.app.android.gotransfer.R;
import cn.m15.app.android.gotransfer.enity.TransferFile;
import cn.m15.app.android.gotransfer.enity.TransferFilesManager;
import cn.m15.app.android.gotransfer.net.ipmsg.NetUdpThreadHelper.ConnectedUserChangedListener;
import cn.m15.app.android.gotransfer.net.ipmsg2.User;
import cn.m15.app.android.gotransfer.net.wifi.WifiApManager.CreateWifiApResultListener;
import cn.m15.app.android.gotransfer.ui.fragment.dialog.CommonDialogFragment;
import cn.m15.app.android.gotransfer.ui.fragment.dialog.CommonDialogFragment.DialogButtonClickListener;
import cn.m15.app.android.gotransfer.utils.DialogUtil;

public class CreateConnectionActivity extends BaseActivity2 implements
		View.OnClickListener, DialogButtonClickListener,
		CreateWifiApResultListener, ConnectedUserChangedListener {

	protected static final FragmentActivity FragmentActivity = null;
	private boolean mIsFromSendBtn = false;

	private TextView mCreateConnectionStatusTv;
	private TextView mCreateSuccessHintTv;
	private Button mBtnCloseConnection;
	private Button mBtnFriendsInstall;
	private Timer mTimer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_connection);

		mIsFromSendBtn = getIntent().getBooleanExtra("is_from_send_btn", false);

		mCreateConnectionStatusTv = (TextView) findViewById(R.id.tv_create_connection_status);
		mCreateSuccessHintTv = (TextView) findViewById(R.id.tv_create_success_hint);

		mApManager.addCreateWifiApResultListener(this);
		netThreadHelper.addConnectedUserChangedListener(this);
		mApManager.createWifiAp();

		mBtnCloseConnection = (Button) findViewById(R.id.btn_close_connection);
		mBtnFriendsInstall = (Button) findViewById(R.id.btn_friends_install);

		mBtnCloseConnection.setVisibility(View.GONE);
		mBtnFriendsInstall.setVisibility(View.GONE);

		mBtnCloseConnection.setOnClickListener(this);
		mBtnFriendsInstall.setOnClickListener(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mApManager.removeCreateWifiApResultListener(this);
		netThreadHelper.removeConnectedUserChangedListener(this);
	}

	// TODO: 等待超时
	public void onWaitTimeout() {
		mTimer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				DialogUtil
						.showWaitJoinTimeTooLongDialog(CreateConnectionActivity.this);
			}
		};
		mTimer.schedule(task, Const.TIME_OUT);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_close_connection:
			disconnect();
			break;
		case R.id.btn_friends_install:
			startActivity(new Intent(this, InviteFriendsActivity.class));
			break;
		}
	}

	// 断开连接
	private void disconnect() {
		closeWifiConnection();
		Intent intent = new Intent(this, MainActivity2.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("isCloseConnection", true);
		startActivity(intent);
		finish();
	}

	@Override
	public void onDialogButtonClick(CommonDialogFragment dialog, int which,
			String tag) {
		if (DialogUtil.DIALOG_WAIT_JOIN_TIME_TOO_LONG.equals(tag)
				&& which == DialogInterface.BUTTON_NEGATIVE) {
			disconnect();
		} else {
			dialog.dismiss();
		}
	}

	@Override
	public void createWifiApStarted() {
		mCreateConnectionStatusTv.setText(R.string.creating_wifi_text);
	}

	@Override
	public void createWifiSuccess() {
		mCreateSuccessHintTv.setVisibility(View.VISIBLE);
		mBtnCloseConnection.setVisibility(View.VISIBLE);
		mBtnFriendsInstall.setVisibility(View.VISIBLE);
		mCreateConnectionStatusTv.setText(R.string.waiting_join_text);

		netThreadHelper.connectSocket();
		onWaitTimeout();
	}

	@Override
	public void createWifiFailed() {
		mCreateConnectionStatusTv.setText(R.string.create_fail);
	}

	@Override
	public void connectedUserChanged(Map<String, User> user) {
		if (user == null || user.size() == 0) return;
		if (mIsFromSendBtn) {
			ArrayList<TransferFile> transferFiles = 
					getIntent().getParcelableArrayListExtra("transfer_file_list");
			startTransferFiles(this, user.values(), transferFiles);
			TransferFilesManager.getInstance().clear();
			Intent intent = new Intent(this, ConversationActivity2.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
		} else {
			Intent intent = new Intent(this, MainActivity2.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
	}
}
