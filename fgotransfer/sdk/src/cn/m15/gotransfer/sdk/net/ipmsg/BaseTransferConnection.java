package cn.m15.gotransfer.sdk.net.ipmsg;

import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.m15.gotransfer.sdk.net.ipmsg.MessageConst.TcpMessageConst;

public abstract class BaseTransferConnection {
	public static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();
	// 预留字节数组，占位用
	public static final byte[] UNUSED = new byte[25];
	
	protected ByteBuffer headBuffer;
	protected ByteBuffer dataBuffer;
	protected String tag;
	
	public BaseTransferConnection() {
		tag = "base";
		headBuffer = ByteBuffer.allocate(TcpMessageConst.PACKET_HRED_LENGTH);
		dataBuffer = ByteBuffer.allocate(TcpMessageConst.PACKET_MAX_LENGTH - TcpMessageConst.PACKET_HRED_LENGTH);
	}
	
	/**
	 * 发送TCP消息头
	 * @param sc
	 * @param dataType 数据类型
	 * @param dataLength 消息数据体长度
	 * @throws IOException
	 */
	protected void sendMessageHeader(SocketChannel sc, byte dataType, int dataLength) throws IOException {
		// 组包
		dataBuffer.clear();
		dataBuffer.putShort(TcpMessageConst.PACKET_VERSION)
			  .put(TcpMessageConst.DATA_TYPE_DATA)
			  .putInt(dataLength)
			  .put(UNUSED);
		// 发送
		dataBuffer.flip();
		int wlen = sc.write(dataBuffer);
		while (wlen != TcpMessageConst.PACKET_HRED_LENGTH) {
			wlen += sc.write(dataBuffer);
		}
	}
	
	/**
	 * 发送TCP消息
	 * @param sc
	 * @param dataType 数据类型
	 * @param data 数据字节数组
	 * @throws IOException
	 */
	protected void sendMessage(SocketChannel sc, byte dataType, byte[] data) throws IOException {
		// 组包
		dataBuffer.clear();
		dataBuffer.putShort(TcpMessageConst.PACKET_VERSION)
			  .put(dataType)
			  .putInt(data.length)
			  .put(UNUSED)
			  .put(data);
		// 发送
		dataBuffer.flip();
		int wlen = sc.write(dataBuffer);
		int messageLen = TcpMessageConst.PACKET_HRED_LENGTH + data.length;
		while (wlen != messageLen) {
			wlen += sc.write(dataBuffer);
		}
	}
	
	/**
	 * 接收消息并处理
	 * @param sc
	 * @throws IOException SocketChannel读取失败时抛出，因为对方可能已经关闭TCP连接
	 */
	protected void receiveMessage(SocketChannel sc) throws IOException {
		headBuffer.clear();
		while (headBuffer.hasRemaining()) {
			if (sc.read(headBuffer) == -1) {
				throw new SocketException("Connection is closed.");
			}
		}
		headBuffer.flip();
		int packetVersion = headBuffer.getShort();
		if (packetVersion == TcpMessageConst.PACKET_VERSION) { // 比较分包版本，保持兼容
			byte dataType = headBuffer.get();
			int dataLength = headBuffer.getInt();
			if (dataType == TcpMessageConst.DATA_TYPE_COMMAND) { // 即将接收的是"命令"
				dataBuffer.clear();
				dataBuffer.limit(dataLength); // 限制channel读取的字节数
				while (dataBuffer.hasRemaining()) {
					if (sc.read(dataBuffer) == -1) {
						throw new SocketException("Connection is closed.");
					}
				}
				dataBuffer.flip();
				byte[] data = new byte[dataLength];
				dataBuffer.get(data);
				onReceivedCommand(sc, data);
			} else if (dataType == TcpMessageConst.DATA_TYPE_DATA) { // 即将接收的是"数据"
				onReceivedData(sc, dataLength);
			}
		}
	}
	
	/**
	 * 处理接收到的命令
	 * @param data
	 * @throws IOException
	 */
	protected abstract void onReceivedCommand(SocketChannel sc, byte[] data) throws IOException;
	
	/**
	 * 处理接收的文件数据
	 * @param sc
	 * @param dataLength 数据长度
	 * @throws IOException
	 */
	protected abstract void onReceivedData(SocketChannel sc, int dataLength) throws IOException;
	
}
