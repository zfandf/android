package cn.m15.gotransfer.sdk.net.ipmsg;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;
import cn.m15.gotransfer.sdk.ConfigManager;
import cn.m15.gotransfer.sdk.SdkConst;
import cn.m15.gotransfer.sdk.entity.TransferFile;
import cn.m15.gotransfer.sdk.net.ipmsg.MessageConst.Args;
import cn.m15.gotransfer.sdk.net.ipmsg.MessageConst.Command;
import cn.m15.gotransfer.sdk.net.ipmsg.MessageConst.UdpMessageConst;
import cn.m15.gotransfer.sdk.utils.LogManager;

public class UdpReceiveThread implements Runnable {
	public static final String TAG = "NetUdpReceiveThread";
	public boolean onWork = true; // 线程工作标识
	
	private DatagramSocket udpSocket = null; // 用于接收和发送udp数据的socket
	private DatagramPacket udpResPacket = null; // 用于接收的udp数据包

	private byte[] resBuffer = new byte[UdpMessageConst.PACKET_MAX_LENGTH]; // 接收数据的缓存
	private UdpThreadManager netThreadHelper;
	private HashMap<Long, SparseArray<byte[]>> multiPackets;
	private HashSet<Long> receiveFileCache;
	
	private TcpFileTransferListener listener;

	public UdpReceiveThread(UdpThreadManager threadHelper, DatagramSocket socket, TcpFileTransferListener listener) {
		netThreadHelper = threadHelper;
		udpSocket = socket;
		udpResPacket = new DatagramPacket(resBuffer, UdpMessageConst.PACKET_MAX_LENGTH);
		multiPackets = new HashMap<Long, SparseArray<byte[]>>();
		this.listener = listener;
	}

