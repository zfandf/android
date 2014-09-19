
/**
 *  Wifi AP 对外接口类
 *  负责AP的创建，搜索，连接 等逻辑
 */

package cn.m15.gotransfer.sdk.net.wifi;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import cn.m15.gotransfer.sdk.SdkConst;
import cn.m15.gotransfer.sdk.net.ipmsg.UdpThreadManager;

public class WifiApConnector {
	// 注：某些手机开启热点后，如果长时间没有连接会自动关闭热点服务
	public static final int WAIT_CONNECT_TIMEOUT = 1000 * 60 * 5;
	
	/**
	 * 连接到AP时的超时设置
	 */
	public static final int CONNECT_AP_TIMEOUT = 1000 * 60;

	// 定义创建AP过程中的各种状态 
	public static final int CREATE_WIFI_AP_INIT = 0;
	public static final int CREATE_WIFI_AP_STARTED = 1;
	public static final int CREATE_WIFI_AP_SUCCESS = 2;
	public static final int CREATE_WIFI_AP_FAILED = 3;
	
	// 定义连接AP的状态 
	public static final int CONNECT_AP_CONNECTING = 1;
	public static final int CONNECT_AP_CONNECTED = 2;
	public static final int CONNECT_AP_DISCONNECTED = 3;
	
	private static WifiApConnector sInstance;

	private List<ScanResult> wifiList;
	private WifiManager wifiManager;
	private WifiScanReceiver wifiScanReceiver;
	private WifiStatusReceiver wifiStatusReceiver;

	private ArrayList<ConnectApStatusListener> mConnectApListeners;
	private ArrayList<CreateWifiApResultListener> mCreateListeners;
	private ArrayList<ConnectWifiListener> mConnectWifiListeners;
	
	private Context mContext = null;
//	private Handler mHandler;
	
//	private int mConnectApTimeout;
	private volatile int mCreateStatus;
	private volatile int mConnectStatus;
	public int mWaitConnectTimeoutNum;
	
//	private Runnable mConnectTimeoutRunnable;

	// 监听WIFI状态，为了在连接上AP之后提醒listener
	private final class WifiStatusReceiver extends BroadcastReceiver {
		
		private boolean isMineWifiAp(NetworkInfo networkInfo) {
			
			String extraInfo = networkInfo.getExtraInfo();
			if (!TextUtils.isEmpty(extraInfo) && extraInfo.contains(SdkConst.AP_PREFIX)) {
				return true;
			}
			
			WifiInfo info = wifiManager.getConnectionInfo();
			if (info != null && info.getSSID() != null 
					&& info.getSSID().contains(SdkConst.AP_PREFIX)) {
				return true;
			} 
			
			return false;
		}
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
				NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				Log.d("WifiApConnector", "networkinfo >>> " + networkInfo + "," + 
						wifiManager.getConnectionInfo().getSSID());
				if (networkInfo != null) {
					boolean isMineWifiAp = isMineWifiAp(networkInfo);
					State state = networkInfo.getState();
					
					if (state == NetworkInfo.State.CONNECTING && isMineWifiAp) { // 正在连接
						mConnectStatus = CONNECT_AP_CONNECTING;
						Log.d("WifiApConnector", "connecting");
					} else if (networkInfo.getState() == NetworkInfo.State.CONNECTED
							&& isMineWifiAp) { // 连接成功
//						if (mConnectTimeoutRunnable != null) {
//							mHandler.removeCallbacks(mConnectTimeoutRunnable);							
//							mConnectTimeoutRunnable = null;
//						}
						mConnectStatus = CONNECT_AP_CONNECTED;
						Log.d("WifiApConnector", "connected");
						for (ConnectApStatusListener connectApStatusListener : mConnectApListeners) {
							if (connectApStatusListener != null) {
								connectApStatusListener.connectApSuccess();
							}
						}
					} else if ((state == NetworkInfo.State.DISCONNECTED
								|| state == NetworkInfo.State.DISCONNECTING)
							&& (mConnectStatus == CONNECT_AP_CONNECTING || mConnectStatus == CONNECT_AP_CONNECTED)) { // 断开连接
						mConnectStatus = CONNECT_AP_DISCONNECTED;
						Log.d("WifiApConnector", "disconnected");
						closeWifiConnection();
						UdpThreadManager.getInstance().disconnectSocket();
						for (ConnectWifiListener listener : mConnectWifiListeners) {
							if (listener != null) {
								listener.wifiDisConnected();
							}
						}
					}
				}
			}
		}
	}

	// 监听WIFI扫描结果，获得扫描得到的WIFI列表并通知
	private final class WifiScanReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
				wifiList = wifiManager.getScanResults();
				if (wifiList == null || wifiList.size() == 0)
					return;
				onReceiveNewNetworks(wifiList);
			}
		}
	}
	
	private WifiApConnector() {
//		mHandler = new Handler();
//		mConnectApTimeout = CONNECT_AP_TIMEOUT;
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
		mContext = context;

		wifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
		wifiScanReceiver = new WifiScanReceiver();
		wifiStatusReceiver = new WifiStatusReceiver();
		
		mConnectApListeners = new ArrayList<ConnectApStatusListener>();
		mCreateListeners = new ArrayList<CreateWifiApResultListener>();
		mConnectWifiListeners = new ArrayList<ConnectWifiListener>();
	}
	
	/**
	 * 获取创建AP状态
	 * @return
	 */
	public int getCreateStatus() {
		return mCreateStatus;
	}
	
	/**
	 * 获取连接AP状态
	 * @return
	 */
	public int getConnectStatus() {
		return mConnectStatus;
	}
	
