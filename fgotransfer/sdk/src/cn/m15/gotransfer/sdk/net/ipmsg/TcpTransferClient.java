package cn.m15.gotransfer.sdk.net.ipmsg;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;

import android.util.Log;
import cn.m15.gotransfer.sdk.SdkConst;
import cn.m15.gotransfer.sdk.entity.TransferFile;
import cn.m15.gotransfer.sdk.net.ipmsg.MessageConst.Command;
import cn.m15.gotransfer.sdk.net.ipmsg.MessageConst.FileStatus;
import cn.m15.gotransfer.sdk.net.ipmsg.MessageConst.FileType;
import cn.m15.gotransfer.sdk.net.ipmsg.MessageConst.TcpMessageConst;
import cn.m15.gotransfer.sdk.utils.LogManager;
import cn.m15.gotransfer.sdk.utils.SdkFileUtil;

/**
 * 文件传输客户端，用于接收文件 
 */
public class TcpTransferClient extends BaseTransferConnection implements Runnable {
	public final static String TAG = "TcpFileTransferClient";
	
	public long packetNo;
	public InetAddress serverAddress;
	private Selector selector;
	private SocketChannel client;
	private TcpFileTransferListener listener;
	
	private volatile boolean isStarted = false;
	
	// 以下接收文件时使用
	private boolean startReceived = false;
	private volatile boolean receivable = true; // 是否可接收文件
	private ArrayList<TransferFile> receivedFileList; // 接收文件列表
	private ArrayList<TransferFile> dirFileList; // 文件夹下的文件列表
	private int receivedIndex; // 正在接收的文件receivedFileList中位置
//	private int receivedIndexOfDir = -1; // 正在接收的文件在dirFileList中位置
	private long receivedSizeOfDir; // 已发送文件夹的字节数
	private long totalSizeOfDir; // 文件夹大小
	private long receivedSize; // 已接收的字节数
	private long totalSize; // 正在接收的文件的总字节数
	private String receivingSrcDirPath; // 正在接收的目录的原路径
	private String receivingDirPath; // 正在接收的目的本地路径
	private String receivingSrcFilePath; // 正在接收的文件的本地路径
	private String receivingFilePath; // 正在接收的文件的本地路径
	private FileOutputStream fileOutSteam;
	private FileChannel fileChannel;
	
	public TcpTransferClient(long packetNo, String serverAddress, ArrayList<TransferFile> fileList) {
		this.packetNo = packetNo;
		this.receivedFileList = fileList;
		try {
			this.serverAddress = InetAddress.getByName(serverAddress);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			LogManager.e(TAG, "TcpFileTransferClient init: " + e.getMessage());
		}
	}
	
