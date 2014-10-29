package cn.m15.gotransfersimplest.net.wifi;

import java.util.ArrayList;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.os.Handler;
import android.os.Looper;

public class WifiApConnector {
	
	// 定义创建AP过程中的各种状态 
	public static final int CREATE_WIFI_AP_INIT = 0;
	public static final int CREATE_WIFI_AP_STARTED = 1;
	public static final int CREATE_WIFI_AP_SUCCESS = 2;
	public static final int CREATE_WIFI_AP_FAILED = 3;
	
	private static WifiApConnector sInstance;

	private WifiApManager mApManager;
	private ArrayList<CreateWifiApResultListener> mCreateListeners;
	private volatile int mCreateStatus;
	
	private WifiApConnector() {
	}

	/**
	 * 获取WifiApConnector 实例
	 * @return
	 */
	public static synchronized WifiApConnector getInstance() {
		if (sInstance == null) {
			sInstance = new WifiApConnector();
		}
		return sInstance;
	}
	
	/**
	 * 初始化 Context
	 * @param context，需要Application Context
	 */
	public void initContext(Context context) {
		mApManager = WifiApManager.getInstance(context);
		mCreateListeners = new ArrayList<CreateWifiApResultListener>();
	}
	
	/**
	 * 获取创建AP状态
	 * @return
	 */
	public int getCreateStatus() {
		return mCreateStatus;
	}
	
	public WifiConfiguration setWifiParams(String ssid) {
		WifiConfiguration apConfig = new WifiConfiguration();
		apConfig.allowedAuthAlgorithms.clear();
		apConfig.allowedGroupCiphers.clear();
		apConfig.allowedKeyManagement.clear();
		apConfig.allowedPairwiseCiphers.clear();
		apConfig.allowedProtocols.clear();

		apConfig.SSID = "\"" + ssid + "\"";

		apConfig.wepTxKeyIndex = 0;   		
		apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);  
		apConfig.status = WifiConfiguration.Status.ENABLED;

		return apConfig;
	}

	/**
	 * 返回WIFI AP是否已创建成功
	 * @return
	 */
	public boolean isWifiApEnabled() {
		return mApManager.isWifiApEnabled();
	}
	
	/**
	 * 判断是否自己已经创建AP
	 * @return boolean
	 */
	public boolean isCreatedAp() {
		return mApManager.isWifiApEnabled() && mApManager.isWifiApMine();
	}
	
	/**
	 * 创建WifiAp
	 * @param needPassWord	是否需要Password
	 */
	public void createWifiAp() {
		mCreateStatus = CREATE_WIFI_AP_INIT;

		if (mApManager.setWifiApEnabled(true)) {
			notifyCreateWifiResult(CREATE_WIFI_AP_STARTED);
			Thread checkWifiApStateThread = new Thread(new Runnable() {

				@Override
				public void run() {
					int times = 0;
					int message = CREATE_WIFI_AP_SUCCESS;
					
					// 循环检查是否创建AP成功
					while (!mApManager.isWifiApEnabled()) {
						try {
							Thread.sleep(100);
						} catch (Exception e) {} // no problem
						
						times++;
						
						// 超时，设置状态为失败
						if (times == 30 * 10) { // 30s
							message = CREATE_WIFI_AP_FAILED;
							break;
						}
					}
					
					// 如果不是主动取消创建AP, 通知 UI Thread
					if (mCreateStatus != CREATE_WIFI_AP_INIT) {
						final int status = message;
						(new Handler(Looper.getMainLooper())).post(new Runnable() {
							public void run() { 
						    	 notifyCreateWifiResult(status);
						    }
						});
					} else {
						destroyWifiAp();
					}
					
				}
			});
			checkWifiApStateThread.start();

		} else {
			notifyCreateWifiResult(CREATE_WIFI_AP_FAILED);
		}
	}

	/**
	 * 通知listener 创建Wifi AP 的状态
	 * @param result
	 */
	private void notifyCreateWifiResult(int result) {
		mCreateStatus = result;
		for (CreateWifiApResultListener listener : mCreateListeners) {
			if (listener != null) {
				switch (result) {
				case CREATE_WIFI_AP_STARTED:
					listener.createWifiApStarted();
					break;

				case CREATE_WIFI_AP_SUCCESS:
					listener.createWifiApSuccess();
					break;

				case CREATE_WIFI_AP_FAILED:
					listener.createWifiApFailed();
					break;

				default:
					break;
				}
			}
		}
	}
	
	/**
	 * 销毁已经创建的AP
	 */
	public void destroyWifiAp() {
		mApManager.setWifiApEnabled(false);
		mCreateStatus = CREATE_WIFI_AP_INIT;
	}
		
	/**
	 * 关闭WIFI
	 */
	public void closeWifiConnection() {
		if (mCreateStatus > CREATE_WIFI_AP_INIT || (mApManager.isWifiApEnabled() && mApManager.isWifiApMine())) {
			// 如果是自己创建的AP，关闭
			destroyWifiAp();
			// TODO: 目前和闪传一样打开数据网络，以后可以通过配置来确定
			mApManager.setMobileNetworkEnabled(true);
		}
	}
	
	/**
	 *  监听创建AP结果
	 */
	public static interface CreateWifiApResultListener {

		public void createWifiApStarted();

		public void createWifiApSuccess();

		public void createWifiApFailed();
	}
	
	/**
	 * 新增创建AP结果监听器
	 * @param listener
	 */
	public void addCreateWifiApResultListener(CreateWifiApResultListener listener) {
		if (!mCreateListeners.contains(listener)) {
			mCreateListeners.add(listener);
		}
	}

	/**
	 * 删除创建AP结果监听器
	 * @param listener
	 */
	public void removeCreateWifiApResultListener(CreateWifiApResultListener listener) {
		if (mCreateListeners.contains(listener)) {
			mCreateListeners.remove(listener);
		}
	}
	
	/**
	 *  监听 Wifi Connection Status
	 */
	public static interface ConnectWifiListener {

		public void wifiDisConnected();
	
	}
	
}
