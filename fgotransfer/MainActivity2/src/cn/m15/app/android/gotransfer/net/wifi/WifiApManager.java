/*
 * 无线热点管理工具类
 */

package cn.m15.app.android.gotransfer.net.wifi;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import cn.m15.app.android.gotransfer.Const;
import cn.m15.app.android.gotransfer.GoTransferApplication;

public class WifiApManager {

	public static final int WIFI_AP_ENABLED = 1;
	public static final int WIFI_AP_DISABLED = 1;

	private static final int CREATE_WIFI_AP_STARTED = 1;
	private static final int CREATE_WIFI_AP_SUCCESS = 2;
	private static final int CREATE_WIFI_AP_FAILED = 3;

	public final WifiManager mWifiManager;
	public final WifiManager.MulticastLock mWifiLock;

	private static WifiApManager sInstance;

	private List<CreateWifiApResultListener> mListeners;

	private boolean previousWifiStatus = true;

	public WifiApManager() {
		mWifiManager = (WifiManager) GoTransferApplication.getInstance()
				.getSystemService(Context.WIFI_SERVICE);
		previousWifiStatus = mWifiManager.isWifiEnabled();
		mWifiLock = mWifiManager.createMulticastLock("cn.m15.app.android.gotransfer");
		mListeners = new ArrayList<WifiApManager.CreateWifiApResultListener>();
	}

	public static synchronized WifiApManager getInstance() {
		if (sInstance == null) {
			sInstance = new WifiApManager();
		}
		return sInstance;
	}

	/**
	 * Start AccessPoint mode with the specified configuration. If the radio is
	 * already running in AP mode, update the new configuration Note that
	 * starting in access point mode disables station mode operation
	 * 
	 * @param wifiConfig
	 *            SSID, security and channel details as part of
	 *            WifiConfiguration
	 * @return {@code true} if the operation succeeds, {@code false} otherwise
	 */
	public boolean setWifiApEnabled(WifiConfiguration wifiConfig,
			boolean enabled) {
		try {
			if (enabled) { // disable WiFi in any case
				mWifiManager.setWifiEnabled(false);
			}

			if (isHTC()) {
				setHTCWifiApConfiguration(wifiConfig);
			}
			Method method = mWifiManager.getClass().getMethod(
					"setWifiApEnabled", WifiConfiguration.class, boolean.class);
			return (Boolean) method.invoke(mWifiManager, wifiConfig, enabled);
		} catch (Exception e) {
			Log.e(this.getClass().toString(), "", e);
			return false;
		}
	}

	/**
	 * Gets the Wi-Fi enabled state.
	 * 
	 * @return {@link WIFI_AP_STATE}
	 * @see #isWifiApEnabled()
	 */
	public WIFI_AP_STATE getWifiApState() {
		try {
			Method method = mWifiManager.getClass().getMethod("getWifiApState");

			int tmp = ((Integer) method.invoke(mWifiManager));
			
			// Fix for Android 4
//			if (tmp > 10) {
//				tmp = tmp - 10;
//			}
			tmp = tmp % 5;
			
			return WIFI_AP_STATE.class.getEnumConstants()[tmp];
		} catch (Exception e) {
			Log.e(this.getClass().toString(), "", e);
			return WIFI_AP_STATE.WIFI_AP_STATE_FAILED;
		}
	}

	/**
	 * Return whether Wi-Fi AP is enabled or disabled.
	 * 
	 * @return {@code true} if Wi-Fi AP is enabled
	 * @see #getWifiApState()
	 * 
	 * @hide Dont open yet
	 */
	public boolean isWifiApEnabled() {
		return getWifiApState() == WIFI_AP_STATE.WIFI_AP_STATE_ENABLED;
	}

