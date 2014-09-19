package cn.m15.gotransfer.sdk.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import cn.m15.gotransfer.sdk.database.TransferDBHelper;
import cn.m15.gotransfer.sdk.database.TransferDBHelper.TransferDatabaseListener;
import cn.m15.gotransfer.sdk.entity.TransferMsgManager.FileTransferMsgPool;
import cn.m15.gotransfer.sdk.net.ipmsg.MessageConst.Status;
import cn.m15.gotransfer.sdk.net.ipmsg.TransferManager;
import cn.m15.gotransfer.sdk.utils.LogManager;

public class TransferMsgPoller implements Runnable {
	private volatile boolean started;

	private Context mContext;
	private WakeLock mWakeLock;

	private TransferMsgManager mManager;
	private ArrayList<TransferMsg> mMsgListTemp;
	private HashMap<String, TransferMsg> mMsgProgressMap;
	private FileTransferMsgPool mMsgPool;
	
	private TransferDatabaseListener mTransferDatabaseListener;
	
	public TransferMsgPoller() {
		mManager = TransferMsgManager.getInstance();
		mMsgListTemp = new ArrayList<TransferMsg>();
		mMsgProgressMap = new HashMap<String, TransferMsg>();
		mMsgPool = TransferMsgManager.getInstance().fileTransferMsgPool;
	}

	public void start(Context context) {
		mContext = context;
		stop();

		PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "gotransfer_wakelock");
		mWakeLock.acquire();

		started = true;
		TransferManager.getInstance().getExecutorService().execute(this);
	}

	public void stop() {
		started = false;
		if (mWakeLock != null) {
			mWakeLock.release();
			mWakeLock = null;
		}
	}

	@Override
	public synchronized void run() {
		while (started) {
			ArrayList<TransferMsg> msgList = mManager.getFileTransferMsgList();

			LogManager.d("file_transfer_msg_poller", "msg size: " + msgList.size());

			mMsgListTemp.addAll(msgList);

			for (TransferMsg msg : mMsgListTemp) {
				if (msg == null)
					continue;
				if (msg.wholeStatus == Status.RECEIVING
						|| msg.wholeStatus == Status.SENDING) {
					String key = msg.packetNo + msg.srcPath;
					if (mMsgProgressMap.containsKey(key)) {
						TransferMsg value = mMsgProgressMap.get(key);
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
				LogManager.d("file_transfer_msg_poller", "msgList: " + Arrays.toString(msgList.toArray()));
				TransferDBHelper.updateConversations(mContext, msgList, mTransferDatabaseListener);
				for (TransferMsg msg : mMsgListTemp) {
					if (msg != null) {
						mMsgPool.release(msg);
					}
				}
			}

			mMsgProgressMap.clear();
			mMsgListTemp.clear();
			
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setTransferDatabaseListener(TransferDatabaseListener listener) {
		mTransferDatabaseListener = listener;
	}
}
