package cn.m15.gotransfer.sdk.net.ipmsg;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.m15.gotransfer.sdk.entity.TransferFile;
import cn.m15.gotransfer.sdk.net.ipmsg.MessageConst.Args;
import cn.m15.gotransfer.sdk.net.ipmsg.MessageConst.Command;

/**
 * TCP消息工厂，解析消息以及生成消息文本
 */
public final class TcpMessageFractory {

	private TcpMessageFractory() {
	}
	
	public static TcpMessage parseMessage(String messageStr) {
		return new TcpMessage(messageStr);
	}
	
	/**
	 * 请求发送文件
	 * @param filePath 请求发送的文件路径
	 * @return
	 */
	public static String createRequestSendFileMsg(String filePath) {
		JSONObject json = new JSONObject();
		try {
			json.put(Args.VERSION, MessageConst.VERSION);
			json.put(Args.COMMAND_NO, Command.REQUEST_SEND_FILE);
			json.put(Args.FILE_PATH, filePath);
			// 断点续传时添加以下三个参数, 目前没有压缩、断点续传，信息封装到哪里待定
//			json.put(Args.COMPRESS, compress);
//			json.put(Args.START_POS, startPos);
//			json.put(Args.LAST_MODIFY, lastModify);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}
	
	/**
	 * 回应请求文件发送
	 * @param filePath
	 * @return
	 */
	public static String createResponseSendFileMsg(String filePath, String dirPath, long fileSize, int fileStatus) {
		JSONObject json = new JSONObject();
		try {
			json.put(Args.VERSION, MessageConst.VERSION);
			json.put(Args.COMMAND_NO, Command.RESPONSE_SEND_FILE);
			json.put(Args.FILE_PATH, filePath);
			json.put(Args.FILE_SIZE, fileSize);
			json.put(Args.FILE_STATUS, fileStatus);
			json.put(Args.DIR_PATH, dirPath);
			// 断点续传时添加以下参数, 目前没有压缩、断点续传，信息封装到哪里待定
//			json.put(Args.START_POS, startPos);
//			json.put(Args.LAST_MODIFY, lastModify);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}
	
	/**
	 * 请求发送文件夹
	 * @param filePath 请求发送的文件夹路径
	 * @param fileList 剩余文件列表(断点续传时传入)
	 * @return
	 */
	public static String createRequestSendDirMsg(String filePath, ArrayList<TransferFile> fileList) {
		JSONObject json = new JSONObject();
		try {
			json.put(Args.VERSION, MessageConst.VERSION);
			json.put(Args.COMMAND_NO, Command.REQUEST_SEND_DIR);
			json.put(Args.FILE_PATH, filePath);
			if (fileList != null && fileList.size() > 0) {
				json.put(Args.FILE_LIST, convertToJsonArray(fileList));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}
	
	/**
	 * 回应请求发送文件夹
	 * @param path 文件夹路径
	 * @param status 文件夹状态
	 * @param fileList 剩余文件列表(断点续传时传入)
	 * @return
	 */
	public static String createResponseSendDirMsg(String path, int status, ArrayList<TransferFile> fileList) {
		JSONObject json = new JSONObject();
		try {
			json.put(Args.VERSION, MessageConst.VERSION);
			json.put(Args.COMMAND_NO, Command.RESPONSE_SEND_DIR);
			json.put(Args.FILE_STATUS, status);
			json.put(Args.FILE_PATH, path);
			if (fileList != null && fileList.size() > 0) {
				json.put(Args.FILE_LIST, convertToJsonArray(fileList));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}
	
	/**
	 * 取消文件传输
	 * @param filePath 如果为空则表示取消全部文件；如果非空则表示取消单个文件
	 * @return
	 */
	public static String createCancelTransferMsg(String filePath) {
		JSONObject json = new JSONObject();
		try {
			json.put(Args.VERSION, MessageConst.VERSION);
			json.put(Args.COMMAND_NO, Command.CANCEL_TRANSFER);
			json.put(Args.FILE_PATH, filePath);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}
	
	/**
	 * 暂停文件传输
	 * @param filePath 如果为空则表示暂停全部文件；如果非空则表示暂停单个文件
	 * @return
	 */
	public static String createPauseReceiveMsg(String filePath) {
		JSONObject json = new JSONObject();
		try {
			json.put(Args.VERSION, MessageConst.VERSION);
			json.put(Args.COMMAND_NO, Command.PAUSE_RECEIVE);
			json.put(Args.FILE_PATH, filePath);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}
	
	public static JSONArray convertToJsonArray(ArrayList<TransferFile> fileList) throws JSONException {
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
		return fileArray;
	}
	
}
