package cn.m15.gotransfer.sdk.net.ipmsg;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
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
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import cn.m15.gotransfer.sdk.ConfigManager;
import cn.m15.gotransfer.sdk.entity.TransferFile;
import cn.m15.gotransfer.sdk.net.ipmsg.MessageConst.Args;
import cn.m15.gotransfer.sdk.net.ipmsg.MessageConst.Command;
import cn.m15.gotransfer.sdk.net.wifi.WifiApManager;
import cn.m15.gotransfer.sdk.utils.LogManager;

/**
 * 网络通信辅助类 实现UDP通信以及UDP端口监听 端口监听采用多线程方式
 * 
 * 单例模式
 * 
 */

public class UdpThreadManager {
	public static final String TAG = "NetUdpThreadHelper";

	private static final int REFRESH_CONNECTED_USER_LIST = 1;
	private static UdpThreadManager instance;

	private Thread udpThread = null; // 接收UDP数据线程
	private UdpReceiveThread netUdpReceiveThread;
	private Map<Long, UdpSendThread> mSendFileUdpThreadMap;
	private DatagramSocket udpSocket = null; // 用于接收和发送UDP数据的socket

	private Map<String, TUser> users; // 当前所有用户的集合，以IP为KEY
	private Map<String, Date> userActivity; // 用户活跃时间

	private Queue<ChatMessage> receiveMsgQueue; // 消息队列,在没有聊天窗口时将接收的消息放到这个队列中
	private Vector<ReceiveMsgListener> listeners; // ReceiveMsgListener容器，当一个聊天窗口打开时，将其加入。一定要记得适时将其移除

	private Handler mHandler;
	private List<ConnectedUserChangedListener> mConnectUserChangedListeners;

	private boolean threadWorking = false;
	private Timer timer;
	private TimerTask mTask;
	private static final long notifyOnlinePeriod = 5000;
	
	private TcpFileTransferListener listener;

