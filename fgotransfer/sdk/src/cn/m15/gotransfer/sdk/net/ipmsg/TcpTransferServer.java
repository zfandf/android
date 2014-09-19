package cn.m15.gotransfer.sdk.net.ipmsg;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;

import cn.m15.gotransfer.sdk.net.ipmsg.MessageConst.TcpMessageConst;
import cn.m15.gotransfer.sdk.utils.LogManager;


/**
 * 文件传输服务端，用于发送文件
 */
public class TcpTransferServer implements Runnable {
	public final static String TAG = "TcpFileTransferServer";
	
	private ServerSocketChannel server;
	private Selector selector;
	private Map<Long, TcpTransferConnection> handlerMap; // key=packetNo
	public Map<String, Long> packetNoMap; // key=IP, value=packetNo
	private int sendedFileTotalCount; // 发送文件的总数目	
	private TcpFileTransferListener listener;
	
	public TcpTransferServer(Map<String, Long> packetNoMap, int sendedFileTotalCount) {
		this.packetNoMap = packetNoMap;
		this.handlerMap = new HashMap<Long, TcpTransferConnection>();
		this.sendedFileTotalCount = sendedFileTotalCount;
	}
	
	@Override
	public void run() {
		try {
			if (server != null) {
				try {
					server.close();
				} catch (IOException e) {
				}
			}
			selector = Selector.open();
			server = ServerSocketChannel.open();
			server.configureBlocking(false);
			server.socket().setReuseAddress(true);
			server.socket().setReceiveBufferSize(TcpMessageConst.PACKET_MAX_LENGTH);
			server.socket().bind(new InetSocketAddress(MessageConst.PORT));				
			server.socket().setSoTimeout(Integer.MAX_VALUE);
			server.register(selector, SelectionKey.OP_ACCEPT);
			LogManager.e(TAG, "server >>> open server:" + server.socket().getLocalSocketAddress());
			
			while (server.isOpen() && selector.isOpen()) {
				// select(): ClosedSelectorException不可能会出现，可能会报IOException
				int n = selector.select();
				if (n == 0) continue;
				
				Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
				while (keyIter.hasNext()) {
					SelectionKey key = keyIter.next();
					keyIter.remove();
					
					if (!key.isValid()) continue;
					
					if (key.isAcceptable()) { // 接受客户端连接
						server = (ServerSocketChannel) key.channel();
						SocketChannel sc = server.accept();
						sc.socket().setTcpNoDelay(true);
						sc.socket().setSendBufferSize(TcpMessageConst.PACKET_MAX_LENGTH);
						sc.configureBlocking(false);
						sc.socket().setTcpNoDelay(true);
						sc.socket().setSoLinger(true, 2000);
						sc.register(selector, SelectionKey.OP_READ);
						String clientIp = sc.socket().getInetAddress().getHostAddress();
						LogManager.e(TAG, "server accept client, client ip: " + clientIp);
						if (packetNoMap.containsKey(clientIp)) {
							LogManager.e(TAG, "server accept client, packetNoMap containsKey:" + clientIp);
							Long packetNo = packetNoMap.get(clientIp);
							handlerMap.put(packetNo, new TcpTransferConnection(packetNo, 
									selector, sc, sendedFileTotalCount, listener));							
							LogManager.e(TAG, "server >>> connect client: " + sc);
						} else {
							sc.close();
						}
					} 
					
					if (key.isReadable()) { // 接收
						SocketChannel sc = (SocketChannel) key.channel();
						String clientIp = sc.socket().getInetAddress().getHostAddress();
						Long packetNo = packetNoMap.get(clientIp);
						if (packetNo != null) {
							TcpTransferConnection handler = handlerMap.get(packetNo);
							LogManager.e(TAG, "server accept message, clientIp:" + clientIp + "," + handler);
							if (handler != null) {
								handler.handleReceive();								
							}
						}
					} else if (key.isWritable()) { // 发送
						SocketChannel sc = (SocketChannel) key.channel();
						String clientIp = sc.socket().getInetAddress().getHostAddress();
						Long packetNo = packetNoMap.get(clientIp);
						if (packetNo != null) {
							TcpTransferConnection handler = handlerMap.get(packetNo);
							LogManager.e(TAG, "server send message, clientIp:" + clientIp + "," + handler);
							if (handler != null) {
								handlerMap.get(packetNo).handleSend();	
							}
						}
					}
				}
			}
		}  catch (IOException e) {
			LogManager.e(TAG, "server exception >>> " + e);
		} finally {
			stopServer(true);
		}
	}
	
	public void cancelSendFile(long packetNo) {
		TcpTransferConnection handler = handlerMap.remove(packetNo);
		if (handler != null) {
			handler.cancelSendFile();
		}
	}
	
	public void startServer(ExecutorService threadPool) {
		threadPool.execute(this);
	}
	
	public synchronized void stopServer(boolean auto) {
		// 自动调用此方法，认为是发送失败，主动调用认为是发送取消
		if (auto && listener != null && (selector.isOpen() || server.isOpen())) {
			listener.onServerClosed(packetNoMap);
		}
		
		// 首先取消正在发送的
		if (handlerMap.size() > 0) {
			for (TcpTransferConnection handler : handlerMap.values()) {
				handler.cancelSendFile();
			}			
		}
		// 然后取消等待发送的
		if (packetNoMap.size() > 0) {
			for (Entry<String, Long> entry : packetNoMap.entrySet()) {
				UdpThreadManager.getInstance().cancelSendingWhenNotStart(
						entry.getKey(), entry.getValue());
			}
		}
		
		// 最后关闭Server
		if (selector != null) {
			try {
				selector.wakeup();
				selector.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (server != null) {
			try {
				server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		LogManager.d(TAG, "stop server!");
	}

	public void setTcpFileTransferListener(TcpFileTransferListener listener) {
		this.listener = listener;
	}

}
