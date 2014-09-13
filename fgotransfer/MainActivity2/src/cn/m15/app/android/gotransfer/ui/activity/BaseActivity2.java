package cn.m15.app.android.gotransfer.ui.activity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import cn.m15.app.android.gotransfer.Const;
import cn.m15.app.android.gotransfer.database.TransferDBHelper;
import cn.m15.app.android.gotransfer.enity.FileTransferMsg;
import cn.m15.app.android.gotransfer.enity.FileTransferMsgManager;
import cn.m15.app.android.gotransfer.enity.TransferFile;
import cn.m15.app.android.gotransfer.net.ipmsg.NetUdpThreadHelper;
import cn.m15.app.android.gotransfer.net.ipmsg2.MessageConst.Args;
import cn.m15.app.android.gotransfer.net.ipmsg2.MessageConst.Status;
import cn.m15.app.android.gotransfer.net.ipmsg2.TcpFileTransferClient;
import cn.m15.app.android.gotransfer.net.ipmsg2.TcpFileTransferServer;
import cn.m15.app.android.gotransfer.net.ipmsg2.User;
import cn.m15.app.android.gotransfer.net.wifi.WifiApConnector;
import cn.m15.app.android.gotransfer.net.wifi.WifiApManager;
import cn.m15.app.android.gotransfer.ui.fragment.dialog.CommonDialogFragment;
import cn.m15.app.android.gotransfer.utils.DialogUtil;

import com.umeng.analytics.MobclickAgent;

public class BaseActivity2 extends ActionBarActivity 
		implements CommonDialogFragment.DialogButtonClickListener {
	
	public static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();
	public static NetUdpThreadHelper netThreadHelper;
	public static TcpFileTransferServer transferServer;
	public static HashMap<Long, TcpFileTransferClient> transferClientMap = 
			new HashMap<Long, TcpFileTransferClient>();
	
	protected ActionBar mActionBar;
	
	protected WifiApManager mApManager;
	protected WifiApConnector mApConnector;
	
	private static int sStartActivityNumber = 0;
	
	private static FileReceiveReceiver sFileReceiveReceiver;
	
	private class FileReceiveReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Const.BROADCAST_ACTION_RECEIVE_FILE)) {
				Intent newIntent = new Intent(BaseActivity2.this, ConversationActivity2.class);
				newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(newIntent);
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sStartActivityNumber++;
		mActionBar = getSupportActionBar();
		mApManager = WifiApManager.getInstance();
		mApConnector = WifiApConnector.getInstance();
		
		netThreadHelper = NetUdpThreadHelper.newInstance();
		netThreadHelper.registerTcpFileTransferListener(FileTransferMsgManager.getInstance());
		
		if (sFileReceiveReceiver == null) {
			sFileReceiveReceiver = new FileReceiveReceiver();			
		}
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Const.BROADCAST_ACTION_RECEIVE_FILE);
		registerReceiver(sFileReceiveReceiver, intentFilter);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		sStartActivityNumber--;
		if (sFileReceiveReceiver != null) {
			unregisterReceiver(sFileReceiveReceiver);
		}
		if (sStartActivityNumber == 0) {
			closeWifiConnection();
		}
	}
	
	public static void startTransferFiles(FragmentActivity activity, Collection<User> userList, 
			ArrayList<TransferFile> transferFiles) {
		List<Map<String, Object>> fileList = new ArrayList<Map<String, Object>>();
		for (TransferFile file : transferFiles) {
			file.transfer_status = TransferFile.TRANSFER_WAIT_SEND;
			Map<String, Object> fileInfoMap = new HashMap<String, Object>();
			fileInfoMap.put(Args.FILE_NAME, file.name);
			fileInfoMap.put(Args.FILE_PATH, file.path);
			fileInfoMap.put(Args.FILE_SIZE, Long.toHexString(file.size));
			fileInfoMap.put(Args.FILE_TYPE, String.valueOf(file.fileType));
			fileList.add(fileInfoMap);
		}

		List<FileTransferMsg> msgList = new ArrayList<FileTransferMsg>();
		String msgStr = "";
		HashMap<String, Long> packetNoMap = new HashMap<String, Long>();
		FileTransferMsgManager manager = FileTransferMsgManager.getInstance();
		for (User user : userList) {
			long packetNo = netThreadHelper.sendFiles(user.getIp(), msgStr, 0, fileList);

			FileTransferMsg msg = manager.fileTransferMsgPool.acquire();
			msg.packetNo = packetNo;
			msg.receiverName = user.getUserName();
			msg.macAddress = user.getMac();
			msg.wholeStatus = Status.WAIT_SEND;
			msg.status = Status.WAIT_SEND;
			msg.files = transferFiles;
			msgList.add(msg);

			packetNoMap.put(user.getIp(), msg.packetNo);
		}
		TransferDBHelper.updateConversations(activity.getApplicationContext(), msgList);
		manager.fileTransferMsgPool.releaseList(msgList);

		if (transferServer == null) {
			transferServer = new TcpFileTransferServer(packetNoMap, fileList.size());
			transferServer.setTcpFileTransferListener(manager);
			transferServer.startServer(THREAD_POOL);			
		} else {
			transferServer.packetNoMap.putAll(packetNoMap);
		}

		Intent intent = new Intent(activity, ConversationActivity2.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		activity.startActivity(intent);
	}
	
	public void closeWifiConnection() {
		netThreadHelper.disconnectSocket();
		if (mApManager.isWifiApEnabled() && mApManager.isWifiApMine()) {
			mApManager.setWifiApEnabled(false);
			mApManager.setMobileNetworkEnabled(true);
		} else {
			mApConnector.disconnectFromAP();
		}
	}
	
	// TODO: nothing makes it
	@Override
	public void onDialogButtonClick(CommonDialogFragment dialog, int which, String tag) {
		dialog.dismiss();
		if (which == DialogInterface.BUTTON_POSITIVE && DialogUtil.DIALOG_NETWORK_DISCONNECTED.equals(tag)) {
			closeWifiConnection();
		}
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

}
