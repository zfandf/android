package cn.m15.app.android.gotransfer.enity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.text.TextUtils;
import cn.m15.app.android.gotransfer.net.ipmsg2.MessageConst.Status;
import cn.m15.app.android.gotransfer.net.ipmsg2.TcpFileTransferClient;
import cn.m15.app.android.gotransfer.net.ipmsg2.TcpFileTransferListener;
import cn.m15.app.android.gotransfer.net.ipmsg2.UdpMessage;
import cn.m15.app.android.gotransfer.net.ipmsg2.User;
import cn.m15.app.android.gotransfer.ui.activity.BaseActivity2;

public class FileTransferMsgManager implements TcpFileTransferListener {
	private static FileTransferMsgManager instance;
	
	public FileTransferMsgPool fileTransferMsgPool;
	
	private ArrayList<FileTransferMsg> fileTransferMsgList;
	private ArrayList<FileTransferMsg> tempMsgList;
	
	private FileTransferMsgManager() {
		fileTransferMsgList = new ArrayList<FileTransferMsg>();
		fileTransferMsgPool = new FileTransferMsgPool(20);
		tempMsgList = new ArrayList<FileTransferMsg>();
	}
	
	public synchronized static FileTransferMsgManager getInstance() {
		if (instance == null) {
			instance = new FileTransferMsgManager();
		}
		return instance;			
	}
	
	public synchronized void addMsg(FileTransferMsg msg) {
		fileTransferMsgList.add(msg);
	}
	
	public synchronized ArrayList<FileTransferMsg> getFileTransferMsgList() {
		tempMsgList.clear();
		tempMsgList.addAll(fileTransferMsgList);
		fileTransferMsgList.clear();
		return tempMsgList;
	}
	
	@Override
	public void onSendedFileNotExist(long packetNo, String filePath) {
		FileTransferMsg msg = fileTransferMsgPool.acquire();
		msg.wholeStatus = Status.SENDING;
		msg.status = Status.SEND_FAILED;
		msg.packetNo = packetNo;
		msg.srcPath = filePath;
		addMsg(msg);
	}

	@Override
	public void onReceivedFileNotExsit(long packetNo, String filePath) {
		FileTransferMsg msg = fileTransferMsgPool.acquire();
		msg.wholeStatus = Status.RECEIVING;
		msg.status = Status.RECEIVE_FAILED;
		msg.packetNo = packetNo;
		msg.srcPath = filePath;
		addMsg(msg);		
	}
	
	@Override
	public void onSendFileProgress(long packetNo, String srcPath, long sendedSize, long totalSize) {
		FileTransferMsg msg = fileTransferMsgPool.acquire();
		msg.wholeStatus = Status.SENDING;
		if (sendedSize == totalSize) {
			msg.status = Status.SEND_FINISH;
		} else {
			msg.status = Status.SENDING;
		}
		msg.packetNo = packetNo;
		msg.srcPath = srcPath;
		msg.transferSize = sendedSize;
		msg.totalSize = totalSize;
		addMsg(msg);
	}

	@Override
	public void onReceiveFileProgress(long packetNo, String srcPath, long receivedSize, long totalSize) {
		FileTransferMsg msg = fileTransferMsgPool.acquire();
		msg.wholeStatus = Status.RECEIVING;
		if (receivedSize == totalSize) {
			msg.status = Status.RECEIVE_FINISH;			
		} else {
			msg.status = Status.RECEIVING;
		}
		msg.packetNo = packetNo;
		msg.srcPath = srcPath;
		msg.transferSize = receivedSize;
		msg.totalSize = totalSize;
		addMsg(msg);
	}
	
	@Override
	public void onClientClosed(long packetNo) {
		FileTransferMsg msg = fileTransferMsgPool.acquire();
		msg.packetNo = packetNo;
		msg.wholeStatus = Status.RECEIVE_FAILED;
		addMsg(msg);
	}
	
	@Override
	public void onCloseConnectionWithClient(long packetNo) {
		FileTransferMsg msg = fileTransferMsgPool.acquire();
		msg.packetNo = packetNo;
		msg.wholeStatus = Status.SEND_FAILED;
		addMsg(msg);
	}

