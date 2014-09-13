package cn.m15.app.android.gotransfer.net.ipmsg2;

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

import cn.m15.app.android.gotransfer.net.ipmsg.NetUdpThreadHelper;

import android.util.Log;

/**
 * 文件传输服务端，用于发送文件
 */
public class TcpFileTransferServer implements Runnable {
	public final static String TAG = "TcpFileTransferServerThread";
	
	private ServerSocketChannel server;
	private Selector selector;
	private Map<Long, TcpFileSendHandler> handlerMap; // key=packetNo
	public Map<String, Long> packetNoMap; // key=IP, value=packetNo
	private int sendedFileTotalCount; // 发送文件的总数目	
	private TcpFileTransferListener listener;
	
	public TcpFileTransferServer(Map<String, Long> packetNoMap, int sendedFileTotalCount) {
		this.packetNoMap = packetNoMap;
		this.handlerMap = new HashMap<Long, TcpFileSendHandler>();
		this.sendedFileTotalCount = sendedFileTotalCount;
	}
	
	@Override
	public void run() {
		try {
			selector = Selector.open();
			server = ServerSocketChannel.open();
			server.configureBlocking(false);
			server.socket().setReuseAddress(true);
			server.socket().bind(new InetSocketAddress(MessageConst.PORT));
			server.socket().setSoTimeout(Integer.MAX_VALUE);
			server.register(selector, SelectionKey.OP_ACCEPT);
			Log.e(TAG, "server >>> open server:" + server.socket().getLocalSocketAddress());
			
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
						String clientIp = sc.socket().getInetAddress().getHostAddress();
						Log.e(TAG, "server accept client, client ip: " + clientIp);
						if (packetNoMap.containsKey(clientIp)) {
							sc.configureBlocking(false);
							sc.register(selector, SelectionKey.OP_READ);
							Long packetNo = packetNoMap.get(clientIp);
							handlerMap.put(packetNo, new TcpFileSendHandler(packetNo, selector, sc, sendedFileTotalCount, listener));							
							Log.e(TAG, "server >>> connect client: " + sc);
						} else {
							sc.close();
						}
					} 
					
					if (key.isReadable()) { // 接收
						SocketChannel sc = (SocketChannel) key.channel();
						String clientIp = sc.socket().getInetAddress().getHostAddress();
						Long packetNo = packetNoMap.get(clientIp);
						if (packetNo != null) {
							handlerMap.get(packetNo).handleReceive();
						}
					} else if (key.isWritable()) { // 发送
						SocketChannel sc = (SocketChannel) key.channel();
						String clientIp = sc.socket().getInetAddress().getHostAddress();
						Long packetNo = packetNoMap.get(clientIp);
						if (packetNo != null) {
							handlerMap.get(packetNo).handleSend();							
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "server exception >>> " + e);
		} finally {
			stopServer();
		}
	}
	
	/**
	 * 取消指定客户端的文件发送
	 */
	public void cancelSendFile(long packetNo) {
		TcpFileSendHandler handler = handlerMap.remove(packetNo);
		if (handler != null) {
			handler.cancelSendFile();
		}
		// 不能停掉server，否则在客户端没有连接到服务端之前取消传输，map就是空的，页面上取消一个就意味着都取消了
//		if (handlerMap.size() == 0) {
//			stopServer();
//		}
	}
	
	public void startServer(ExecutorService threadPool) {
		threadPool.execute(this);
	}
	
	public synchronized void stopServer() {
		if (listener != null && (selector.isOpen() || server.isOpen())) {
			listener.onServerClosed(packetNoMap);
		}
		
		if (handlerMap.size() > 0) {
			for (TcpFileSendHandler handler : handlerMap.values()) {
				handler.cancelSendFile();
			}			
		}
		
		if (packetNoMap.size() > 0) {
			for (Entry<String, Long> entry : packetNoMap.entrySet()) {
				NetUdpThreadHelper.newInstance().cancelSendingWhenNotStart(
						entry.getKey(), entry.getValue());
			}
		}
		
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
		Log.e(TAG, "stop server!");
	}

	public void setTcpFileTransferListener(TcpFileTransferListener listener) {
		this.listener = listener;
	}

}
