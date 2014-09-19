package cn.m15.gotransfer.sdk.net.ipmsg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.content.Intent;
import cn.m15.gotransfer.sdk.SdkConst;
import cn.m15.gotransfer.sdk.database.TransferDBHelper;
import cn.m15.gotransfer.sdk.entity.TransferFile;
import cn.m15.gotransfer.sdk.entity.TransferMsg;
import cn.m15.gotransfer.sdk.entity.TransferMsgManager;
import cn.m15.gotransfer.sdk.net.ipmsg.MessageConst.Status;

public class TransferManager {
	
	private static TransferManager sInstance;

	private ExecutorService mThreadPool;
	private TcpTransferServer transferServer;

	public HashMap<Long, TcpTransferClient> transferClientMap = null;
	
	private TransferManager() {
		transferServer = null;
		mThreadPool = Executors.newCachedThreadPool();
		
		transferClientMap = new HashMap<Long, TcpTransferClient>();	
	}
	
	public static TransferManager getInstance() {
		if (sInstance == null) {
			sInstance = new TransferManager();			
		}
		return sInstance;
	}
	
	public ExecutorService getExecutorService() {
		return mThreadPool;
	}
	
	public TcpTransferServer getTransferServer() {
		return transferServer;
	}
	
	public HashMap<Long, TcpTransferClient> getTransferClientMap() {
		return transferClientMap;
	}
	
	/**
	 * 根据packetNo 返回TcpFileTransferClient
	 * @param packetNo
	 * @return
	 */
	public TcpTransferClient getTransferClient(long packetNo) {
		return transferClientMap.get(packetNo);
	}
	
	public void stopServer(boolean auto) {
		if (transferServer != null) {
			transferServer.stopServer(auto);
			transferServer = null;
		}
	}
	
	public void cancelSendFile(long packetNo) {
		if (transferServer != null) {
			transferServer.cancelSendFile(packetNo);
		}
	}
	
	public void startTransferFiles(Context context, Collection<TUser> userList, ArrayList<TransferFile> transferFiles) {
		List<TransferMsg> msgList = new ArrayList<TransferMsg>();
		HashMap<String, Long> packetNoMap = new HashMap<String, Long>();
		TransferMsgManager manager = TransferMsgManager.getInstance();
		
		UdpThreadManager netThreadHelper = UdpThreadManager.getInstance();
		
		for (TUser user : userList) {
			// 发送"发送文件列表(UDP)"
			long packetNo = netThreadHelper.sendFiles(user.getIp(), "", 0, transferFiles);

			// 构造传输消息
			TransferMsg msg = manager.fileTransferMsgPool.acquire();
			msg.packetNo = packetNo;
			msg.receiverName = user.getUserName();
			msg.macAddress = user.getMac();
			msg.wholeStatus = Status.WAIT_SEND;
			msg.status = Status.WAIT_SEND;
			msg.files = transferFiles;
			msgList.add(msg);

			packetNoMap.put(user.getIp(), msg.packetNo);
		}
		TransferDBHelper.updateConversations(context, msgList, null);
		manager.fileTransferMsgPool.releaseList(msgList);

		// 开启传输Server
		if (transferServer != null) {
			transferServer.stopServer(true);
			transferServer = null;
		}
		transferServer = new TcpTransferServer(packetNoMap, transferFiles.size());
		transferServer.setTcpFileTransferListener(manager);
		mThreadPool.execute(transferServer);

		// 跳转到传输页面
		Intent intent = new Intent(SdkConst.BROADCAST_ACTION_RECEIVE_FILE);
		context.getApplicationContext().sendBroadcast(intent);
	}
	
	public Collection<Long> getPacketNos() {
		if (transferServer != null && transferServer.packetNoMap != null) {
			return transferServer.packetNoMap.values();
		}
		return null;
	}

}
