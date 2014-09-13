package cn.m15.app.android.gotransfer;

import java.io.File;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import org.apache.http.conn.util.InetAddressUtils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import cn.m15.app.android.gotransfer.database.TransferDBHelper;
import cn.m15.app.android.gotransfer.net.httpserver.HTTPServerForShare;
import cn.m15.app.android.gotransfer.net.wifi.WifiApConnector;
import cn.m15.app.android.gotransfer.net.wifi.WifiApManager;
import cn.m15.app.android.gotransfer.utils.FileUtil;

import com.umeng.analytics.MobclickAgent;

public class GoTransferApplication extends Application {

	private static final String DEFAULT_USER_AGENT = "1000";

	private String selfGroup = "Android";
	private String selfMac = "";
	private String machineModel;
	private HTTPServerForShare httpServer;
	private String mStorePath = "";
	private String userAgent = DEFAULT_USER_AGENT;
	private SharedPreferences selfNamePreference;

	private static GoTransferApplication sInstance;

	@Override
	public void onCreate() {
		super.onCreate();
		selfNamePreference = getSharedPreferences("SelfName", MODE_PRIVATE);
		sInstance = this;
		selfMac = getLocalMacAddress();
		
		initMachineModel();
		initUserAgent();

		Context context = getApplicationContext();
		initStorePath(context);
		TransferDBHelper.refreshConversation(context);
		
		MobclickAgent.setDebugMode(true);
		initWifiConnection();
	}
	
	private void initWifiConnection() {
		WifiApManager apManager = WifiApManager.getInstance();
		if (apManager.isWifiApEnabled() && apManager.isWifiApMine()) {
			apManager.setWifiApEnabled(false);
			apManager.setMobileNetworkEnabled(true);
		} else {
			WifiApConnector.getInstance().disconnectFromAP();
		}
	}
	
	private void initUserAgent() {
		try {
			ApplicationInfo ai = getPackageManager().getApplicationInfo(
					getPackageName(), PackageManager.GET_META_DATA);
			Bundle bundle = ai.metaData;
			userAgent = String.valueOf(bundle.getInt("USER_AGENT"));
		} catch (NameNotFoundException e) {
			userAgent = DEFAULT_USER_AGENT;
			e.printStackTrace();
		} catch (ClassCastException e) {
			userAgent = DEFAULT_USER_AGENT;
			e.printStackTrace();
		}
	}

	public static synchronized GoTransferApplication getInstance() {
		return sInstance;
	}

	public String getLocalMacAddress() {
		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if (!wifi.isWifiEnabled()) {
			wifi.setWifiEnabled(true);
		}
		WifiInfo info = wifi.getConnectionInfo();
		String mac = info.getMacAddress();
		if (mac != null) {
			return mac.replace(":", "-");
		}
		return "aa-bb-cc-dd-ee-ff";
	}

	private void initMachineModel() {
		String modelName = Build.MANUFACTURER + " " + Build.MODEL;
		machineModel = modelName.substring(0, 
				modelName.length() >= 15 ? 15 : modelName.length());
	}
	
	public String getMachineModel() {
		return machineModel;
	}

	public String getSelfName() {
		return selfNamePreference.getString("username", machineModel);
	}
	
	public void setSelfName(String username) {
		Editor editor = selfNamePreference.edit();
		editor.putString("username", username);
		editor.commit();
	}

	public String getSelfGroup() {
		return selfGroup;
	}

	public String getSelfMac() {
		return selfMac;
	}

	public String getStorePath() {
		return mStorePath;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public String getBroadcastAddress() {
		WifiApManager apManager = WifiApManager.getInstance();
		if (apManager.isWifiApEnabled() && apManager.isWifiApMine()) {
			return "192.168.43.255";
		}
		try {
			Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
			while (en.hasMoreElements()) {
				NetworkInterface nif = en.nextElement();
				List<InterfaceAddress> ifAddrs = nif.getInterfaceAddresses();
				for (InterfaceAddress ifAddr : ifAddrs) {
					InetAddress iaddr = ifAddr.getAddress();
					if (iaddr != null
							&& !iaddr.isLoopbackAddress()
							&& InetAddressUtils.isIPv4Address(iaddr
									.getHostAddress())) {
						InetAddress ibAddr = ifAddr.getBroadcast();
						if (ibAddr != null) {
							return ibAddr.getHostAddress();
						} else {
							return "255.255.255.255";
						}
					}
				}
			}
		} catch (SocketException ex) {
			ex.printStackTrace();
		}

		return "255.255.255.255";
	}

	public void shareAPKWithHttp(String apkPath) {
		if (httpServer == null) {
			httpServer = new HTTPServerForShare(apkPath, this);
		} else {
			httpServer.setAPKPath(apkPath);
		}
		try {
			httpServer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stopShareHttpServer() {
		try {
			httpServer.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getUniqueId() {
		String uniqueId = "";
		WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		String mac = wm.getConnectionInfo().getMacAddress();
		String androidId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		String imei = tm.getDeviceId();
		uniqueId = mac + androidId + imei;

		if (TextUtils.isEmpty(uniqueId))
			return "";

		MessageDigest m = null;
		try {
			m = MessageDigest.getInstance("MD5");
			m.update(uniqueId.getBytes(), 0, uniqueId.length());
			// get md5 bytes
			byte p_md5Data[] = m.digest();
			// create a hex string
			String m_szUniqueID = new String();
			for (int i = 0; i < p_md5Data.length; i++) {
				int b = (0xFF & p_md5Data[i]);
				// if it is a single digit, make sure it have 0 in front (proper
				// padding)
				if (b <= 0xF)
					m_szUniqueID += "0";
				// add number to string
				m_szUniqueID += Integer.toHexString(b);
			}
			// hex string to uppercase
			m_szUniqueID = m_szUniqueID.toUpperCase(Locale.getDefault());
			return m_szUniqueID;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return uniqueId;
	}

	public static String getAppVersionName(Context context) {
		String versionName = "";
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			versionName = pi.versionName;
			if (TextUtils.isEmpty(versionName)) {
				return "9.9.9";
			}
		} catch (Exception e) {
			Log.e("versionInfo", "Exception", e);
		}
		return versionName;
	}
	
	/**
	 * 获取最佳的存储路径, 并测试目录是否可写入
	 * @param context
	 */
	private void initStorePath(Context context) {
		String storageFolder = File.separator + "GoTransfer" + File.separator;
		String[] paths = FileUtil.getVolumePaths(context);
		
		if (paths == null) {
			mStorePath = Environment.getExternalStorageDirectory().getAbsolutePath() + storageFolder;
		} else if (paths.length == 1) {
			mStorePath = paths[0] + storageFolder;
		} else {
			
			long freeSpace = 0;
			String folder = "";
			
			for (int i = 0; i < paths.length; i++) {
				if (paths[i].toLowerCase(Locale.getDefault()).indexOf("usbotg") >= 0) {
					continue;
				}
				
				File file = new File(paths[i]);
				String testFolder = file.getAbsolutePath() + storageFolder;
				
				// 测试是否可写入
				File fileDir = new File(testFolder);
				if (!fileDir.exists()) { // 若不存在
					boolean result = fileDir.mkdirs();
					if (!result)
						continue;
				}

				// 比较剩余空间，选择较大的 
				long freeSpace2 = file.getFreeSpace();
				if (freeSpace2 > freeSpace) {
					freeSpace = freeSpace2;
					folder = testFolder;
				}
			}
			mStorePath = folder;
		}
		
	}
	
	
}