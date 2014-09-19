package cn.m15.gotransfer.sdk.entity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;
import cn.m15.gotransfer.sdk.ConfigManager;
import cn.m15.gotransfer.sdk.net.ipmsg.UdpThreadManager;
import cn.m15.gotransfer.sdk.net.ipmsg.TcpTransferClient;
import cn.m15.gotransfer.sdk.net.ipmsg.TcpFileTransferListener;
import cn.m15.gotransfer.sdk.net.ipmsg.TransferManager;
import cn.m15.gotransfer.sdk.net.ipmsg.UdpMessage;
import cn.m15.gotransfer.sdk.net.ipmsg.TUser;
import cn.m15.gotransfer.sdk.net.ipmsg.MessageConst.Status;

public class TransferMsgManager implements TcpFileTransferListener {
	private static TransferMsgManager instance;
	
	public FileTransferMsgPool fileTransferMsgPool;
	
	private ArrayList<TransferMsg> fileTransferMsgList;
	private ArrayList<TransferMsg> tempMsgList;
	
	private Handler handler;
	
	private TransferMsgManager() {
		fileTransferMsgList = new ArrayList<TransferMsg>();
		fileTransferMsgPool = new FileTransferMsgPool(20);
		tempMsgList = new ArrayList<TransferMsg>();
	}
	
	public synchronized static TransferMsgManager getInstance() {
		if (instance == null) {
			instance = new TransferMsgManager();
		}
		return instance;			
	}
	
	public synchronized void addMsg(TransferMsg msg) {
		fileTransferMsgList.add(msg);
	}
	
	public synchronized ArrayList<TransferMsg> getFileTransferMsgList() {
		tempMsgList.clear();
		tempMsgList.addAll(fileTransferMsgList);
		fileTransferMsgList.clear();
		return tempMsgList;
	}
	
	@Override
	public void onSendedFileNotExist(long packetNo, String filePath) {
		TransferMsg msg = fileTransferMsgPool.acquire();
		msg.wholeStatus = Status.SENDING;
		msg.status = Status.SEND_FAILED;
		msg.packetNo = packetNo;
		msg.srcPath = filePath;
		addMsg(msg);
	}

	@Override
	public void onReceivedFileNotExsit(long packetNo, String filePath) {
		TransferMsg msg = fileTransferMsgPool.acquire();
		msg.wholeStatus = Status.RECEIVING;
		msg.status = Status.RECEIVE_FAILED;
		msg.packetNo = packetNo;
		msg.srcPath = filePath;
		addMsg(msg);		
	}
	
	@Override
	public void onSendFileProgress(long packetNo, String srcPath, long sendedSize, long totalSize) {
		TransferMsg msg = fileTransferMsgPool.acquire();
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
	public void onReceiveFileProgress(long packetNo, String srcPath, String localPath, long receivedSize, long totalSize) {
		TransferMsg msg = fileTransferMsgPool.acquire();
		msg.wholeStatus = Status.RECEIVING;
		if (receivedSize == totalSize) {
			msg.status = Status.RECEIVE_FINISH;		
			scanFile(ConfigManager.getInstance().getApplicationContext(), localPath);
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
		TransferMsg msg = fileTransferMsgPool.acquire();
		msg.packetNo = packetNo;
		msg.wholeStatus = Status.RECEIVE_FAILED;
		addMsg(msg);
	}
	
	@Override
	public void onCloseConnectionWithClient(long packetNo) {
		TransferMsg msg = fileTransferMsgPool.acquire();
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
		TransferMsg msg = fileTransferMsgPool.acquire();
		msg.wholeStatus = Status.RECEIVE_FINISH;
		msg.packetNo = packetNo;
		addMsg(msg);
	}

	@Override
	public void onFileSendFinish(long packetNo) {
		TransferMsg msg = fileTransferMsgPool.acquire();
		msg.wholeStatus = Status.SEND_FINISH;
		msg.packetNo = packetNo;
		addMsg(msg);
	}
	
	@Override
	public void onCancelledByPeer(long packetNo) {
		TransferMsg msg = fileTransferMsgPool.acquire();
		msg.wholeStatus = Status.CANCELLED_BY_PEER;
		msg.packetNo = packetNo;
		addMsg(msg);
	}
	
	@Override
	public void onStorageTooSmallAtReceiver(long packetNo) {
		if (handler == null) {
			handler = new Handler(Looper.getMainLooper());
		}
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				ConfigManager cm = ConfigManager.getInstance();
				Toast.makeText(cm.getApplicationContext(), 
						cm.getStringForNotEnoughStorage(), Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	@Override
	public void onBeforeReceiveFile(long packetNo, String srcPath, String localPath, long fileSize) {
		TransferMsg msg = fileTransferMsgPool.acquire();
		msg.wholeStatus = 0;
		msg.status = Status.WAIT_RECEIVE;
		msg.packetNo = packetNo;
		msg.srcPath = srcPath;
		msg.localPath = localPath;
		msg.totalSize = fileSize;
		addMsg(msg);
	}
	
	@Override
	public void onReceiveSendFileUdp(UdpMessage message, ArrayList<TransferFile> files) {
		String senderIp = null;
		
		Map<String, TUser> users = UdpThreadManager.getInstance().getUsers();
		
		for (TUser user : users.values()) {
			if(user.getMac().equals(message.senderMac)) {
				senderIp = user.getIp();
				break;
			}
		}
		if (!TextUtils.isEmpty(senderIp)) {
			TcpTransferClient client = new TcpTransferClient(message.getPacketNo(), senderIp, files);
			client.setTcpFileTransferListener(TransferMsgManager.getInstance());
			
			TransferManager.getInstance().getTransferClientMap().put(message.getPacketNo(), client);	
		}
		
		TransferMsg msg = fileTransferMsgPool.acquire();
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
		TransferMsg msg = fileTransferMsgPool.acquire();
		msg.wholeStatus = Status.REFUSED;
		msg.packetNo = packetNo;
		addMsg(msg);
	}
	
	private static void scanFile(Context ctx, String path) {
		if (path == null) return;
		Uri uri = Uri.fromFile(new File(path));
		Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
		ctx.sendBroadcast(intent);
	}
	
	public static class FileTransferMsgPool {
		
		private final TransferMsg[] pool;

        private int poolSize;

        public FileTransferMsgPool(int maxPoolSize) {
        	 if (maxPoolSize <= 0) {
                 throw new IllegalArgumentException("The max pool size must be > 0");
             }
        	 pool = new TransferMsg[maxPoolSize];
        }
        
        public synchronized TransferMsg acquire() {
        	if (poolSize > 0) {
        		final int lastPooledIndex = poolSize - 1;
	            TransferMsg instance = (TransferMsg) pool[lastPooledIndex];
	            pool[lastPooledIndex] = null;
	            poolSize--;
	            instance.reset();
	            return instance;
        	}
        	return new TransferMsg();
        }
        
        public synchronized void release(TransferMsg instance) {
        	if (!isInPool(instance)) {
            	if (poolSize < pool.length) {
            		instance.reset();
                	pool[poolSize] = instance;
                    poolSize++;
            	}
        	}
        }
        
        private boolean isInPool(TransferMsg instance) {
            for (int i = 0; i < poolSize; i++) {
                if (pool[i] == instance) {
                    return true;
                }
            }
            return false;
        }
        
        public synchronized void releaseList(List<TransferMsg> list) {
        	for (TransferMsg msg : list) {
        		release(msg);
        	}
        }
    }

}
