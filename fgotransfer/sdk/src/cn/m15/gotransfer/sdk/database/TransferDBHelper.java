package cn.m15.gotransfer.sdk.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import cn.m15.gotransfer.sdk.utils.LogManager;
import cn.m15.gotransfer.sdk.database.Transfer.Conversation;
import cn.m15.gotransfer.sdk.entity.TransferFile;
import cn.m15.gotransfer.sdk.entity.TransferMsg;
import cn.m15.gotransfer.sdk.net.ipmsg.TUser;
import cn.m15.gotransfer.sdk.net.ipmsg.MessageConst.Status;

public class TransferDBHelper {
	
	public static final String[] USER_PEOJECTION = { 
		Transfer.User._ID, 
		Transfer.User.NAME,
		Transfer.User.AVATAR,
		Transfer.User.MAC_ADDRESS 
	};
	
	public static void clearConversationData(Context context) {
		context.getContentResolver().delete(Conversation.CONTENT_URI, null, null);
	}

	public static void updateConversations(Context context, List<TransferMsg> msgList, TransferDatabaseListener listener) {
		LogManager.d("TransferDBHelper", "updateConversations start>>> " + Arrays.toString(msgList.toArray()));
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		for (TransferMsg msg : msgList) {
			LogManager.d("TransferDBHelper", "updateConversations foreache >>> " + msg);
			if (msg == null) continue;
			ContentProviderOperation operation = null;
			if (msg.wholeStatus == Status.RECEIVING
					|| msg.wholeStatus == Status.SENDING) {
				operation = buildUpdateTransferProgress(msg);
				operations.add(operation);
			} else if (msg.wholeStatus >= Status.RECEIVE_FINISH) {
				ContentProviderOperation[] operationArray = buildUpdateWholeStatus(msg.packetNo, msg.wholeStatus);
				operations.add(operationArray[0]);
				operations.add(operationArray[1]);
			} else if (msg.wholeStatus == 0
					&& msg.status == Status.WAIT_RECEIVE) {
				operation = buildUpdateLocalPath(msg);
				operations.add(operation);
			} else if (msg.wholeStatus == Status.WAIT_SEND) { // 发送方存储发送传输信息
				long created = System.currentTimeMillis();
				for (TransferFile file : msg.files) {
					operation = buildInsertFileSendInfo(msg, file, created);
					operations.add(operation);
				}
			} else if (msg.wholeStatus == Status.WAIT_RECEIVE) { // 接收方存储接收传输信息
				long created = System.currentTimeMillis();
				for (TransferFile file : msg.files) {
					operation = buildInsertFileReceiveInfo(msg, file, created);
					operations.add(operation);
				}
			}
		}
		LogManager.d("TransferDBHelper", "operations size:"+operations.size());
		if (operations.size() > 0) {
			try {
				ContentProviderResult[] result = context.getContentResolver().applyBatch(Transfer.AUTHORITY, operations);
				LogManager.d("TransferDBHelper", Arrays.toString(result));
				if (listener != null) {
					listener.onTransferDataChanged();
				}
				
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (OperationApplicationException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static ContentProviderOperation buildInsertFileReceiveInfo(TransferMsg msg, TransferFile file, long created) {
		 return ContentProviderOperation
				 .newInsert(Conversation.CONTENT_URI)
				 .withValue(Conversation.PACKET_ID, msg.packetNo)
				 .withValue(Conversation.FILENAME, file.name)
				 .withValue(Conversation.SRCPATH, file.path)
				 .withValue(Conversation.IS_SEND, 0)
				 .withValue(Conversation.FRIEND, msg.senderName)
				 .withValue(Conversation.FILETYPE, file.fileType)
				 .withValue(Conversation.STATUS, msg.status)
				 .withValue(Conversation.WHOLE_STATUS, msg.status)
				 .withValue(Conversation.FILESIZE, file.size)
				 .withValue(Conversation.CREATED, created)
				 .withValue(Conversation.LAST_MODIFIED, file.lastModify)
				 .withValue(Conversation.MAC_ADDRESS, msg.macAddress)
				 .build();	
	}
	
	private static ContentProviderOperation buildInsertFileSendInfo(TransferMsg msg, TransferFile file, long created) {
		 return ContentProviderOperation
				 .newInsert(Conversation.CONTENT_URI)
				 .withValue(Conversation.PACKET_ID, msg.packetNo)
				 .withValue(Conversation.FILENAME, file.name)
				 .withValue(Conversation.SRCPATH, file.path)
				 .withValue(Conversation.LOCALPATH, file.path) // 发送方的localpath就是srcpath
				 .withValue(Conversation.IS_SEND, 1)
				 .withValue(Conversation.FRIEND, msg.receiverName)
				 .withValue(Conversation.FILETYPE, file.fileType)
				 .withValue(Conversation.STATUS, msg.status)
				 .withValue(Conversation.WHOLE_STATUS, msg.status)
				 .withValue(Conversation.FILESIZE, file.size)
				 .withValue(Conversation.CREATED, created)
				 .withValue(Conversation.LAST_MODIFIED, file.lastModify)
				 .withValue(Conversation.MAC_ADDRESS, msg.macAddress)
				 .build();	
	}

	private static ContentProviderOperation buildUpdateTransferProgress(TransferMsg msg) {
		return ContentProviderOperation
				.newUpdate(Conversation.CONTENT_URI)
				.withSelection(Conversation.PACKET_ID + " = ? AND " 
							+ Conversation.SRCPATH + " = ? AND " 
							+ Conversation.STATUS + "<" + Status.RECEIVE_FINISH, 
						new String[]{ String.valueOf(msg.packetNo), msg.srcPath })
				.withValue(Conversation.STATUS, msg.status)
				.withValue(Conversation.POSITION, msg.transferSize)
				.build();
	}

	private static ContentProviderOperation[] buildUpdateWholeStatus(long packetNo, int wholeStatus) {
		ContentProviderOperation[] result = new ContentProviderOperation[2];
		result[0] = ContentProviderOperation
				.newUpdate(Conversation.CONTENT_URI)
				.withSelection(Conversation.PACKET_ID + " = ? AND " 
							+ Conversation.WHOLE_STATUS + "<" + Status.RECEIVE_FINISH, 
						new String[]{ String.valueOf(packetNo) })
				.withValue(Conversation.WHOLE_STATUS, wholeStatus)
				.build();
		result[1] = ContentProviderOperation
				.newUpdate(Conversation.CONTENT_URI)
				.withSelection(Conversation.PACKET_ID + " = ? AND " 
							+ Conversation.STATUS + "<" + Status.RECEIVE_FINISH, 
						new String[]{ String.valueOf(packetNo) })
				.withValue(Conversation.STATUS, wholeStatus)
				.build(); 
		return result;		
	}
	
	public static void updateWholeTransferStatus(Context context, long packetNo, 
			int wholeStatus, TransferDatabaseListener listener) {
		ContentProviderOperation[] operationArray = buildUpdateWholeStatus(packetNo, wholeStatus);
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		operations.add(operationArray[0]);
		operations.add(operationArray[1]);
		try {
			context.getContentResolver().applyBatch(Transfer.AUTHORITY, operations);
			if (listener != null) {
				listener.onTransferDataChanged();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			e.printStackTrace();
		}
	}
	
	public static void changeWaitReceiveToReceiving(Context context, long packetNo, 
			int wholeStatus, TransferDatabaseListener listener) {
		ContentValues values = new ContentValues();
		values.put(Conversation.WHOLE_STATUS, wholeStatus);
		context.getContentResolver().update(Conversation.CONTENT_URI, values, 
				Conversation.PACKET_ID + "=?", new String[] { String.valueOf(packetNo) });
		if (listener != null) {
			listener.onTransferDataChanged();
		}
	}
	
	private static ContentProviderOperation buildUpdateLocalPath(TransferMsg msg) {
		ContentProviderOperation.Builder builder = ContentProviderOperation
				.newUpdate(Conversation.CONTENT_URI)
				.withSelection(Conversation.PACKET_ID + " = ? AND " 
							+ Conversation.SRCPATH + "= ?", 
						new String[]{ String.valueOf(msg.packetNo), msg.srcPath })
				.withValue(Conversation.LOCALPATH, msg.localPath);
				if (msg.totalSize != 0) {
					builder.withValue(Conversation.FILESIZE, msg.totalSize);
				}
		return builder.build();
	}
	
	public static void deleteTransferRecord(Context context, long packetNo, String localPath, TransferDatabaseListener listener) {
		context.getContentResolver().delete(Conversation.CONTENT_URI, 
				Conversation.PACKET_ID + "=? AND " + Conversation.LOCALPATH + "=?", 
				new String[] { String.valueOf(packetNo), localPath });
		if (listener != null) {
			listener.onTransferDataChanged();
		}
	}
	
	public static void insertConnectedUserInfo(Context ctx, TUser user) {
		if (isUserExist(ctx, user)) {
			ctx.getContentResolver()
				.update(Transfer.User.CONTENT_URI, buildContentValuesFromUser(user), 
						Transfer.User.MAC_ADDRESS + " = ?", new String[]{ user.getMac() });
		} else {
			ctx.getContentResolver()
				.insert(Transfer.User.CONTENT_URI, buildContentValuesFromUser(user));
		}
	}

	private static boolean isUserExist(Context ctx, TUser user) {
		Cursor c = ctx.getContentResolver().query(Transfer.User.CONTENT_URI, USER_PEOJECTION,
				Transfer.User.MAC_ADDRESS + " = ?", new String[] { user.getMac() },
				Transfer.User.DEFAULT_SORT_ORDER);
		if (c != null) {
			int count = c.getCount();
			c.close();
			return count > 0;
		}
		return false;
	}

	private static ContentValues buildContentValuesFromUser(TUser user) {
		ContentValues cv = new ContentValues();
		cv.put(Transfer.User.AVATAR, "");
		cv.put(Transfer.User.MAC_ADDRESS, user.getMac());
		cv.put(Transfer.User.NAME, user.getUserName());
		return cv;
	}
	
	// 在应用启动时刷新conversation表，将正在传输的状态刷为传输结束的状态
	public static void refreshConversation(Context context) {
		ContentProviderOperation operation1 = ContentProviderOperation
			.newUpdate(Conversation.CONTENT_URI)
			.withSelection(Conversation.STATUS + "<" + Status.RECEIVE_FINISH, null)
			.withValue(Conversation.STATUS, Status.RECEIVE_FAILED)
			.build();
		ContentProviderOperation operation2 = ContentProviderOperation
			.newUpdate(Conversation.CONTENT_URI)
			.withSelection(Conversation.WHOLE_STATUS + "<" + Status.RECEIVE_FINISH, null)
			.withValue(Conversation.WHOLE_STATUS, Status.RECEIVE_FAILED)
			.build();
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		operations.add(operation1);
		operations.add(operation2);
		try {
			context.getContentResolver().applyBatch(Transfer.AUTHORITY, operations);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			e.printStackTrace();
		}
	}
	
	public static void updateDirSize(Context context, long packetNo, String srcPath, 
			long dirSize, TransferDatabaseListener listener) {
		ContentValues values = new ContentValues();
		values.put(Conversation.FILESIZE, dirSize);
		context.getContentResolver().update(Conversation.CONTENT_URI, values, 
				Conversation.PACKET_ID + "=? AND " + Conversation.SRCPATH + "=?", 
				new String[]{ String.valueOf(packetNo), srcPath });
	}
	
	public static interface TransferDatabaseListener {
		
		public void onTransferDataChanged();
	} 
}
