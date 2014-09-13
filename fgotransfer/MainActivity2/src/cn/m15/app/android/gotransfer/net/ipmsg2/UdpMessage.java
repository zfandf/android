package cn.m15.app.android.gotransfer.net.ipmsg2;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import cn.m15.app.android.gotransfer.GoTransferApplication;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

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
		senderDevice = GoTransferApplication.getInstance().getMachineModel();
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
				JSONTokener jsonParser = new JSONTokener(messageStr);
				JSONObject packet = (JSONObject) jsonParser.nextValue();
				version = packet.getInt("version");
				packetNo = packet.getLong("packetNo");
				commandNo = packet.getInt("commandNo");
				senderName = packet.getString("senderName");
				senderPlatform = packet.getString("senderPlatform");
				senderMac = packet.getString("senderMac");
				senderDevice = packet.getString("senderDevice");
				senderGroup = packet.optString("senderGroup", "");
				
				additionalSection = new HashMap<String, Object>();
				String additionals = packet.getString("additionalSection");

				Gson gson = new Gson();
				@SuppressWarnings("unchecked")
				Map<String, Object> addi = gson.fromJson(additionals, LinkedTreeMap.class);

				Iterator<Entry<String, Object>> iter = addi.entrySet().iterator();
				while (iter.hasNext()) {
					Entry<String, Object> entry = iter.next();
					additionalSection.put(entry.getKey(), entry.getValue());
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
			json.put("senderGroup", GoTransferApplication.getInstance().getSelfGroup());
			
			Gson gson = new Gson();
			json.put("additionalSection", gson.toJson(additionalSection));
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