	@Override
	public void onServerClosed(Map<String, Long> packetNoMap) {
		if (packetNoMap != null && packetNoMap.size() > 0) {
			for (long packetNo : packetNoMap.values()) {
				onCloseConnectionWithClient(packetNo);
			}			
		}
	}
	
	@Override
	public void onFileReceiveFinish(long packetNo) {
		FileTransferMsg msg = fileTransferMsgPool.acquire();
		msg.wholeStatus = Status.RECEIVE_FINISH;
		msg.packetNo = packetNo;
		addMsg(msg);
	}

	@Override
	public void onFileSendFinish(long packetNo) {
		FileTransferMsg msg = fileTransferMsgPool.acquire();
		msg.wholeStatus = Status.SEND_FINISH;
		msg.packetNo = packetNo;
		addMsg(msg);
	}
	
	@Override
	public void onCancelledByPeer(long packetNo) {
		FileTransferMsg msg = fileTransferMsgPool.acquire();
		msg.wholeStatus = Status.CANCELLED_BY_PEER;
		msg.packetNo = packetNo;
		addMsg(msg);
	}
	
	@Override
	public void onStorageTooSmallAtReceiver(long packetNo) {
		// this will not run in UI thread.
//		Toast.makeText(GoTransferApplication.getInstance(), R.string.storage_too_small, Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onBeforeReceiveFile(long packetNo, String srcPath, String localPath) {
		FileTransferMsg msg = fileTransferMsgPool.acquire();
		msg.wholeStatus = 0;
		msg.status = Status.WAIT_RECEIVE;
		msg.packetNo = packetNo;
		msg.srcPath = srcPath;
		msg.localPath = localPath;
		addMsg(msg);
	}
	
	@Override
	public void onReceiveSendFileUdp(UdpMessage message, ArrayList<TransferFile> files) {
		String senderIp = null;
		for (User user : BaseActivity2.netThreadHelper.getUsers().values()) {
			if(user.getMac().equals(message.senderMac)) {
				senderIp = user.getIp();
				break;
			}
		}
		if (!TextUtils.isEmpty(senderIp)) {
			TcpFileTransferClient client = new TcpFileTransferClient(message.getPacketNo(), senderIp, files);
			client.setTcpFileTransferListener(FileTransferMsgManager.getInstance());
			BaseActivity2.transferClientMap.put(message.getPacketNo(), client);			
		}
		
		FileTransferMsg msg = fileTransferMsgPool.acquire();
		msg.wholeStatus = Status.WAIT_RECEIVE;
		msg.status = Status.WAIT_RECEIVE;
		msg.packetNo = message.getPacketNo();
		msg.senderName = message.senderName;
		msg.macAddress = message.senderMac;
		msg.files = files;
		addMsg(msg);
	}
	
	@Override
	public void onRefuseReceive(long packetNo) {
		FileTransferMsg msg = fileTransferMsgPool.acquire();
		msg.wholeStatus = Status.REFUSED;
		msg.packetNo = packetNo;
		addMsg(msg);
	}
	
	public static class FileTransferMsgPool {
		
		private final FileTransferMsg[] pool;

        private int poolSize;

        public FileTransferMsgPool(int maxPoolSize) {
        	 if (maxPoolSize <= 0) {
                 throw new IllegalArgumentException("The max pool size must be > 0");
             }
        	 pool = new FileTransferMsg[maxPoolSize];
        }
        
        public synchronized FileTransferMsg acquire() {
        	if (poolSize > 0) {
        		final int lastPooledIndex = poolSize - 1;
	            FileTransferMsg instance = (FileTransferMsg) pool[lastPooledIndex];
	            pool[lastPooledIndex] = null;
	            poolSize--;
	            instance.reset();
	            return instance;
        	}
        	return new FileTransferMsg();
        }
        
        public synchronized void release(FileTransferMsg instance) {
        	if (!isInPool(instance)) {
            	if (poolSize < pool.length) {
            		instance.reset();
                	pool[poolSize] = instance;
                    poolSize++;
            	}
        	}
        }
        
        private boolean isInPool(FileTransferMsg instance) {
            for (int i = 0; i < poolSize; i++) {
                if (pool[i] == instance) {
                    return true;
                }
            }
            return false;
        }
        
        public synchronized void releaseList(List<FileTransferMsg> list) {
        	for (FileTransferMsg msg : list) {
        		release(msg);
        	}
        }
    }

}