	@Override
	public void run() {
		try {
			selector = Selector.open();
			client = SocketChannel.open();
			client.configureBlocking(false);
			client.socket().setReuseAddress(true);
			client.socket().setSendBufferSize(TcpMessageConst.PACKET_MAX_LENGTH);
			client.socket().setReceiveBufferSize(TcpMessageConst.PACKET_MAX_LENGTH);
			client.socket().setTcpNoDelay(true);
			client.socket().setSoLinger(true, 2000);
			if (!client.connect(new InetSocketAddress(serverAddress, MessageConst.PORT))) {
				while (!client.finishConnect()) {
				}
			}
			client.register(selector, SelectionKey.OP_WRITE);
			
			while (client.isOpen() && selector.isOpen()) {
				int n = selector.select();
				if (n == 0) continue;
				
				Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
				while (keyIter.hasNext()) {
					SelectionKey key = keyIter.next();
					keyIter.remove();
					
					if (!key.isValid()) continue;
					
					if (key.isReadable()) {
						client = (SocketChannel) key.channel();
						if (receivable) {
							receiveMessage(client);
							if (totalSize == 0 && totalSizeOfDir == 0 
									&& receivedIndex == receivedFileList.size()) { // 全部接收完成
								stopClient(false);
								LogManager.d(TAG, "onFileReceiveFinish?: " + startReceived);
								if (listener != null && startReceived) {
									LogManager.d(TAG, "onFileReceiveFinish: " + packetNo);
									listener.onFileReceiveFinish(packetNo);								
								} else if (listener != null && !startReceived) {
									
								}
								return;
							} else if ((receivedSize != totalSize && totalSize > 0) || 
									(receivedSizeOfDir != totalSizeOfDir && totalSizeOfDir > 0)) { // 接收未完成
								// 接收未完成时，不切换读写模式
								if (client.socket().getInputStream().available() == 0) {}
								continue;
							}
						}
						client.register(selector, SelectionKey.OP_WRITE);
					} if (key.isWritable()) {
						client = (SocketChannel) key.channel();
						if (receivable) {
							sendRequestSendFileOrDirMessage();
							client.register(selector, SelectionKey.OP_READ); // 抛出ClosedChannelException							
						}
						if (!receivable) { // 如果receivable为false，那么就发送"取消传输"命令
							String messageStr = TcpMessageFractory.createCancelTransferMsg(null);
							sendMessage(client, TcpMessageConst.DATA_TYPE_COMMAND, messageStr.getBytes(SdkConst.USEDCHARACTORSET));
							LogManager.d(TAG, "client >>> send cancel message: " + messageStr);
							key.cancel();
							stopClient(false);
							deleteReceivingFile();
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			stopClient(true);
			deleteReceivingFile();
			LogManager.e(TAG, "run: " + e.getMessage());
		}
	}
	
	private void deleteReceivingFile() {
		if ((receivedSize != totalSize && totalSize > 0)) {
			File file = new File(receivingFilePath);
			if (file.exists()) {
				file.delete();
			}
		}
	}
	
	private void sendRequestSendFileOrDirMessage() {
		if (receivedIndex < receivedFileList.size()) { // 请求发送文件(夹)
			TransferFile receivedFile = receivedFileList.get(receivedIndex);
			String messageStr = null;
			if (receivedFile.fileType == FileType.DIR) {
				receivingSrcDirPath = receivedFile.path;
				messageStr = TcpMessageFractory.createRequestSendDirMsg(receivingSrcDirPath, null);								
			} else {
				messageStr = TcpMessageFractory.createRequestSendFileMsg(receivedFile.path);
			}
			try {
				sendMessage(client, TcpMessageConst.DATA_TYPE_COMMAND, messageStr.getBytes(SdkConst.USEDCHARACTORSET));
				LogManager.d(TAG, "client >>> send message: " + messageStr);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				stopClient(true);
				LogManager.e(TAG, "sendRequestSendFileOrDirMessage: " + e.getMessage());
			}
			receivedIndex++;							
		}
	}
	
	@Override
	public void onReceivedCommand(SocketChannel sc, byte[] data) throws UnsupportedEncodingException {
		String messageStr = new String(data, SdkConst.USEDCHARACTORSET);
		LogManager.d(TAG, "client >>> receive message: " + messageStr);
		TcpMessage message = TcpMessageFractory.parseMessage(messageStr);
		if (message.getVersion() == MessageConst.VERSION) {
			if (message.commandNo ==  Command.RESPONSE_SEND_FILE) {  // 接收到"回应发送文件"消息
				startReceived = true;
				// 判断文件状态
				if (message.fileStatus == FileStatus.NORMAL) { 
					// 指定正在接收的文件的原路径
					receivingSrcFilePath = message.filePath;
					// 判断正在接收的文件是否属于文件夹中的文件
					if (message.dirPath != null && message.dirPath.length() > 0) {
						receivingFilePath = SdkFileUtil.getStoreReceviedFilePath(receivingSrcDirPath, message.filePath);
						readyForReceiveFileOrDir(message.fileSize, true);
					} else {
						String fileName = message.filePath.substring(message.filePath.lastIndexOf(File.separator) + 1, message.filePath.length());
						receivingFilePath = SdkFileUtil.getStoreReceviedFilePath(fileName);
						readyForReceiveFileOrDir(message.fileSize, false);
					}
				} else if (message.fileStatus == FileStatus.NOT_EXSIT && listener != null) {
					// 不监听文件夹内文件不存在的情况
					if (message.dirPath == null || message.dirPath.length() == 0) {
						LogManager.d(TAG, "onReceivedFileNotExsit: " + packetNo + ", " + message.filePath);
						listener.onReceivedFileNotExsit(packetNo, message.filePath);						
					} else {
//						receivedIndexOfDir++;
					}
				}
			} else if (message.commandNo ==  Command.RESPONSE_SEND_DIR) {  // 接收到"回应发送文件夹"消息
				LogManager.d(TAG, "response_send_dir: " + packetNo + "," + message.fileList);
				startReceived = true;
				// 判断文件夹状态
				if (message.fileList != null && message.fileList.size() > 0) {
					dirFileList = message.fileList;
					// 计算文件夹大小
					for (TransferFile file : dirFileList) {
						totalSizeOfDir += file.size;
					}
					TransferFile file = dirFileList.get(0);
					receivingSrcFilePath = file.path;
					receivingFilePath = SdkFileUtil.getStoreReceviedFilePath(receivingSrcDirPath, file.path);
					
					String dirName = SdkFileUtil.getDirName(receivingSrcDirPath);
					receivingDirPath = SdkFileUtil.getSubFolderPath(FileType.DIR) + dirName;
					
					readyForReceiveFileOrDir(file.size, true);
				} else {
					if (listener != null) {
						if (message.fileStatus == FileStatus.NOT_EXSIT) {
							listener.onReceivedFileNotExsit(packetNo, message.filePath);						
							LogManager.d(TAG, "onReceivedFileNotExsit: " + packetNo + "," + message.filePath);
						} else {
							String dirName = SdkFileUtil.getDirName(receivingSrcDirPath);
							receivingDirPath = SdkFileUtil.getSubFolderPath(FileType.DIR) + dirName;
							File dirFile = new File(receivingDirPath);
							if (!dirFile.exists()) {
								dirFile.mkdirs();
							}
							listener.onBeforeReceiveFile(packetNo, message.filePath, receivingDirPath, 0);
							listener.onReceiveFileProgress(packetNo, message.filePath, receivingFilePath, 100, 100);
							LogManager.d(TAG, "received dir is empty: " + packetNo + "," + message.filePath + ", " + receivingSrcDirPath);
							receivingDirPath = "";
						}						
					}
				}
			} else if (message.commandNo == Command.CANCEL_TRANSFER) {
				stopClient(false);
				if (listener != null) {
					listener.onCancelledByPeer(packetNo);
					LogManager.d(TAG, "onCancelledByPeer: " + packetNo);
				}
			}
		}
	}
	
	private void readyForReceiveFileOrDir(long fileSize, boolean isInDir) {
		LogManager.d(TAG, "receivingFilePath >>> " + receivingSrcFilePath + ", " + receivingFilePath);
		File receivedFile  = new File(receivingFilePath);
		File parentDir = receivedFile.getParentFile();
		if (!parentDir.exists()) {
			parentDir.mkdirs();
		}
		// TODO: 如果文件已存在，则重新命名
		if (receivedFile.exists()) {
			receivedFile.delete();
		}
		try {
			receivedFile.setWritable(true);
			receivedFile.createNewFile();
			
			if (receivedFile.getFreeSpace() < fileSize) { // 存储空间不足
				receivedFile.delete();
				stopClient(false);
				if (listener != null) {
					listener.onStorageTooSmallAtReceiver(packetNo);
					LogManager.d(TAG, "onStorageTooSmallAtReceiver: " + packetNo);
				}
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
			LogManager.e(TAG, "client >>> create file failed: " + receivingFilePath);
		}
		
		if (listener != null) {
			if (isInDir) {
				listener.onBeforeReceiveFile(packetNo, receivingSrcDirPath, receivingDirPath, totalSizeOfDir);
				LogManager.d(TAG, "onBeforeReceiveFile: " + packetNo + ", " + receivingSrcDirPath
						+ ", " + receivingDirPath);
			} else {
				listener.onBeforeReceiveFile(packetNo, receivingSrcFilePath, receivingFilePath, fileSize);
				LogManager.d(TAG, "onBeforeReceiveFile: " + packetNo + ", " + receivingSrcFilePath
						+ ", " + receivingSrcFilePath);				
			}
		}
		receivedSize = 0;
		totalSize = fileSize;
	}
	
	@Override
	public void onReceivedData(SocketChannel sc, int dataLength) throws IOException {
		if (!receivable) return; 
		if (fileOutSteam == null) {
			fileOutSteam = new FileOutputStream(receivingFilePath);
			fileChannel = fileOutSteam.getChannel();
		}
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		LogManager.d(TAG, "dataLength:" + receivingFilePath + ", " + dataLength);
		
		// 写入到文件
		long transferSize = fileChannel.transferFrom(sc, receivedSize, dataLength);
		if (transferSize != dataLength) {
			receivedSize += transferSize;
			long tryAgainSize = transferSize;
			long timeStamp = System.currentTimeMillis();
			while (tryAgainSize != dataLength) {
				// 如果对方因某种原因挂掉,那么transferFrom会一直返回0而不报任何异常，所以此处加上超时时间
				transferSize = fileChannel.transferFrom(sc, receivedSize, dataLength - tryAgainSize);
				tryAgainSize += transferSize;
				receivedSize += transferSize;
				long currentTimestamp = System.currentTimeMillis();
				Log.e(TAG, "write to file: " + timeStamp + ", " + (currentTimestamp - timeStamp));
				if (currentTimestamp - timeStamp > 10000) {
					throw new IOException("read time out!");
				}
				
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}			
			transferSize = 0;
		}
		receivedSize += transferSize;
		
		// 文件进度
		if (totalSizeOfDir > 0) { 
			receivedSizeOfDir += dataLength;
			if (listener != null) {
				listener.onReceiveFileProgress(packetNo, receivingSrcDirPath, receivingFilePath, receivedSizeOfDir, totalSizeOfDir);
				LogManager.d(TAG, "onReceiveFileProgress: " + packetNo + ", " + receivingSrcDirPath
						+ ", " + receivedSizeOfDir + ", " + totalSizeOfDir);
			}
			LogManager.d(TAG, "receive directory precent >>> " + (100.0*receivedSizeOfDir/totalSizeOfDir));
		} else {
			if (listener != null) {
				listener.onReceiveFileProgress(packetNo, receivingSrcFilePath, receivingFilePath, receivedSize, totalSize);
				LogManager.d(TAG, "onReceiveFileProgress: " + packetNo + ", " + receivingSrcFilePath
						+ ", " + receivedSize + ", " + totalSize);
			}
			LogManager.d(TAG, "receive file precent >>> " + (100.0*receivedSize/totalSize));
		}
		// 文件接收完成时，重置参数，并关闭文件通道
		// 之所以是大于等于，可能原因是在传输过程总文件大小发生变化，那么如果小了的话，那就认为文件发送失败
		if (receivedSize >= totalSize) { 
			receivedSize = 0;
			totalSize = 0;
			if (totalSizeOfDir > 0) {
//				receivedIndexOfDir++;
			}
			if (receivedSizeOfDir >= totalSizeOfDir) {
				receivedSizeOfDir = 0;
				totalSizeOfDir = 0;
//				receivedIndexOfDir = 0;
			}
			LogManager.d(TAG, "client >>> receive finish: " + receivingFilePath);
			closeFileStreamAndChannel();
		}
	}
	
	private void closeFileStreamAndChannel() {
		if (fileOutSteam != null) {
			try {
				fileOutSteam.close();
				fileOutSteam = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (fileChannel != null) {
			try {
				fileChannel.close();
				fileChannel = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean isStarted() {
		return isStarted;
	}
	
	public void startClient(ExecutorService threadPool) {
		isStarted = true;
		threadPool.execute(this);
	}
		
	public synchronized void stopClient(boolean canListener) {
		isStarted = false;
		if (selector == null || client == null) return;
		
		if (listener != null && (selector.isOpen() || client.isOpen()) && canListener) {
			listener.onClientClosed(packetNo);
			LogManager.d(TAG, "onClientClosed: " + packetNo);
		}
		if (selector != null) {
			try {
				selector.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (client != null) {
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		LogManager.d(TAG, "stop client!");
	}
	
	public void cancelReceiveFile() {
		LogManager.d(TAG, "client >>> cancel receive file");
		receivable = false;
		selector.wakeup();
	}
	
	public void setTcpFileTransferListener(TcpFileTransferListener listener) {
		this.listener = listener;
	}
}