	private UdpThreadManager() {
		users = Collections.synchronizedMap(new HashMap<String, TUser>());
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

	public static UdpThreadManager getInstance() {
		if (instance == null) {
			instance = new UdpThreadManager();
		}
		return instance;
	}

	public void resetNetThreadHelper() {
		users.clear();
		userActivity.clear();
	}

	public Map<String, TUser> getUsers() {
		return users;
	}

	public TUser getUser(String userIp) {
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
		ConfigManager cm = ConfigManager.getInstance();

		UdpMessage ipmsgSend = new UdpMessage();
		ipmsgSend.commandNo = Command.BR_ENTRY; // 上线命令
		ipmsgSend.senderName = cm.getSelfName();
		ipmsgSend.senderPlatform = cm.getSelfGroup();
		ipmsgSend.senderMac = cm.getSelfMac();

		LogManager.d(TAG, "noticeOnLine >>> " + ipmsgSend.toMessageString());

		InetAddress broadcastAddr;
		try {
			broadcastAddr = InetAddress.getByName(cm.getBroadcastAddress()); // 广播地址
			sendUdpData(ipmsgSend.toMessageString(), broadcastAddr, MessageConst.UDP_PORT, null); // 发送数据 
		} catch (UnknownHostException e) {
			e.printStackTrace();
			LogManager.e(TAG, "noticeOnline()....广播地址有误");
		}
	}

	public void refreshUsers() { // 刷新在线用户
		LogManager.d(TAG, "refresh users");

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

		LogManager.d(TAG, "" + getUsers());

		if (needUpdate) {
			Message msg = mHandler.obtainMessage(REFRESH_CONNECTED_USER_LIST);
			msg.sendToTarget();
		}

		noticeOnline(); // 发送上线通知
	}

	public boolean connectSocket() { // 监听端口，接收UDP数据
		if (threadWorking) return true;
		WifiApManager.getInstance().mWifiLock.acquire();
		boolean result = false;
		try {
			if (udpSocket == null) {
				udpSocket = new DatagramSocket(null); // 绑定端口
				udpSocket.setReuseAddress(true);
				udpSocket.setSoTimeout((int) notifyOnlinePeriod);
				udpSocket.bind(new InetSocketAddress(MessageConst.UDP_PORT));
				LogManager.d(TAG, "connectSocket()....绑定UDP端口" + MessageConst.UDP_PORT + "成功");
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
				timer = new Timer(true);
				timer.schedule(mTask, 500, notifyOnlinePeriod);
			}
			
		} catch (SocketException e) {
			e.printStackTrace();
			disconnectSocket();
			LogManager.e(TAG, "connectSocket()....绑定UDP端口" + MessageConst.UDP_PORT + "失败");
			result = false;
		}

		return result;
	}

	@SuppressLint("HandlerLeak")
	Handler myHandler = new Handler() {
		public void handleMessage(Message msg) {
			LogManager.e(TAG, "接收到消息："+msg);
			switch (msg.what) {
			case Command.THREAD_MESSAGE_END:
				stopThread();
				threadWorking = false;
				if (msg.obj != null) {
					if (mSendFileUdpThreadMap != null) {
						mSendFileUdpThreadMap.remove((Long) msg.obj);
					}
				}
				break;
			}
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
		if (users != null) {
			users.clear();
		}
		if (userActivity != null) {
			userActivity.clear();
		}
	}
	
	public void sendExitUdpMessage() {
		ConfigManager cm = ConfigManager.getInstance();

		UdpMessage ipmsgSend = new UdpMessage();
		ipmsgSend.commandNo = Command.BR_EXIT; // 下线命令
		ipmsgSend.senderName = cm.getSelfName();
		ipmsgSend.senderPlatform = cm.getSelfGroup();
		ipmsgSend.senderMac = cm.getSelfMac();

		InetAddress broadcastAddr;
		try {
			broadcastAddr = InetAddress.getByName(cm.getBroadcastAddress()); // 广播地址
			sendUdpData(ipmsgSend.toMessageString(), broadcastAddr, MessageConst.UDP_PORT, myHandler);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			LogManager.e(TAG, "noticeOnline()....广播地址有误");
		}
	}

	private void stopThread() { // 停止线程
		LogManager.e(TAG, "停止监听UDP数据");
		if (netUdpReceiveThread != null) {
			netUdpReceiveThread.onWork = false;
		}
		if (udpThread != null) {
			udpThread.interrupt(); // 若线程堵塞，则中断
		}
		cleanDeadReveiveThread();
		resetNetThreadHelper();
		MulticastLock wifiLock = WifiApManager.getInstance().mWifiLock;
		if (wifiLock.isHeld()) {
			wifiLock.release(); // 应用保持一个wifiLock就好了
		}
	}

	private void startThread() { // 启动线程
		if (udpThread == null) {
			netUdpReceiveThread = new UdpReceiveThread(this, udpSocket, listener);
			udpThread = new Thread(netUdpReceiveThread);
			udpThread.start();
			LogManager.d(TAG, "正在监听UDP数据");
		}
	}
	
	void sendUdpData(String sendStr, InetAddress sendto, int sendPort, Handler myHandler) { // 发送UDP数据包的方法
		if (!threadWorking) {
			return;
		}

		UdpSendThread runnable = new UdpSendThread(udpSocket, sendStr, sendto,
				sendPort, myHandler);
		Thread udpSendThread = new Thread(runnable);
		udpSendThread.start();
		
		UdpMessage ipmsgSend = new UdpMessage(sendStr);
		if (ipmsgSend.commandNo == Command.SEND_FILE_LIST) {
			if (mSendFileUdpThreadMap == null) {
				mSendFileUdpThreadMap = new HashMap<Long, UdpSendThread>();
			}
			mSendFileUdpThreadMap.put(ipmsgSend.getPacketNo(), runnable);
		}
		
	}

	void addUser(UdpMessage ipmsgPro, String userIp) { // 添加用户到Users的Map中
		String userMac = ipmsgPro.senderMac;

		boolean needUpdate = !users.containsKey(userIp);

		TUser user = new TUser();
		user.setAlias(ipmsgPro.senderName); // 别名暂定发送者名称

		user.setUserName(ipmsgPro.senderName);
		user.setGroupName(ipmsgPro.senderPlatform);
		user.setIp(userIp);
		user.setHostName(userMac);
		user.setMac(ipmsgPro.senderMac); // 暂时没用这个字段
		users.put(userIp, user);
		LogManager.d(TAG, "成功添加ip为" + userIp + "的用户");

		userActivity.put(userIp, new Date());

		if (needUpdate) {
			Message msg = mHandler.obtainMessage(REFRESH_CONNECTED_USER_LIST);
			msg.sendToTarget();
		}
	}

	void removeUser(String userIp) {
		boolean needUpdate = users.containsKey(userIp);
		users.remove(userIp);

		if (needUpdate) {
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
		
		ConfigManager cm = ConfigManager.getInstance();
		
		UdpMessage ipmsgSend = new UdpMessage();
		ipmsgSend.commandNo = Command.REFUSE_RECEIVE;
		ipmsgSend.senderName = cm.getSelfName();
		ipmsgSend.senderPlatform = cm.getSelfGroup();
		ipmsgSend.senderMac = cm.getSelfMac();
		Map<String, Object> addi = new HashMap<String, Object>();
		addi.put(Args.PACKET_NUMBER, packetNo);
		ipmsgSend.additionalSection = addi;

		InetAddress sendAddress = null;
		try {
			sendAddress = InetAddress.getByName(ipAddress);
		} catch (UnknownHostException e) {
			LogManager.e(TAG, "发送地址有误");
		}
		if (sendAddress != null) {
			sendUdpData(ipmsgSend.toMessageString(), sendAddress, MessageConst.UDP_PORT, null);
		}
	}

	// 返回 packetNo
	public long sendFiles(String ipAddress, String msg, int support, ArrayList<TransferFile> fileList) {
		ConfigManager cm = ConfigManager.getInstance();

		// 发送传送文件UDP数据报
		UdpMessage sendPro = new UdpMessage();
		sendPro.commandNo = Command.SEND_FILE_LIST;
		sendPro.senderName = cm.getSelfName();
		sendPro.senderPlatform = cm.getSelfGroup();
		sendPro.senderMac = cm.getSelfMac();
		
		Map<String, Object> addi = new HashMap<String, Object>();
		addi.put(Args.MSG_TEXT, msg);
		addi.put(Args.MSG_SUPPORT, support);
		addi.put(Args.FILE_LIST, fileList);
		sendPro.additionalSection = addi;

		InetAddress sendto = null;
		try {
			sendto = InetAddress.getByName(ipAddress);
		} catch (UnknownHostException e) {
			LogManager.e(TAG, "发送地址有误");
		}
		if (sendto != null) {
			String messageStr = sendPro.toMessageString();
			LogManager.d(TAG, "即将发送UDP数据：" + messageStr);
			sendUdpData(messageStr, sendto, MessageConst.UDP_PORT, null);
			return sendPro.getPacketNo();
		}
		return 0;
	}
	
	public void sendReceiveFileUdpPacket(long paketNo, String ipAddress) {
		
		ConfigManager cm = ConfigManager.getInstance();
		
		UdpMessage sendPro = new UdpMessage();
		sendPro.commandNo = Command.RESPONSE_SEND_FILE_REQUEST;
		sendPro.senderName = cm.getSelfName();
		sendPro.senderPlatform = cm.getSelfGroup();
		sendPro.senderMac = cm.getSelfMac();
		Map<String, Object> addi = new HashMap<String, Object>();
		addi.put(Args.PACKET_NUMBER, paketNo);
		sendPro.additionalSection = addi;

		InetAddress sendto = null;
		try {
			sendto = InetAddress.getByName(ipAddress);
		} catch (UnknownHostException e) {
			LogManager.e(TAG, "发送地址有误");
		}
		if (sendto != null) {
			sendUdpData(sendPro.toMessageString(), sendto, MessageConst.UDP_PORT, null);
		}
	}

	public Map<Long, UdpSendThread> getSendFileUdpThreadMap() {
		return mSendFileUdpThreadMap;
	}
	
	public void cancelSendingWhenNotStart(String ipAddress, long packetNo) {
		ConfigManager cm = ConfigManager.getInstance();

		// 发送传送文件UDP数据报
		UdpMessage sendPro = new UdpMessage();
		sendPro.commandNo = Command.CANCEL_TRANSFER;
		sendPro.senderName = cm.getSelfName();
		sendPro.senderPlatform = cm.getSelfGroup();
		sendPro.senderMac = cm.getSelfMac();
		Map<String, Object> addi = new HashMap<String, Object>();
		addi.put(Args.PACKET_NUMBER, packetNo);
		sendPro.additionalSection = addi;

		InetAddress sendto = null;
		try {
			sendto = InetAddress.getByName(ipAddress);
		} catch (UnknownHostException e) {
			LogManager.e(TAG, "发送地址有误");
		}
		if (sendto != null) {
			sendUdpData(sendPro.toMessageString(), sendto, MessageConst.UDP_PORT, null);
		}
	}

	public interface ConnectedUserChangedListener {

		public void connectedUserChanged(Map<String, TUser> user);

	}
}
