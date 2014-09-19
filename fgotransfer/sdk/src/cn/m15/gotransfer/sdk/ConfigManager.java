
/**
 * SDK configuration, 对SDK进行各种配置, 初始化
 */
package cn.m15.gotransfer.sdk;

import java.io.File;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import org.apache.http.conn.util.InetAddressUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import cn.m15.gotransfer.sdk.database.TransferDBHelper;
import cn.m15.gotransfer.sdk.net.httpserver.HTTPServerForShare;
import cn.m15.gotransfer.sdk.net.wifi.WifiApConnector;
import cn.m15.gotransfer.sdk.net.wifi.WifiApManager;
import cn.m15.gotransfer.sdk.utils.SdkFileUtil;

public class ConfigManager {

	private static ConfigManager sInstance = null;
	private HTTPServerForShare httpServer;
	
	private Context mContext;
	private String mStorePath = "";
	private String mSelfName = "";
	private String mSelfGroup = "Android";
	private String mSelfMac = "";
	private String mMachineModel;
	
	private String mNotEnoughStorage = "Not enough storage";
	
	private ConfigManager() {
	}
	
	/**
	 * 获取 ConfigManager 实例Singleton
	 * @param context  ApplicationContext
	 * @return
	 */
	public static ConfigManager getInstance() {
		if (sInstance == null) {
			sInstance = new ConfigManager();
		}
		return sInstance;
	}
	
	/**
	 * 获取保存的 Application Context
	 * @return
	 */
	public Context getApplicationContext() {
		return mContext;
	}

	/** 
	 * 获取机器型号
	 * @return
	 */
	public String getMachineModel() {
		return mMachineModel;
	}
	
	/**
	 * 获取当前用户分组
	 * @return
	 */
	public String getSelfGroup() {
		return mSelfGroup;
	}

	/**
	 * 获取本机Mac address
	 * @return
	 */
	public String getSelfMac() {
		return mSelfMac;
	}
	
	/**
	 * 获取用户名
	 * @return String 用户名称
	 */
	public String getSelfName() {
		return mSelfName;
	}
	
	/**
	 * 获取接收文件的存储目录
	 * @return
	 */
	public String getStorePath() {
		return mStorePath;
	}
	
	/**
	 * ConfigManager进行各项初始化
	 * @param context, 必须是ApplicationContext, ConfigManager会保存此Context的Reference
	 * 
	 */
	public void init(Context context) {
		mContext = context;
		
		// 初始化 WifiApConnector
		WifiApConnector connector = WifiApConnector.getInstance();
		connector.initContext(context);

		// 获取机器型号
		String modelName = Build.MANUFACTURER + " " + Build.MODEL;
		mMachineModel = modelName.substring(0, modelName.length() >= 15 ? 15 : modelName.length());
		
		// 获取用户名, 默认为机器型号
		SharedPreferences selfNamePreference = context.getSharedPreferences("SelfName", Context.MODE_PRIVATE);
		mSelfName = selfNamePreference.getString("username", mMachineModel);
		
		mSelfMac = getLocalMacAddress();
		
		initStorePath();
		
		// 异步刷新数据库
		new RefreshDbTask().execute();
	}
	
	/**
	 * 设置用户名称
	 * @param username
	 */
	public void setSelfName(String username) {
		SharedPreferences selfNamePreference = mContext.getSharedPreferences("SelfName", Context.MODE_PRIVATE);
		Editor editor = selfNamePreference.edit();
		editor.putString("username", username);
		editor.commit();
		
		mSelfName = username;
	}
	
	/**
	 * 获取UDP广播地址
	 * @return
	 */
	public String getBroadcastAddress() {
		WifiApManager apManager = WifiApManager.getInstance();
		if (apManager.isWifiApEnabled() && apManager.isWifiApMine()) {
			return "192.168.43.255";
		}
		try {
			Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
			if (en != null) {
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
			}
		} catch (Exception ex) {
			// no problem
		}

		return "255.255.255.255";
	}
	
	/**
	 * 获取本机的Mac address
	 */
	private String getLocalMacAddress() {
		WifiManager wifi = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
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

	/**
	 * 获取最佳的存储路径, 并测试目录是否可写入
	 */
	private void initStorePath() {
		String storageFolder = File.separator + "GoTransfer" + File.separator;
		String[] paths = SdkFileUtil.getVolumePaths(mContext);
		
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
	
	/**
	 * 设置"存储空间不足"字符串
	 * @param text
	 */
	public void setStringForNotEnoughStorage(String text) {
		mNotEnoughStorage = text;
	}

	/**
	 * 获取"存储空间不足"字符串
	 * @return String
	 */
	public String getStringForNotEnoughStorage() {
		return mNotEnoughStorage;
	}
	
	/**
	 * 开启Http服务器，分享APK
	 * @param apkPath 打算分享的apk路径
	 * @param apkName APK 文件名
	 */
	public void shareAPKWithHttp(String apkPath, String apkName) {
		if (httpServer == null) {
			httpServer = new HTTPServerForShare(mContext, apkPath, apkName);
		} else {
			httpServer.setAPKPath(apkPath);
		}
		
		try {
			httpServer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 关闭Http服务器, 停止分享
	 */
	public void stopShareHttpServer() {
		try {
			httpServer.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 *  异步刷新数据库，将未完成的传输标记为失败
	 */
	private class RefreshDbTask extends AsyncTask<Void, Void, Void> {
		protected Void doInBackground(Void... args) {
	    	TransferDBHelper.refreshConversation(mContext);
	    	return null;
	    }
	}
	 	
}
