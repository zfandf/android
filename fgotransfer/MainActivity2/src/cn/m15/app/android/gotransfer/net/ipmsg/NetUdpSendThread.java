package cn.m15.app.android.gotransfer.net.ipmsg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import cn.m15.app.android.gotransfer.Const;
import cn.m15.app.android.gotransfer.net.ipmsg2.MessageConst;
import cn.m15.app.android.gotransfer.net.ipmsg2.MessageConst.Command;
import cn.m15.app.android.gotransfer.net.ipmsg2.MessageConst.UdpMessageConst;
import cn.m15.app.android.gotransfer.net.ipmsg2.UdpMessage;

public class NetUdpSendThread implements Runnable {
	public static final String TAG = "NetUdpSendThread";
	
	public static final int SEND_SUCCESS = 1;
	public static final int SEND_FAILED = 2;
	
	private byte[] mSendBuffer = null;
	
	private DatagramSocket mUdpSocket = null; // 用于接收和发送UDP数据的socket
	private DatagramPacket mUdpSendPacket = null; // 用于发送的UDP数据包

	private ArrayList<DatagramPacket> mResendDatagramPackets;

	private String mSendStr = null;
	private InetAddress mSendto = null;
	private int mSendPort = 0;
	private Handler mHandler = null;
	private Handler mDetectSendResultHandler;
	private int mResendCount = 2;

	public NetUdpSendThread(DatagramSocket socket, String sendStr, InetAddress sendto, int sendPort) {
		this(socket, sendStr, sendto, sendPort, null);
	}

	public NetUdpSendThread(DatagramSocket socket, String sendStr, InetAddress sendto,
			int sendPort, Handler handler) {
		mUdpSocket = socket;
		mSendStr = sendStr;
		mSendto = sendto;
		mSendPort = sendPort;
		mHandler = handler;
	}

	@Override
	public void run() {
		final UdpMessage ip = new UdpMessage(mSendStr);
		try {
			mSendBuffer = compress(mSendStr);

			if (mSendBuffer == null) {
				return;
			}

			int dataBodyMaxLength = UdpMessageConst.PACKET_MAX_LENGTH
					- UdpMessageConst.PACKET_HRED_LENGTH;
			int packetCount = mSendBuffer.length / dataBodyMaxLength;
			if (mSendBuffer.length % dataBodyMaxLength != 0) {
				packetCount++;
			}
			
			// 分包
			Date nowDate = new Date();
			long packetId = nowDate.getTime();

			for (int i = 0; i < packetCount; i++) { // 分包
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				DataOutputStream dos = new DataOutputStream(baos);
				dos.writeShort(UdpMessageConst.PACKET_VERSION); // 分包版本，2B
				dos.writeLong(packetId); // 包ID，8B
				dos.writeInt(packetCount);// 分包数，4B
				dos.writeInt(i); // 包序号，4B
				dos.write(new byte[14]); // 预留，14B
				if (i == packetCount - 1) { // 数据体
					int realLength = mSendBuffer.length - i * dataBodyMaxLength;
					dos.write(mSendBuffer, i * dataBodyMaxLength, realLength);
				} else {
					dos.write(mSendBuffer, i * dataBodyMaxLength, dataBodyMaxLength);
				}
				dos.close();

				byte[] sendBytes = baos.toByteArray();
				
				// 构造发送的UDP数据包
				mUdpSendPacket = new DatagramPacket(sendBytes, sendBytes.length, mSendto, mSendPort);
				if (mResendDatagramPackets == null) {
					mResendDatagramPackets = new ArrayList<DatagramPacket>();
				}
				if (!mResendDatagramPackets.contains(mUdpSendPacket)) {
					mResendDatagramPackets.add(mUdpSendPacket);
				}
				mUdpSocket.send(mUdpSendPacket); // 发送UDP数据包
				Log.d(TAG, "向IP为" + mSendto.getHostAddress() + "发送UDP数据包：" + (i + 1) + "/"
						+ packetCount);
				mUdpSendPacket = null;
				
				baos.close();
			}
			
			Looper.prepare();
			
			mDetectSendResultHandler = new Handler() {
				
				@Override
				public void handleMessage(Message msg) {
					switch (msg.what) {
					case SEND_SUCCESS:
						mResendCount = 0;
						break;
						
					case SEND_FAILED:
						if (mResendCount > 0) {
							try {
								resendDataPackage(ip);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						break;
						
					default:
						mResendCount = 0;
						break;
					}
				}
			};
			
			Looper.loop();
			while (mResendCount > 0 && (ip.commandNo == Command.SEND_FILE_LIST || 
					ip.commandNo == Command.BR_EXIT)) {
				
			}

			Log.d(TAG, "成功向IP为" + mSendto.getHostAddress() + "发送UDP数据：" + mSendStr);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			Log.e(TAG, "sendUdpData(String sendStr, int port)....系统不支持编码" + Const.USEDCHARACTORSET);
		} catch (IOException e) { // 发送UDP数据包出错
			e.printStackTrace();
			mUdpSendPacket = null;
			Log.e(TAG,
					"sendUdpData(String sendStr, int port)....发送UDP数据包失败, ip:"
							+ mSendto.getHostAddress());
		}

		if (mHandler != null) {
			Message msg = mHandler.obtainMessage();
			Bundle data = new Bundle();
			data.putLong("packetNo", ip.getPacketNo());
			msg.what = Command.THREAD_MESSAGE_END;
			msg.setData(data);
			msg.sendToTarget();
//			mHandler.sendEmptyMessage(IpMessageConst.IPMSG_THREAD_MESSAGE_END);
		}
	}

	private void resendDataPackage(UdpMessage ip) throws IOException {
		mResendCount--;
		if (ip.commandNo == Command.SEND_FILE_LIST) {
			if (mResendDatagramPackets != null && mResendDatagramPackets.size() > 0) {
				for (DatagramPacket datagramPacket : mResendDatagramPackets) {
					mUdpSocket.send(datagramPacket);
				}
			}
		} else if (ip.commandNo == Command.BR_EXIT) {
			NetUdpThreadHelper.newInstance().sendExitUdpMessage();
		}
	}

	public static byte[] compress(String string) {
		byte[] compressed = null;
		if (string == null)
			return compressed;
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream(string.length());
			GZIPOutputStream gos = new GZIPOutputStream(os);
			gos.write(string.getBytes(Const.USEDCHARACTORSET));
			gos.close();
			compressed = os.toByteArray();
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "压缩数据失败");
		}
		return compressed;
	}
	
	public Handler getHandler() {
		return mDetectSendResultHandler;
	}
}
