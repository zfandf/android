package cn.m15.gotransfer.sdk.net.ipmsg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.m15.gotransfer.sdk.ConfigManager;
import cn.m15.gotransfer.sdk.entity.TransferFile;
import cn.m15.gotransfer.sdk.net.ipmsg.MessageConst.Args;
import cn.m15.gotransfer.sdk.net.ipmsg.MessageConst.FileType;

/**
 * UDP消息
 */
public class UdpMessage {
	
	private int version; 	 // 协议版本号
	public int commandNo;	 // 命令
	private long packetNo; // 数据包ID
	public String senderName;// 发送者名称
	public String senderPlatform;// 发送者主机名
	public String senderMac; // 发送主机MAC
	public String senderDevice;
	public String senderGroup;
	public Map<String, Object> additionalSection; // 附加数据
	
	public UdpMessage() {
		version = MessageConst.VERSION;
		packetNo = System.currentTimeMillis();
		additionalSection = new HashMap<String, Object>();
		senderDevice = ConfigManager.getInstance().getMachineModel();
		senderGroup = "";
	}
	
	public UdpMessage(int commandNo, String senderPlatform, 
			String senderHost, String senderMac) {
		this();
		this.commandNo = commandNo;
		this.senderName = senderPlatform;
		this.senderPlatform = senderHost;
		this.senderMac = senderMac;
	}
	
	public UdpMessage(String messageStr) {
		if (messageStr != null) {
			try {
				JSONObject packet = new JSONObject(messageStr);
				version = packet.optInt("version");
				packetNo = packet.optLong("packetNo");
				commandNo = packet.optInt("commandNo");
				senderName = packet.optString("senderName");
				senderPlatform = packet.optString("senderPlatform");
				senderMac = packet.optString("senderMac");
				senderDevice = packet.optString("senderDevice");
				senderGroup = packet.optString("senderGroup");

				additionalSection = new HashMap<String, Object>();
				JSONObject additionalJson = packet.optJSONObject("additionalSection");
				if (additionalJson != null) {
					@SuppressWarnings("unchecked")
					Iterator<String> keysIter = additionalJson.keys();
					if (keysIter != null) {
						while (keysIter.hasNext()) {
							String key = keysIter.next();
							if (Args.FILE_LIST.equals(key)) {
								ArrayList<TransferFile> fileList = new ArrayList<TransferFile>();
								JSONArray fileJsonArr = additionalJson.optJSONArray(key);
								if (fileJsonArr != null) {
									int length = fileJsonArr.length();
									for (int i = 0; i < length; i++) {
										JSONObject fileJson = fileJsonArr.optJSONObject(i);
										if (fileJson != null) {
											TransferFile file  = new TransferFile();
											file.name = fileJson.optString(Args.FILE_NAME);
											file.path = fileJson.optString(Args.FILE_PATH);
											file.size = fileJson.optLong(Args.FILE_SIZE);
											file.fileType = fileJson.optInt(Args.FILE_TYPE, FileType.OTHERS);
											fileList.add(file);
										}
									}							
								}
								if (fileList.size() > 0) {
									additionalSection.put(key, fileList);							
								}
							} else {
								additionalSection.put(key, additionalJson.opt(key));
							}
						}
					}
				}
			} catch (JSONException e) {
				packetNo = 0;
				e.printStackTrace();
			}
		}
	}
	
	public String toMessageString() {
		String messageStr = "";
		try {
			JSONObject json = new JSONObject();
			json.put("version", version);
			json.put("commandNo", commandNo);
			json.put("packetNo", packetNo);
			json.put("senderName", senderName);
			json.put("senderPlatform", senderPlatform);
			json.put("senderMac", senderMac);
			json.put("senderDevice", senderDevice);
			json.put("senderGroup", ConfigManager.getInstance().getSelfGroup());
			
			JSONObject additionalJson = new JSONObject();
			for (Entry<String, Object> entry : additionalSection.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				if (Args.FILE_LIST.contains(key)) {
					JSONArray fileJsonArr = new JSONArray();
					@SuppressWarnings("unchecked")
					List<TransferFile> fileList = (List<TransferFile>) value;
					for (TransferFile file : fileList) {
						JSONObject fileJson = new JSONObject();
						fileJson.put(Args.FILE_NAME, file.name);
						fileJson.put(Args.FILE_PATH, file.path);
						fileJson.put(Args.FILE_SIZE, file.size);
						fileJson.put(Args.FILE_TYPE, file.fileType);
						fileJsonArr.put(fileJson);
					}
					additionalJson.put(key, fileJsonArr);
				} else {
					additionalJson.put(key, value);
				}
			}
			json.put("additionalSection", additionalJson);
			
			messageStr = json.toString();
		} catch (JSONException ex) {
			// 键为null或使用json不支持的数字格式(NaN, infinities)
			throw new RuntimeException(ex);
		}
		return messageStr;
	}
	
	public int getVersion() {
		return version;
	}
	
	public long getPacketNo() {
		return packetNo;
	}
}
