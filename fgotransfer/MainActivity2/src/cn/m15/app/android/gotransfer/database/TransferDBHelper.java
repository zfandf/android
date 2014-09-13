package cn.m15.app.android.gotransfer.database;

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
import android.util.Log;
import cn.m15.app.android.gotransfer.database.Transfer.Conversation;
import cn.m15.app.android.gotransfer.enity.FileTransferMsg;
import cn.m15.app.android.gotransfer.enity.TransferFile;
import cn.m15.app.android.gotransfer.net.ipmsg2.MessageConst.Status;
import cn.m15.app.android.gotransfer.net.ipmsg2.User;

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

	public static void updateConversations(Context context, List<FileTransferMsg> msgList) {
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		for (FileTransferMsg msg : msgList) {
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
				for (TransferFile file : msg.files) {
					operation = buildInsertFileSendInfo(msg, file);
					operations.add(operation);
				}
			} else if (msg.wholeStatus == Status.WAIT_RECEIVE) { // 接收方存储接收传输信息
				for (TransferFile file : msg.files) {
					operation = buildInsertFileReceiveInfo(msg, file);
					operations.add(operation);
				}
			}
		}
		if (operations.size() > 0) {
			try {
				ContentProviderResult[] result = context.getContentResolver().applyBatch(Transfer.AUTHORITY, operations);
				Log.e("TransferDBHelper", Arrays.toString(result));
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (OperationApplicationException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static ContentProviderOperation buildInsertFileReceiveInfo(FileTransferMsg msg, TransferFile file) {
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
				 .withValue(Conversation.CREATED, System.currentTimeMillis())
				 .withValue(Conversation.LAST_MODIFIED, file.lastModify)
				 .withValue(Conversation.MAC_ADDRESS, msg.macAddress)
				 .build();	
	}
	
	private static ContentProviderOperation buildInsertFileSendInfo(FileTransferMsg msg, TransferFile file) {
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
				 .withValue(Conversation.CREATED, System.currentTimeMillis())
				 .withValue(Conversation.LAST_MODIFIED, file.lastModify)
				 .withValue(Conversation.MAC_ADDRESS, msg.macAddress)
				 .build();	
	}

	private static ContentProviderOperation buildUpdateTransferProgress(FileTransferMsg msg) {
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
	
	public static void updateWholeTransferStatus(Context context, long packetNo, int wholeStatus) {
		ContentProviderOperation[] operationArray = buildUpdateWholeStatus(packetNo, wholeStatus);
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		operations.add(operationArray[0]);
		operations.add(operationArray[1]);
		try {
			context.getContentResolver().applyBatch(Transfer.AUTHORITY, operations);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			e.printStackTrace();
		}
	}
	
	public static void changeWaitReceiveToReceiving(Context context, long packetNo, int wholeStatus) {
		ContentValues values = new ContentValues();
		values.put(Conversation.WHOLE_STATUS, wholeStatus);
		context.getContentResolver().update(Conversation.CONTENT_URI, values, 
				Conversation.PACKET_ID + "=?", new String[] { String.valueOf(packetNo) });
	}
	
	private static ContentProviderOperation buildUpdateLocalPath(FileTransferMsg msg) {
		return ContentProviderOperation
				.newUpdate(Conversation.CONTENT_URI)
				.withSelection(Conversation.PACKET_ID + " = ? AND " 
							+ Conversation.SRCPATH + "= ?", 
						new String[]{ String.valueOf(msg.packetNo), msg.srcPath })
				.withValue(Conversation.LOCALPATH, msg.localPath)
				.build();
	}
	
	public static void insertConnectedUserInfo(Context ctx, User user) {
		if (isUserExist(ctx, user)) {
			ctx.getContentResolver()
				.update(Transfer.User.CONTENT_URI, buildContentValuesFromUser(user), 
						Transfer.User.MAC_ADDRESS + " = ?", new String[]{ user.getMac() });
		} else {
			ctx.getContentResolver()
				.insert(Transfer.User.CONTENT_URI, buildContentValuesFromUser(user));
		}
	}

	private static boolean isUserExist(Context ctx, User user) {
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

	private static ContentValues buildContentValuesFromUser(User user) {
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
}