	@Override
	public void run() {
		while (onWork) {
			try {
				udpSocket.receive(udpResPacket);
			} catch (SocketException e) {
				onWork = false;
				LogManager.e(TAG, "UDP数据包接收失败！线程停止");
				break;
			} catch (SocketTimeoutException e) {
				e.printStackTrace();
				notifySendFileThreadResult(0, UdpSendThread.SEND_FAILED);
				continue;
			} catch (IOException e) {
				onWork = false;
				LogManager.e(TAG, "UDP数据包接收失败！线程停止");
				break;
			}

			if (udpResPacket.getLength() == 0) {
				LogManager.d(TAG, "无法接收UDP数据或者接收到的UDP数据为空");
				continue;
			}

			try {
				// 组包
				ByteArrayInputStream bios = new ByteArrayInputStream(resBuffer);
				DataInputStream ios = new DataInputStream(bios);
				@SuppressWarnings("unused")
				short version = ios.readShort(); // 分包版本
				long packetId = ios.readLong(); // 包ID
				int packetCount = ios.readInt(); // 总包数
				int packetIndex = ios.readInt(); // 包序号
				ios.read(new byte[14]); // 预留
				byte[] packetData = new byte[UdpMessageConst.PACKET_MAX_LENGTH - UdpMessageConst.PACKET_HRED_LENGTH];
				// read method will do something.
				ios.read(packetData);
				
				if (packetCount == 1) {
					processData(packetData);
				} else {
					SparseArray<byte[]> packetMap = multiPackets.get(packetId);
					if (packetMap == null) {
						packetMap = new SparseArray<byte[]>();
						multiPackets.put(packetId, packetMap);
					}
					packetMap.put(packetIndex, packetData);

					if (packetMap.size() == packetCount) { // 全部接收，组包
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						DataOutputStream dos = new DataOutputStream(baos);

						for (int i = 0; i < packetCount; i++) {
							dos.write(packetMap.get(i));
						}

						dos.close();

						byte[] finalPacket = baos.toByteArray();
						processData(finalPacket);
						baos.close();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				LogManager.e(TAG, "读取分包UDP数据包失败, ip:" + udpResPacket.getAddress().getHostAddress());
			}

			if (udpResPacket != null) { // 每次接收完UDP数据后，重置长度。否则可能会导致下次收到数据包被截断。
				udpResPacket.setLength(UdpMessageConst.PACKET_MAX_LENGTH);
			}
		}

		if (udpResPacket != null) {
			udpResPacket = null;
		}

		if (udpSocket != null) {
			udpSocket.close();
			udpSocket = null;
		}

		netThreadHelper.cleanDeadReveiveThread();
	}

	private void processData(byte[] receivedData) {
		ConfigManager cm = ConfigManager.getInstance();
		
		String ipmsgStr = decompress(receivedData);
		// 截取收到的数据
		LogManager.d(TAG, "接收到的UDP数据内容为:" + ipmsgStr + ", length:" + receivedData.length);
		UdpMessage ipmsgPro = new UdpMessage(ipmsgStr);
		int commandNo = ipmsgPro.commandNo;
		String userIp = udpResPacket.getAddress().getHostAddress(); // 得到发送者IP
		String userMac = ipmsgPro.senderMac;
		Map<String, Object> extraMsg = ipmsgPro.additionalSection;

		switch (commandNo) {
		case Command.BR_ENTRY: // 收到上线数据包，添加用户，并回送ANSENTRY应答。
			if (userMac == null) return;
			if (!userMac.equals(cm.getSelfMac())) {
				netThreadHelper.addUser(ipmsgPro, userIp); // 添加用户

				// 下面构造回送报文内容
				UdpMessage ipmsgSend = new UdpMessage();
				ipmsgSend.commandNo = Command.ANSENTRY; // 回送报文命令
				ipmsgSend.senderName = cm.getSelfName();
				ipmsgSend.senderPlatform = cm.getSelfGroup();
				ipmsgSend.senderMac = cm.getSelfMac();

				netThreadHelper.sendUdpData(ipmsgSend.toMessageString(), udpResPacket.getAddress(),
						udpResPacket.getPort(), null); // 发送数据
			}
			break;

		case Command.ANSENTRY: // 收到上线应答，更新在线用户列表
			netThreadHelper.addUser(ipmsgPro, userIp);
			break;

		case Command.BR_EXIT: // 收到下线广播，删除users中对应的值
			netThreadHelper.removeUser(userIp);
			LogManager.d(TAG, "根据下线报文成功删除ip为" + userIp + "的用户");
			break;

		case Command.SENDMSG: // 收到消息，处理
			String senderName = ipmsgPro.senderName; // 得到发送者的名称
			Map<String, Object> additionals = ipmsgPro.additionalSection; // 得到附加信息
			Date time = new Date(); // 收到信息的时间
			String msgTemp = ""; // 直接收到的消息，根据加密选项判断是否是加密消息
			String msgStr = ""; // 解密后的消息内容

			// 构造通报收到消息报文
			UdpMessage ipmsgSend = new UdpMessage();
			ipmsgSend.commandNo = Command.RECVMSG; // 回送报文命令
			ipmsgSend.senderName = cm.getSelfName();
			ipmsgSend.senderPlatform = cm.getSelfGroup();
			ipmsgSend.senderMac = cm.getSelfMac();

			Map<String, Object> addi = new HashMap<String, Object>();
			addi.put(Args.PACKET_NUMBER, Long.valueOf(ipmsgPro.getPacketNo()));
			ipmsgSend.additionalSection = addi;

			netThreadHelper.sendUdpData(ipmsgSend.toMessageString(), udpResPacket.getAddress(),
					udpResPacket.getPort(), null); // 发送数据

			Object msgObject = additionals.get(Args.MSG_TEXT);
			if (msgObject != null) {
				msgTemp = (String) msgObject;
			}

			// 是否有加密选项，暂缺
			msgStr = msgTemp;

			// 若只是发送消息，处理消息
			ChatMessage msg = new ChatMessage(userIp, senderName, userMac, msgStr, time);
			if (!netThreadHelper.receiveMsg(msg)) { // 没有聊天窗口对应的activity
				netThreadHelper.addMsgToQueue(msg); // 添加到信息队列
				// TODO: 播放声音
				Intent newIntent = new Intent(SdkConst.BROADCAST_ACTION_RECEIVE_MSG);
				ConfigManager.getInstance().getApplicationContext().sendBroadcast(newIntent);
			}
			break;
		case Command.SEND_FILE_LIST:
			if (receiveFileCache == null) {
				receiveFileCache = new HashSet<Long>();
				receiveFileCache.add(ipmsgPro.getPacketNo());
			} else {
				if (receiveFileCache.contains(ipmsgPro.getPacketNo())) {
					return;
				} else {
					receiveFileCache.add(ipmsgPro.getPacketNo());
				}
			}
			// 下面进行发送文件相关处理
			@SuppressWarnings("unchecked")
			ArrayList<TransferFile> fileList = (ArrayList<TransferFile>) extraMsg.get(Args.FILE_LIST);
			if (fileList != null) {
				LogManager.d(TAG, "received 'send file list': " + Arrays.toString(fileList.toArray()));
				int size = fileList.size();
				for (int i = 0; i < size; i++) { // 循环每个文件
					TransferFile file = fileList.get(i);
					file.transfer_status = TransferFile.TRANSFER_WAIT_RECEIVE;
				}	
				
				LogManager.d(TAG, "received 'send file list' 2: send broadcast");
				
				Intent intent = new Intent(SdkConst.BROADCAST_ACTION_RECEIVE_FILE);
				cm.getApplicationContext().sendBroadcast(intent);
				
				if (listener != null) {
					listener.onReceiveSendFileUdp(ipmsgPro, fileList);
				}

				UdpThreadManager.getInstance().sendReceiveFileUdpPacket(ipmsgPro.getPacketNo(), userIp);
			}
			break;
		case Command.REFUSE_RECEIVE: // 拒绝接收文件
			Long packetNo = Long.parseLong(String.valueOf(extraMsg.get(Args.PACKET_NUMBER)));
			if (listener != null && packetNo != null) {
				listener.onRefuseReceive(packetNo);
			}
			break;
		case Command.CANCEL_TRANSFER: // 接收方得知发送方取消发送
			Long packetNo2 = Long.parseLong(String.valueOf(extraMsg.get(Args.PACKET_NUMBER)));
			if (listener != null && packetNo2 != null) {
				listener.onCancelledByPeer(packetNo2);
			}
			break;
		case Command.RESPONSE_SEND_FILE_REQUEST:
			Map<String, Object> a = ipmsgPro.additionalSection;
			if (a != null) {
				Long packetNumber = Long.parseLong(String.valueOf(extraMsg.get(Args.PACKET_NUMBER)));
				if (packetNumber != null && packetNumber > 0) {
					notifySendFileThreadResult(packetNumber, UdpSendThread.SEND_SUCCESS);
				}
			}
			break;
		}
	}

	public static String decompress(byte[] compressed) {
		String result = "";
		if (compressed == null)
			return result;
		try {
			final int BUFFER_SIZE = 2048;
			ByteArrayInputStream is = new ByteArrayInputStream(compressed);
			GZIPInputStream gis = new GZIPInputStream(is, BUFFER_SIZE);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			byte[] data = new byte[BUFFER_SIZE];
			int bytesRead;
			while ((bytesRead = gis.read(data)) != -1) {
				os.write(data, 0, bytesRead);
			}

			result = new String(os.toByteArray(), 0, os.toByteArray().length,
					SdkConst.USEDCHARACTORSET);
			os.close();
			gis.close();
			is.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			LogManager.e(TAG, "接收数据时，系统不支持编码" + SdkConst.USEDCHARACTORSET);
		} catch (IOException e) {
			e.printStackTrace();
			LogManager.e(TAG, "解压缩数据失败");
		}
		return result;
	}

	private void notifySendFileThreadResult(long packetNo, int result) {
		Map<Long, UdpSendThread> sendUdpThreadMap = UdpThreadManager.getInstance()
				.getSendFileUdpThreadMap();
		if (sendUdpThreadMap != null && !sendUdpThreadMap.isEmpty()) {
			if (packetNo > 0) {
				for (UdpSendThread thread : sendUdpThreadMap.values()) {
					sendMessage(thread, result);
				}
			} else {
				UdpSendThread thread = sendUdpThreadMap.get(packetNo);
				if (thread != null) {
					sendMessage(thread, result);
				}
			}
		}

	}

	private void sendMessage(UdpSendThread thread, int result) {
		Handler h = thread.getHandler();
		if (h != null) {
			Message msg = h.obtainMessage();
			msg.what = result;
			msg.sendToTarget();
		}
	}

}