	/**
	 * Gets the Wi-Fi AP Configuration.
	 * 
	 * @return AP details in {@link WifiConfiguration}
	 */
	public WifiConfiguration getWifiApConfiguration() {
		try {
			Method method = mWifiManager.getClass().getMethod(
					"getWifiApConfiguration");
			return (WifiConfiguration) method.invoke(mWifiManager);
		} catch (Exception e) {
			Log.e(this.getClass().toString(), "", e);
			return null;
		}
	}

	public boolean isHTC() {
		boolean isHtc = false;
		try {
			isHtc = (WifiConfiguration.class.getDeclaredField("mWifiApProfile") != null);
		} catch (java.lang.NoSuchFieldException e) {
			isHtc = false;
		}
		return isHtc;
	}

	/**
	 * Sets the Wi-Fi AP Configuration.
	 * 
	 * @return {@code true} if the operation succeeded, {@code false} otherwise
	 */
	public boolean setWifiApConfiguration(WifiConfiguration wifiConfig) {
		try {
			if (isHTC()) {
				setHTCWifiApConfiguration(wifiConfig);
			}
			Method method = mWifiManager.getClass().getMethod(
					"setWifiApConfiguration", WifiConfiguration.class);
			return (Boolean) method.invoke(mWifiManager, wifiConfig);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public String getCurrentAPSSID() {
		WifiConfiguration config = WifiApManager.getInstance()
				.getWifiApConfiguration();
		if (config != null) {
			if (isHTC()) {
				try {
					Field mWifiApProfileField = WifiConfiguration.class
							.getDeclaredField("mWifiApProfile");
					mWifiApProfileField.setAccessible(true);
					Object hotSpotProfile = mWifiApProfileField.get(config);
					mWifiApProfileField.setAccessible(false);
					if (hotSpotProfile != null) {
						Field ssidField = hotSpotProfile.getClass()
								.getDeclaredField("SSID");
						String ssid = (String) ssidField.get(hotSpotProfile);
						return ssid == null ? "" : ssid;
					} else {
						return "";
					}
				} catch (Exception e) {
					e.printStackTrace();
					return "";
				}
			} else {
				return config.SSID;
			}
		} else {
			return "";
		}
	}

	public void setHTCWifiApConfiguration(WifiConfiguration config) {
		try {
			Field mWifiApProfileField = WifiConfiguration.class
					.getDeclaredField("mWifiApProfile");
			mWifiApProfileField.setAccessible(true);
			Object hotSpotProfile = mWifiApProfileField.get(config);
			mWifiApProfileField.setAccessible(false);

			if (hotSpotProfile != null) {
				Field ssidField = hotSpotProfile.getClass().getDeclaredField(
						"SSID");
				ssidField.setAccessible(true);
				ssidField.set(hotSpotProfile, config.SSID);
				ssidField.setAccessible(false);

				Field secureField = hotSpotProfile.getClass().getDeclaredField(
						"secureType");
				secureField.setAccessible(true);
				if (config.preSharedKey.equals("")) {
					secureField.set(hotSpotProfile, "open");
				} else {
					secureField.set(hotSpotProfile, "wpa2-psk");
				}
				secureField.setAccessible(false);

				// 密码设置
				Field passField = hotSpotProfile.getClass().getDeclaredField(
						"key");
				passField.setAccessible(true);
				passField.set(hotSpotProfile, config.preSharedKey);
				passField.setAccessible(false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets a list of the clients connected to the Hotspot, reachable timeout is
	 * 300
	 * 
	 * @param onlyReachables
	 *            {@code false} if the list should contain unreachable (probably
	 *            disconnected) clients, {@code true} otherwise
	 * @param finishListener
	 *            , Interface called when the scan method finishes
	 */
	public void getClientList(boolean onlyReachables,
			FinishScanListener finishListner) {
		getClientList(onlyReachables, 300, finishListner);
	}

	/**
	 * Gets a list of the clients connected to the Hotspot
	 * 
	 * @param onlyReachables
	 *            {@code false} if the list should contain unreachable (probably
	 *            disconnected) clients, {@code true} otherwise
	 * @param reachableTimeout
	 *            Reachable Timout in miliseconds
	 * @param finishListener
	 *            , Interface called when the scan method finishes
	 */
	public void getClientList(final boolean onlyReachables,
			final int reachableTimeout, final FinishScanListener finishListener) {

		Runnable runnable = new Runnable() {
			public void run() {

				BufferedReader br = null;
				final ArrayList<ClientScanResult> result = new ArrayList<ClientScanResult>();

				try {
					br = new BufferedReader(new FileReader("/proc/net/arp"));
					String line;
					while ((line = br.readLine()) != null) {
						String[] splitted = line.split(" +");

						if ((splitted != null) && (splitted.length >= 4)) {
							// Basic sanity check
							String mac = splitted[3];

							if (mac.matches("..:..:..:..:..:..")) {
								boolean isReachable = InetAddress.getByName(
										splitted[0]).isReachable(
										reachableTimeout);

								if (!onlyReachables || isReachable) {
									result.add(new ClientScanResult(
											splitted[0], splitted[3],
											splitted[5], isReachable));
								}
							}
						}
					}
				} catch (Exception e) {
					Log.e(this.getClass().toString(), e.toString());
				} finally {
					if (br != null) {
						try {
							br.close();
						} catch (IOException e) {
							Log.e(this.getClass().toString(), e.getMessage());
						}
					}
				}

				// Get a handler that can be used to post to the main thread
				Handler mainHandler = new Handler(GoTransferApplication
						.getInstance().getMainLooper());
				Runnable myRunnable = new Runnable() {
					@Override
					public void run() {
						finishListener.onFinishScan(result);
					}
				};
				mainHandler.post(myRunnable);
			}
		};

		Thread mythread = new Thread(runnable);
		mythread.start();
	}

	public void setMobileNetworkEnabled(boolean enabled) {
		ConnectivityManager connectivityManager = null;
		try {
			connectivityManager = (ConnectivityManager) GoTransferApplication
					.getInstance().getSystemService(
							Context.CONNECTIVITY_SERVICE);
			Method method = connectivityManager.getClass().getMethod(
					"setMobileDataEnabled", boolean.class);
			method.invoke(connectivityManager, enabled);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void deleteUselessMyWifis(Context ctx) {
		WifiManager wifiManager = (WifiManager) ctx
				.getSystemService(Context.WIFI_SERVICE);
		if (wifiManager == null)
			return;
		List<WifiConfiguration> wifiConfigs = wifiManager
				.getConfiguredNetworks();
		if (wifiConfigs == null || wifiConfigs.size() == 0)
			return;
		for (WifiConfiguration wifiConfig : wifiConfigs) {
			if (wifiConfig == null || wifiConfig.SSID == null)
				continue;
			Log.d("MainActivity", wifiConfig.SSID + ", " + wifiConfig.status);

			if (wifiConfig.SSID.contains(Const.APPREFIX)
					&& wifiConfig.status != WifiConfiguration.Status.CURRENT) {
				wifiManager.removeNetwork(wifiConfig.networkId);
			}
		}
		wifiManager.saveConfiguration();
	}

	public boolean setWifiApEnabled(boolean enabled) {
		boolean r = true;
		// 热点的配置类
		WifiConfiguration apConfig = new WifiConfiguration();

		apConfig.allowedAuthAlgorithms.clear();
		apConfig.allowedGroupCiphers.clear();
		apConfig.allowedKeyManagement.clear();
		apConfig.allowedPairwiseCiphers.clear();
		apConfig.allowedProtocols.clear();
		// 配置热点的名称

		apConfig.SSID = getWifiApName().trim();

		if (WifiApManager.getInstance().isWifiApEnabled()) {
			r = WifiApManager.getInstance().setWifiApEnabled(apConfig, false);
		}

		if (enabled && r) {
			r = WifiApManager.getInstance().setWifiApEnabled(apConfig, true);
		}

		// 如果之前WIFI是打开的，则重新打开WIFI
		if (!enabled && previousWifiStatus
				&& !WifiApManager.getInstance().mWifiManager.isWifiEnabled()) {
			WifiApManager.getInstance().mWifiManager.setWifiEnabled(true);
		}

		return r;
	}

	public String getWifiApName() {
		String selfname = GoTransferApplication.getInstance().getSelfName();
		int paddedLength = selfname.length() % 3;
		paddedLength = paddedLength == 0 ? 0 : 3 - paddedLength;
		String paddedSpace = "  ";
		String paddedName = paddedLength != 0 ? selfname
				+ paddedSpace.substring(paddedSpace.length() - paddedLength)
				: selfname;
		String base64Name = android.util.Base64.encodeToString(
				paddedName.getBytes(), Base64.DEFAULT);
		String ssid = Const.APPREFIX + base64Name;
		return ssid.trim();
	}

	public boolean isWifiApMine() {
		return WifiApManager.getInstance().getCurrentAPSSID()
				.startsWith(Const.APPREFIX);
	}


	public void addCreateWifiApResultListener(CreateWifiApResultListener listener) {
		if (!mListeners.contains(listener)) {
			mListeners.add(listener);
		}
	}

	public void removeCreateWifiApResultListener(
			CreateWifiApResultListener listener) {
		if (mListeners.contains(listener)) {
			mListeners.remove(listener);
		}
	}

	/**
	 * 创建WifiAp
	 */
	public void createWifiAp() {
		if (setWifiApEnabled(true)) {
			notifyCreateWifiResult(CREATE_WIFI_AP_STARTED);
			Thread checkWifiApStateThread = new Thread(new Runnable() {

				@Override
				public void run() {
					int times = 0;
					int message = CREATE_WIFI_AP_SUCCESS;
					
					// 循环坚持是否创建成功
					while (!isWifiApEnabled()) {
						try {
							Thread.sleep(100);
						} catch (Exception e) {} // no problem
						
						times++;
						Log.d("wait", String.valueOf(times));
						
						// 超时，设置状态为失败
						if (times == Const.CREATE_AP_WAIT_SECONDES*10) {
							message = CREATE_WIFI_AP_FAILED;
							break;
						}
					}
					
					// 通知 UI Thread
					final int status = message;
					(new Handler(Looper.getMainLooper())).post(new Runnable() {
						public void run() { 
					    	 notifyCreateWifiResult(status);
					    }
					});
					
				}
			});
			checkWifiApStateThread.start();

		} else {
			notifyCreateWifiResult(CREATE_WIFI_AP_FAILED);
		}
	}

	public void destroyWifiAp() {
		setWifiApEnabled(false);
		mListeners.clear();
	}
	
	public void openWifi() {
		mWifiManager.setWifiEnabled(true);
	}
	
	/**
	 * 通知listener 创建Wifi AP 的状态
	 * @param result
	 */
	private void notifyCreateWifiResult(int result) {
		for (CreateWifiApResultListener listener : mListeners) {
			if (listener != null) {
				switch (result) {
				case CREATE_WIFI_AP_STARTED:
					listener.createWifiApStarted();
					break;

				case CREATE_WIFI_AP_SUCCESS:
					listener.createWifiSuccess();
					break;

				case CREATE_WIFI_AP_FAILED:
					listener.createWifiFailed();
					break;

				default:
					break;
				}
			}
		}
	}

	public interface CreateWifiApResultListener {

		public void createWifiApStarted();

		public void createWifiSuccess();

		public void createWifiFailed();

	}
	
	/**
	 * Get IP address, if necessary, close AP modal and open wifi.
	 * @return
	 */
	public String getWifiIpAddress() {
		if (isWifiApEnabled()) {
			destroyWifiAp();
		}
		if (!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);
		}
		WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
		int ip = wifiInfo.getIpAddress();
		if (ip != 0) {
			return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "."
					+ ((ip >> 16) & 0xFF) + "." + (ip >> 24 & 0xFF);
		}
		return null;		
	}
	
}