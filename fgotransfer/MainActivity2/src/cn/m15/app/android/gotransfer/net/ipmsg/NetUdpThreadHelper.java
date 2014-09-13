package cn.m15.app.android.gotransfer.net.ipmsg;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import cn.m15.app.android.gotransfer.Const;
import cn.m15.app.android.gotransfer.GoTransferApplication;
import cn.m15.app.android.gotransfer.net.ipmsg2.ChatMessage;
import cn.m15.app.android.gotransfer.net.ipmsg2.MessageConst;
import cn.m15.app.android.gotransfer.net.ipmsg2.MessageConst.Args;
import cn.m15.app.android.gotransfer.net.ipmsg2.MessageConst.Command;
import cn.m15.app.android.gotransfer.net.ipmsg2.ReceiveMsgListener;
import cn.m15.app.android.gotransfer.net.ipmsg2.TcpFileTransferListener;
import cn.m15.app.android.gotransfer.net.ipmsg2.UdpMessage;
import cn.m15.app.android.gotransfer.net.ipmsg2.User;
import cn.m15.app.android.gotransfer.net.wifi.WifiApManager;

/**
 * 网络通信辅助类 实现UDP通信以及UDP端口监听 端口监听采用多线程方式
 * 
 * 单例模式
 * 
 */

public class NetUdpThreadHelper {
	public static final String TAG = "NetUdpThreadHelper";

	private static final int REFRESH_CONNECTED_USER_LIST = 1;
	private static NetUdpThreadHelper instance;

	private Thread udpThread = null; // 接收UDP数据线程
	private NetUdpReceiveThread netUdpReceiveThread;
	private Map<Long, NetUdpSendThread> mSendFileUdpThreadMap;
	private DatagramSocket udpSocket = null; // 用于接收和发送UDP数据的socket

	private Map<String, User> users; // 当前所有用户的集合，以IP为KEY
	private Map<String, Date> userActivity; // 用户活跃时间

	private Queue<ChatMessage> receiveMsgQueue; // 消息队列,在没有聊天窗口时将接收的消息放到这个队列中
	private Vector<ReceiveMsgListener> listeners; // ReceiveMsgListener容器，当一个聊天窗口打开时，将其加入。一定要记得适时将其移除

	private Handler mHandler;
	private List<ConnectedUserChangedListener> mConnectUserChangedListeners;

	private boolean threadWorking = false;
	private Timer timer;
	private TimerTask mTask;
	private static final long notifyOnlinePeriod = 15000;
	
	private TcpFileTransferListener listener;

