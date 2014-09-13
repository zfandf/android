package cn.m15.app.android.gotransfer.net.ipmsg2;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.m15.app.android.gotransfer.enity.TransferFile;
import cn.m15.app.android.gotransfer.net.ipmsg2.MessageConst.Args;
import cn.m15.app.android.gotransfer.net.ipmsg2.MessageConst.FileStatus;

/**
 * TCP消息协议体 
 */
public class TcpMessage {
	
	private int version;    // 协议版本号
	public long packetNo; // 数据包ID
 	public int commandNo;   // 命令
	public String filePath; 
	public String dirPath; 
	public long fileSize;   
	
	/**
	 * 值范围为{@link FileStatus}中定义的常量
	 */
	public int fileStatus; // 文件状态
	
	public int compress;   // 压缩支持
	public long startPos;  // 断点续传位置
	public long lastModify;  
	public ArrayList<TransferFile> fileList;
	
	public TcpMessage() {
		version = MessageConst.VERSION;
		fileStatus = FileStatus.NORMAL;
	}
	
	public TcpMessage(String messageStr) {
		if (messageStr != null && messageStr.length() > 0) {
			try {
				JSONObject json = new JSONObject(messageStr);
				version = json.optInt(Args.VERSION);
				commandNo = json.optInt(Args.COMMAND_NO);
				filePath = json.optString(Args.FILE_PATH, null);
				fileSize = json.optLong(Args.FILE_SIZE);
				fileStatus = json.optInt(Args.FILE_STATUS);
				compress = json.optInt(Args.COMPRESS);
				startPos = json.optLong(Args.START_POS);
				lastModify = json.optLong(Args.LAST_MODIFY);
				dirPath = json.optString(Args.DIR_PATH);
				
				JSONArray fileJsonArray = json.optJSONArray(Args.FILE_LIST);
				if (fileJsonArray != null) {
					fileList = new ArrayList<TransferFile>();
					int size = fileJsonArray.length();
					for (int i = 0; i < size; i++) {
						JSONObject item = fileJsonArray.optJSONObject(i);
						if (item != null) {
							TransferFile file = new TransferFile();
							file.name = item.optString(Args.FILE_NAME);	
							file.path = item.optString(Args.FILE_PATH);
							file.fileType = item.optInt(Args.FILE_TYPE);
							file.size = item.optLong(Args.FILE_SIZE);
							file.lastModify = item.optLong(Args.LAST_MODIFY);
							fileList.add(file);
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	public String toMessageString() {
		String messageStr = "";
		JSONObject json = new JSONObject();
		try {
			json.put(Args.VERSION, version);
			json.put(Args.COMMAND_NO, commandNo);
			json.put(Args.FILE_PATH, filePath);
			json.put(Args.FILE_SIZE, fileSize);
			json.put(Args.FILE_STATUS, fileStatus);
			json.put(Args.COMPRESS, compress);
			json.put(Args.START_POS, startPos);
			json.put(Args.LAST_MODIFY, lastModify);
			json.put(Args.DIR_PATH, dirPath);
			
			JSONArray fileArray = new JSONArray();
			for (TransferFile file : fileList) {
				JSONObject item = new JSONObject();
				item.put(Args.FILE_NAME, file.name);
				item.put(Args.FILE_PATH, file.path);
				item.put(Args.FILE_TYPE, file.fileType);
				item.put(Args.FILE_SIZE, file.size);
				item.put(Args.LAST_MODIFY, file.lastModify);
				fileArray.put(item);
			}
			json.put(Args.FILE_LIST, fileArray);
			
			messageStr = json.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return messageStr;
	}
	
//	public void reset() {
//		version = ProtocolConst.VERSION;
//		packetNo = null;
//	 	commandNo = 0;
//		filePath = null;  
//		fileSize = 0;   
//		fileStatus = FileStatus.NORMAL;
//		compress = 0;  
//		startPos = 0;  
//		lastModify = 0;  
//		fileList = null;
//	}
	
	public int getVersion() {
		return version;
	}
}
