package tools;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class ToolWifiManage {
	
	public static String getIpAddress(Context ctx) {
		String ipAddress = null;
		try {
			WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			int ip = wifiInfo.getIpAddress();
			if (ip != 0) {
				ipAddress = (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "."
						+ ((ip >> 16) & 0xFF) + "." + (ip >> 24 & 0xFF);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.i("<<<wifi catch>>>", "你没有权限");
		}
		return ipAddress;	
	}
}
