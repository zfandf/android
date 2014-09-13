package cn.m15.app.android.gotransfer.enity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import cn.m15.app.android.gotransfer.GoTransferApplication;
import cn.m15.app.android.gotransfer.database.TransferDBHelper;
import cn.m15.app.android.gotransfer.enity.FileTransferMsgManager.FileTransferMsgPool;
import cn.m15.app.android.gotransfer.net.ipmsg2.MessageConst.Status;

public class FileTransferMsgPoller {
	
	private Timer mTimer;
	private FileTransferMsgTimerTask mTimerTask;
	private WakeLock mWakeLock;
	
	public FileTransferMsgPoller() {
		mTimer = new Timer();
		mTimerTask = new FileTransferMsgTimerTask();
	}
	
	public void start(Context context) {
		stop();
		
		PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "gotransfer_wakelock");
		mWakeLock.acquire();
		mTimer.schedule(mTimerTask, 0, 1500); // 1.5s
	}
	
	public void stop() {
		if (mWakeLock != null) {
			mTimerTask.cancel();
			mWakeLock.release();
			mWakeLock = null;
		}
	}
	
	public class FileTransferMsgTimerTask extends TimerTask {
		private FileTransferMsgManager mManager;
		private ArrayList<FileTransferMsg> mMsgListTemp;
		private HashMap<String, FileTransferMsg> mMsgProgressMap;
		private FileTransferMsgPool mMsgPool;
		
		public FileTransferMsgTimerTask() {
			mManager = FileTransferMsgManager.getInstance();
			mMsgListTemp = new ArrayList<FileTransferMsg>();
			mMsgProgressMap = new HashMap<String, FileTransferMsg>();
			mMsgPool = FileTransferMsgManager.getInstance().fileTransferMsgPool;
		}

		@Override
		public void run() {
			ArrayList<FileTransferMsg> msgList = mManager.getFileTransferMsgList();
			mMsgListTemp.addAll(msgList);
			
			for (FileTransferMsg msg : mMsgListTemp) {
				if (msg == null) continue; 
				if (msg.wholeStatus == Status.RECEIVING	|| msg.wholeStatus == Status.SENDING) {
					String key = msg.packetNo + msg.srcPath;
					if (mMsgProgressMap.containsKey(key)) {
						FileTransferMsg value = mMsgProgressMap.get(key);
						if (msg.transferSize > value.transferSize) {
							mMsgProgressMap.put(key, msg);
							msgList.remove(value);
						}
					} else {
						mMsgProgressMap.put(key, msg);
					}
				}
			}
			
			if (msgList != null && msgList.size() > 0) {
				Log.e("FileTransferMsgPoller", "poller msg : " + Arrays.toString(msgList.toArray()));
				TransferDBHelper.updateConversations(GoTransferApplication.getInstance(), msgList);
				for (FileTransferMsg msg : mMsgListTemp) {
					mMsgPool.release(msg);
				}				
			}
			
			mMsgProgressMap.clear();
			mMsgListTemp.clear();
		}
		
	}
}