//	/**
//	 * 设置判断连接AP超时 的 时间长度
//	 * @param timeout
//	 */
//	public void setConnectApTimeout(int timeout) {
//		mConnectApTimeout = timeout;
//	}
	
	/**
	 * 刷新WIFI AP 列表
	 */
	public void refreshAPList() {
		wifiManager.setWifiEnabled(false);
		wifiManager.setWifiEnabled(true);
		mContext.registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		wifiManager.startScan();
	}

	/**
	 * 过滤掉非GT的WIFI
	 * @param wifiList
	 */
	private void onReceiveNewNetworks(List<ScanResult> wifiList) {
		ArrayList<String> passableHotsPot = new ArrayList<String>();
		for (ScanResult result : wifiList) {
			if ((result.SSID).contains(SdkConst.AP_PREFIX))
				passableHotsPot.add(result.SSID);
		}
		synchronized (this) {
			Intent intent = new Intent(SdkConst.BROADCAST_ACTION_REFRESH_AP_LIST);
			intent.putExtra(SdkConst.INTENT_EXTRA_AP_LIST, passableHotsPot);
			mContext.sendBroadcast(intent);
		}
	}

	/**
	 * 返回是否已连接WIFI
	 * @return
	 */
	public boolean isWifiConnected() {
		ConnectivityManager connectivityManager = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		return (connectivityManager.getNetworkInfo(
				ConnectivityManager.TYPE_WIFI).getState() == State.CONNECTED);
	}
	
	/**
	 * 连接到指定的AP
	 * @param ssid  AP SSID
	 * @return
	 */
	public boolean connectToAP(String ssid) {
		WifiConfiguration wifiConfig = this.setWifiParams(ssid);

		WifiConfiguration tempConfig = this.IsExsits(ssid);
		if (tempConfig != null) {
			wifiManager.removeNetwork(tempConfig.networkId);
		}

		int wcgId = wifiManager.addNetwork(wifiConfig);
		boolean flag = wifiManager.enableNetwork(wcgId, true);

		try {
			mContext.unregisterReceiver(wifiScanReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		mContext.registerReceiver(wifiStatusReceiver, intentFilter);
		
		for (ConnectApStatusListener connectApStatusListener : mConnectApListeners) {
			if (connectApStatusListener != null) {
				connectApStatusListener.connectApStarted();
			}
		}
		
		// 连接超时检查
//		final String ap = ssid;
//		mConnectTimeoutRunnable = new Runnable() {
//			public void run() {
//				
//				if (!isConnectedAp(ap) && mConnectStatus != 0) {
//				
//					// unregister receiver
//					try {
//						mContext.unregisterReceiver(wifiStatusReceiver);
//					} catch (Exception e) {
//						// no problem
//					}
//					
//					// notice timeout
//					for (ConnectApStatusListener connectApStatusListener : mConnectApListeners) {
//						if (connectApStatusListener != null) {
//							connectApStatusListener.connectApTimeout();
//						}
//					}
//				}
//			}
//		};
//		mHandler.postDelayed(mConnectTimeoutRunnable, mConnectApTimeout);
		
		return flag;
	}
	
	/**
	 * 断开AP连接
	 * @return
	 */
	public boolean disconnectFromAP() {
		mConnectStatus = 0;
		
		try {
			mContext.unregisterReceiver(wifiStatusReceiver);
		} catch (Exception e) {
			// no problem
		}
				
		String ssid = wifiManager.getConnectionInfo().getSSID();
		if (ssid == null) {
			return false;
		}
		if (!ssid.contains(SdkConst.AP_PREFIX)) {
			return false;
		}
		int netId = wifiManager.getConnectionInfo().getNetworkId();
		if (netId == -1) {
			return false;
		}
		
		boolean flag = wifiManager.disableNetwork(netId);
		wifiManager.removeNetwork(netId);
		
		return flag;
	}

	private WifiConfiguration IsExsits(String SSID) {
		if (wifiManager == null) return null;
		List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
		if (existingConfigs == null || existingConfigs.size() > 0) return null;
		for (WifiConfiguration existingConfig : existingConfigs) {
			if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
				return existingConfig;
			}
		}
		return null;
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
		return WifiApManager.getInstance().isWifiApEnabled();
	}
	
	/**
	 * 判断是否自己已经创建AP
	 * @return boolean
	 */
	public boolean isCreatedAp() {
		// 是否是自己建立的AP
		WifiApManager apManager = WifiApManager.getInstance();
		return apManager.isWifiApEnabled() && apManager.isWifiApMine();
	}
	
	/**
	 * 是否已连接到指定的AP
	 * @param ssid  指定的 AP ssid, 如果是空串，则判断是否已连接上GO快传建立的AP
	 * @return
	 */
	public boolean isConnectedAp(String ssid) {
		
		if (mConnectStatus != CONNECT_AP_CONNECTED) {
			return false;
		}

		// 需要判断是否连上指定AP
		if (!TextUtils.isEmpty(ssid)) {
			WifiInfo currentWifi = wifiManager.getConnectionInfo();
			if (currentWifi != null) {
				return TextUtils.equals(ssid, currentWifi.getSSID());
			}
			return false;
		}

		// 只要mConnectStatus处于连接状态，肯定是GO快传AP
		return true;
	}
	
	/**
	 * 判断是自己创建的AP或者已连接到别人的GoTransfer AP上 
	 * @return
	 */
	public boolean isCreateOrConnectMine() {

		return isCreatedAp() || isConnectedAp("");
	}
	
	/**
	 * 创建WifiAp
	 * @param needPassWord	是否需要Password
	 */
	public void createWifiAp() {
		mWaitConnectTimeoutNum = 0;
		mCreateStatus = CREATE_WIFI_AP_INIT;

		WifiApManager apManager = WifiApManager.getInstance();
		if (apManager.setWifiApEnabled(true)) {
			notifyCreateWifiResult(CREATE_WIFI_AP_STARTED);
			Thread checkWifiApStateThread = new Thread(new Runnable() {

				@Override
				public void run() {
					int times = 0;
					int message = CREATE_WIFI_AP_SUCCESS;
					
					// 循环检查是否创建AP成功
					WifiApManager apManager = WifiApManager.getInstance();
					while (!apManager.isWifiApEnabled()) {
						try {
							Thread.sleep(100);
						} catch (Exception e) {} // no problem
						
						times++;
						
						// 超时，设置状态为失败
						if (times == SdkConst.CREATE_AP_WAIT_SECONDES*10) {
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
		WifiApManager.getInstance().setWifiApEnabled(false);
		mWaitConnectTimeoutNum = 0;
		mCreateStatus = CREATE_WIFI_AP_INIT;
	}
		
	/**
	 * 关闭WIFI
	 */
	public void closeWifiConnection() {
		WifiApManager apManager = WifiApManager.getInstance();
		
//		if (apManager.isWifiApEnabled() && apManager.isWifiApMine()) {
		if (mCreateStatus > CREATE_WIFI_AP_INIT) {
			// 如果是自己创建的AP，关闭
			destroyWifiAp();
			// TODO: 目前和闪传一样打开数据网络，以后可以通过配置来确定
			apManager.setMobileNetworkEnabled(true);
		}
		
		Log.e("WifiApConnector", "closeWifiConnection: " + mConnectStatus);
		if (mConnectStatus >= 0) {
			// 从已连接的AP断开
			disconnectFromAP();
		}
	}
	
	/**
	 * 删除旧的GoTransfer Wifi记录 
	 * @param ctx
	 */
	public static void deleteUselessMyWifis(Context ctx) {
		WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
		if (wifiManager == null) return;
		List<WifiConfiguration> wifiConfigs = wifiManager.getConfiguredNetworks();
		if (wifiConfigs == null || wifiConfigs.size() == 0) return;
		for (WifiConfiguration wifiConfig : wifiConfigs) {
			if (wifiConfig == null || wifiConfig.SSID == null) 	continue;

			if (wifiConfig.SSID.contains(SdkConst.AP_PREFIX)
					&& wifiConfig.status != WifiConfiguration.Status.CURRENT) {
				wifiManager.removeNetwork(wifiConfig.networkId);
			}
		}
		wifiManager.saveConfiguration();
	}
	
	/**
	 *  监听连接到AP状态
	 */
	public interface ConnectApStatusListener {
		
		public void connectApStarted();

		public void connectApSuccess();
		
//		public void connectApTimeout();
	}

	/**
	 * 增加连接AP状态监听器
	 * @param listener
	 */
	public void addConnectApStatusListener(ConnectApStatusListener listener) {
		if (!mConnectApListeners.contains(listener)) {
			mConnectApListeners.add(listener);
		}
	}
	
	/**
	 * 删除连接AP状态监听器
	 * @param listener
	 */
	public void removeConnectApStatusListener(ConnectApStatusListener listener) {
		if (mConnectApListeners.contains(listener)) {
			mConnectApListeners.remove(listener);
		}
	}
	
	/**
	 *  监听创建AP结果
	 */
	public interface CreateWifiApResultListener {

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
	public interface ConnectWifiListener {

		public void wifiDisConnected();
	
	}
	
	/**
	 * 新增Wifi连接状态监听器 
	 * @param listener
	 */
	public void addConnectWifiListener(ConnectWifiListener listener) {
		if (!mConnectWifiListeners.contains(listener)) {
			mConnectWifiListeners.add(listener);
		}
	}

	/**
	 * 删除Wifi连接状态监听器
	 * @param listener
	 */
	public void removeConnectWifiListener(ConnectWifiListener listener) {
		if (mConnectWifiListeners.contains(listener)) {
			mConnectWifiListeners.remove(listener);
		}
	}

}