	private NetUdpThreadHelper() {
		users = new HashMap<String, User>();
		userActivity = new HashMap<String, Date>();
		receiveMsgQueue = new ConcurrentLinkedQueue<ChatMessage>();
		listeners = new Vector<ReceiveMsgListener>();
		mConnectUserChangedListeners = new ArrayList<ConnectedUserChangedListener>();
		mHandler = new Handler(Looper.getMainLooper()) {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {

				case REFRESH_CONNECTED_USER_LIST:
					notifyConnectedUserChange();
					break;

				default:
					break;
				}
				super.handleMessage(msg);
			}

		};
	}

	private void notifyConnectedUserChange() {
		for (ConnectedUserChangedListener listener : mConnectUserChangedListeners) {
			if (listener != null) {
				listener.connectedUserChanged(getUsers());
			}
		}
	}

	public static NetUdpThreadHelper newInstance() {
		if (instance == null) {
			instance = new NetUdpThreadHelper();
		}
		return instance;
	}

	public void resetNetThreadHelper() {
		users.clear();
		userActivity.clear();
	}

	public Map<String, User> getUsers() {
		return users;
	}

	public User getUser(String userIp) {
		return users.get(userIp);
	}

	public Queue<ChatMessage> getReceiveMsgQueue() {
		return receiveMsgQueue;
	}

	void addMsgToQueue(ChatMessage msg) {
		receiveMsgQueue.add(msg);
	}
	
	public void registerTcpFileTransferListener(TcpFileTransferListener listener) {
		this.listener = listener;
	}

	// 添加listener到容器中
	public void addReceiveMsgListener(ReceiveMsgListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	// 从容器中移除相应listener
	public void removeReceiveMsgListener(ReceiveMsgListener listener) {
		if (listeners.contains(listener)) {
			listeners.remove(listener);
		}
	}

	public void addConnectedUserChangedListener(ConnectedUserChangedListener listener) {
		if (!mConnectUserChangedListeners.contains(listener)) {
			mConnectUserChangedListeners.add(listener);
		}
	}
	
	public void removeConnectedUserChangedListener(ConnectedUserChangedListener listener) {
		if (mConnectUserChangedListeners.contains(listener)) {
			mConnectUserChangedListeners.remove(listener);
		}
	}
	
	/**
	 * 
	 * 此方法用来判断是否有处于前台的聊天窗口对应的activity来接收收到的数据。
	 */
	boolean receiveMsg(ChatMessage msg) {
		for (int i = 0; i < listeners.size(); i++) {
			ReceiveMsgListener listener = listeners.get(i);
			if (listener.receive(msg)) {
				return true;
			}
		}
		return false;
	}

	private void noticeOnline() { // 发送上线广播
		if (!threadWorking) {
			return;
		}
		GoTransferApplication app = GoTransferApplication.getInstance();

		UdpMessage ipmsgSend = new UdpMessage();
		ipmsgSend.commandNo = Command.BR_ENTRY; // 上线命令
		ipmsgSend.senderName = app.getSelfName();
		ipmsgSend.senderPlatform = app.getSelfGroup();
		ipmsgSend.senderMac = app.getSelfMac();

		Log.d(TAG, "noticeOnLine >>> " + ipmsgSend.toMessageString());

		InetAddress broadcastAddr;
		try {
			broadcastAddr = InetAddress.getByName(app.getBroadcastAddress()); // 广播地址
			sendUdpData(ipmsgSend.toMessageString() + "\0", broadcastAddr, MessageConst.PORT,
					null); // 发送数据
		} catch (UnknownHostException e) {
			e.printStackTrace();
			Log.e(TAG, "noticeOnline()....广播地址有误");
		}
	}

	public void refreshUsers() { // 刷新在线用户
		Log.d(TAG, "refresh users");

		if (!threadWorking) {
			return;
		}

		// 删除不活跃用户, 未响应时间>2*notifyOnlinePeriod
		Date now = new Date();
		ArrayList<String> removeKeys = new ArrayList<String>();
		for (String k : userActivity.keySet()) {
			Date lastActivity = userActivity.get(k);
			if (now.getTime() - lastActivity.getTime() >= notifyOnlinePeriod * 2) {
				removeKeys.add(k);
			}
		}
		boolean needUpdate = false;
		for (String k : removeKeys) {
			if (users.containsKey(k)) {
				users.remove(k);
				needUpdate = true;
			}
			userActivity.remove(k);
		}

		Log.d(TAG, "" + getUsers());

		if (needUpdate) {
			Message msg = mHandler.obtainMessage(REFRESH_CONNECTED_USER_LIST);
			msg.sendToTarget();
		}

		noticeOnline(); // 发送上线通知
	}

	public boolean connectSocket() { // 监听端口，接收UDP数据
		boolean result = false;
		WifiApManager.getInstance().mWifiLock.acquire();
		try {
			if (udpSocket == null) {
				udpSocket = new DatagramSocket(null); // 绑定端口
				udpSocket.setReuseAddress(true);
				udpSocket.bind(new InetSocketAddress(MessageConst.PORT));
				Log.d(TAG, "connectSocket()....绑定UDP端口" + MessageConst.PORT + "成功");
			}
			startThread(); // 启动线程接收UDP数据
			result = true;
			threadWorking = true;
			if (mTask == null) {
				mTask = new TimerTask() {
					public void run() {
						refreshUsers();// 定时刷新在线列表
					}
				};
				// TODO: 没有唤醒CPU，屏幕关闭CPU就会休眠然后就不会进行轮询了
				timer = new Timer(true);
				timer.schedule(mTask, 500, notifyOnlinePeriod);
			}
			
		} catch (SocketException e) {
			e.printStackTrace();
			disconnectSocket();
			Log.e(TAG, "connectSocket()....绑定UDP端口" + MessageConst.PORT + "失败");
			result = false;
		}

		return result;
	}

	@SuppressLint("HandlerLeak")
	Handler myHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Command.THREAD_MESSAGE_END:
				stopThread();
				threadWorking = false;
				Bundle data = msg.getData();
				if (data != null) {
					long packetNo = data.getLong("packetNo");
					if (mSendFileUdpThreadMap != null) {
						mSendFileUdpThreadMap.remove(packetNo);
					}
				}
				
				break;
			}
			super.handleMessage(msg);
		}
	};

	public void disconnectSocket() { // 停止监听UDP数据
		if (!threadWorking) {
			return;
		}
		if (timer != null) {
			mTask.cancel();
			timer.cancel();
			timer.purge();
			timer = null;
			mTask = null;
		}
		sendExitUdpMessage();
	}
	
	public void sendExitUdpMessage() {
		GoTransferApplication app = GoTransferApplication.getInstance();

		UdpMessage ipmsgSend = new UdpMessage();
		ipmsgSend.commandNo = Command.BR_EXIT; // 下线命令
		ipmsgSend.senderName = app.getSelfName();
		ipmsgSend.senderPlatform = app.getSelfGroup();
		ipmsgSend.senderMac = app.getSelfMac();

		InetAddress broadcastAddr;
		try {
			broadcastAddr = InetAddress.getByName(GoTransferApplication.getInstance()
					.getBroadcastAddress()); // 广播地址
			sendUdpData(ipmsgSend.toMessageString() + "\0", broadcastAddr, MessageConst.PORT,
					myHandler);
			// TODO: delete the following codes
			// Thread udpSendThread = new Thread(new NetUdpSendThread(udpSocket,
			// ipmsgSend.getProtocolString() + "\0", broadcastAddr,
			// IpMessageConst.PORT, myHandler));
			// udpSendThread.start();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			Log.e(TAG, "noticeOnline()....广播地址有误");
		}
	}

	private void stopThread() { // 停止线程
		if (netUdpReceiveThread != null) {
			netUdpReceiveThread.onWork = false;
		}
		if (udpThread != null) {
			udpThread.interrupt(); // 若线程堵塞，则中断
		}
		cleanDeadReveiveThread();
		resetNetThreadHelper();
		Log.d(TAG, "停止监听UDP数据");
		WifiApManager.getInstance().mWifiLock.release();
	}

	private void startThread() { // 启动线程
		if (udpThread == null) {
			netUdpReceiveThread = new NetUdpReceiveThread(this, udpSocket, listener);
			udpThread = new Thread(netUdpReceiveThread);
			udpThread.start();
			Log.d(TAG, "正在监听UDP数据");
		}
	}
	
	void sendUdpData(String sendStr, InetAddress sendto, int sendPort, Handler myHandler) { // 发送UDP数据包的方法
		if (!threadWorking) {
			return;
		}

		NetUdpSendThread runnable = new NetUdpSendThread(udpSocket, sendStr, sendto,
				sendPort, myHandler);
		Thread udpSendThread = new Thread(runnable);
		udpSendThread.start();
		
		UdpMessage ipmsgSend = new UdpMessage(sendStr);
		if (ipmsgSend.commandNo == Command.SEND_FILE_LIST) {
			if (mSendFileUdpThreadMap == null) {
				mSendFileUdpThreadMap = new HashMap<Long, NetUdpSendThread>();
			}
			mSendFileUdpThreadMap.put(ipmsgSend.getPacketNo(), runnable);
		}
		
	}

	void addUser(UdpMessage ipmsgPro, String userIp) { // 添加用户到Users的Map中
		String userMac = ipmsgPro.senderMac;

		boolean needUpdate = !users.containsKey(userIp);

		User user = new User();
		user.setAlias(ipmsgPro.senderName); // 别名暂定发送者名称

		user.setUserName(ipmsgPro.senderName);
		user.setGroupName(ipmsgPro.senderPlatform);
		user.setIp(userIp);
		user.setHostName(userMac);
		user.setMac(ipmsgPro.senderMac); // 暂时没用这个字段
		users.put(userIp, user);
		Log.d(TAG, "成功添加ip为" + userIp + "的用户");

		userActivity.put(userIp, new Date());

		if (needUpdate) {
//			Intent newIntent = new Intent(Const.BROADCAST_ACTION_REFRESH_USER_LIST);
//			GoTransferApplication.getInstance().sendBroadcast(newIntent);
			
			Message msg = mHandler.obtainMessage(REFRESH_CONNECTED_USER_LIST);
			msg.sendToTarget();
		}
	}

	void removeUser(String userIp) {
		boolean needUpdate = users.containsKey(userIp);
		users.remove(userIp);

		if (needUpdate) {
//			Intent newIntent = new Intent(Const.BROADCAST_ACTION_REFRESH_USER_LIST);
//			GoTransferApplication.getInstance().sendBroadcast(newIntent);
			
			Message msg = mHandler.obtainMessage(REFRESH_CONNECTED_USER_LIST);
			msg.sendToTarget();
		}
	}

	void cleanDeadReveiveThread() {
		if (udpSocket != null) {
			udpSocket.close();
			udpSocket = null;
		}
		udpThread = null;
		netUdpReceiveThread = null;
	}

	public void refuseReceiveFiles(String ipAddress, long packetNo) {
		UdpMessage ipmsgSend = new UdpMessage();
		ipmsgSend.commandNo = Command.REFUSE_RECEIVE;
		ipmsgSend.senderName = GoTransferApplication.getInstance().getSelfName();
		ipmsgSend.senderPlatform = GoTransferApplication.getInstance().getSelfGroup();
		ipmsgSend.senderMac = GoTransferApplication.getInstance().getSelfMac();
		Map<String, Object> addi = new HashMap<String, Object>();
		addi.put(Args.PACKET_NUMBER, String.valueOf(packetNo));
		ipmsgSend.additionalSection = addi;

		InetAddress sendAddress = null;
		try {
			sendAddress = InetAddress.getByName(ipAddress);
		} catch (UnknownHostException e) {
			Log.e(TAG, "发送地址有误");
		}
		if (sendAddress != null) {
			sendUdpData(ipmsgSend.toMessageString(), sendAddress, MessageConst.PORT, null);
		}
	}

	// 返回 packetNo
	public long sendFiles(String ipAddress, String msg, int support,
			List<Map<String, Object>> fileList) {
		GoTransferApplication app = GoTransferApplication.getInstance();

		// 发送传送文件UDP数据报
		UdpMessage sendPro = new UdpMessage();
		sendPro.commandNo = Command.SEND_FILE_LIST;
		sendPro.senderName = app.getSelfName();
		sendPro.senderPlatform = app.getSelfGroup();
		sendPro.senderMac = app.getSelfMac();
		
		Map<String, Object> addi = new HashMap<String, Object>();
		addi.put(Args.MSG_TEXT, msg);
		addi.put(Args.MSG_SUPPORT, support);
		addi.put(Args.FILE_LIST, fileList);
		sendPro.additionalSection = addi;

		InetAddress sendto = null;
		try {
			sendto = InetAddress.getByName(ipAddress);
		} catch (UnknownHostException e) {
			Log.e(TAG, "发送地址有误");
		}
		if (sendto != null) {
			sendUdpData(sendPro.toMessageString(), sendto, MessageConst.PORT, null);
			return sendPro.getPacketNo();
		}
		return 0;
	}
	
	public void sendReceiveFileUdpPacket(long paketNo, String ipAddress) {
		UdpMessage sendPro = new UdpMessage();
		sendPro.commandNo = Command.RESPONSE_SEND_FILE_REQUEST;
		sendPro.senderName = GoTransferApplication.getInstance().getSelfName();
		sendPro.senderPlatform = GoTransferApplication.getInstance().getSelfGroup();
		sendPro.senderMac = GoTransferApplication.getInstance().getSelfMac();
		Map<String, Object> addi = new HashMap<String, Object>();
		addi.put(Args.PACKET_NUMBER, String.valueOf(paketNo));
		sendPro.additionalSection = addi;

		InetAddress sendto = null;
		try {
			sendto = InetAddress.getByName(ipAddress);
		} catch (UnknownHostException e) {
			Log.e(TAG, "发送地址有误");
		}
		if (sendto != null) {
			sendUdpData(sendPro.toMessageString(), sendto, MessageConst.PORT, null);
		}
	}

	public Map<Long, NetUdpSendThread> getSendFileUdpThreadMap() {
		return mSendFileUdpThreadMap;
	}
	
	public void cancelSendingWhenNotStart(String ipAddress, long packetNo) {
		GoTransferApplication app = GoTransferApplication.getInstance();

		// 发送传送文件UDP数据报
		UdpMessage sendPro = new UdpMessage();
		sendPro.commandNo = Command.CANCEL_TRANSFER;
		sendPro.senderName = app.getSelfName();
		sendPro.senderPlatform = app.getSelfGroup();
		sendPro.senderMac = app.getSelfMac();
		Map<String, Object> addi = new HashMap<String, Object>();
		addi.put(Args.PACKET_NUMBER, String.valueOf(packetNo));
		sendPro.additionalSection = addi;

		InetAddress sendto = null;
		try {
			sendto = InetAddress.getByName(ipAddress);
		} catch (UnknownHostException e) {
			Log.e(TAG, "发送地址有误");
		}
		if (sendto != null) {
			sendUdpData(sendPro.toMessageString(), sendto, MessageConst.PORT, null);
		}
	}

	public interface ConnectedUserChangedListener {

		public void connectedUserChanged(Map<String, User> user);

	}
}
