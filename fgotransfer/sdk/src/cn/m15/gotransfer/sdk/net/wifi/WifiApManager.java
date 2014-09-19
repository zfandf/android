/*
 * 无线热点管理工具类
 */

package cn.m15.gotransfer.sdk.net.wifi;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import cn.m15.gotransfer.sdk.ConfigManager;
import cn.m15.gotransfer.sdk.SdkConst;

public class WifiApManager {

	public final WifiManager mWifiManager;
	public final WifiManager.MulticastLock mWifiLock;

	private static WifiApManager sInstance;
	private boolean previousWifiStatus = true;

	private WifiApManager() {
		Context context = ConfigManager.getInstance().getApplicationContext();
		mWifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		previousWifiStatus = mWifiManager.isWifiEnabled();
		mWifiLock = mWifiManager.createMulticastLock("cn.m15.app.android.gotransfer");
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
	 * @return {@link WifiApState}
	 * @see #isWifiApEnabled()
	 */
	public WifiApState getWifiApState() {
		try {
			Method method = mWifiManager.getClass().getMethod("getWifiApState");

			int tmp = ((Integer) method.invoke(mWifiManager));
			
			// Fix for Android 4
//			if (tmp > 10) {
//				tmp = tmp - 10;
//			}
			tmp = tmp % 5;
			
			return WifiApState.class.getEnumConstants()[tmp];
		} catch (Exception e) {
			Log.e(this.getClass().toString(), "", e);
			return WifiApState.WIFI_AP_STATE_FAILED;
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
		return getWifiApState() == WifiApState.WIFI_AP_STATE_ENABLED;
	}

	/**
	 * 获取本热点是否由GoTransfer创建
	 * @return
	 */
	public boolean isWifiApMine() {
		return getCurrentAPSSID().startsWith(SdkConst.AP_PREFIX);
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
				Handler mainHandler = new Handler(Looper.getMainLooper());
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
			Context context = ConfigManager.getInstance().getApplicationContext();
			connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

			Method method = connectivityManager.getClass().getMethod("setMobileDataEnabled", boolean.class);
			method.invoke(connectivityManager, enabled);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		apConfig.SSID = encodeWifiApName();
		apConfig.priority = 143;

		// 热点不加密
		apConfig.wepTxKeyIndex = 0;   		
		apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);  

		if (isWifiApEnabled()) {
			r = setWifiApEnabled(apConfig, false);
		}

		if (enabled && r) {
			r = setWifiApEnabled(apConfig, true);
		}

		// 如果之前WIFI是打开的，则重新打开WIFI
		if (!enabled && previousWifiStatus
				&& !mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);
		}

		return r;
	}

	public static String encodeWifiApName() {
		String selfname = ConfigManager.getInstance().getSelfName();
		int paddedLength = selfname.length() % 3;
		paddedLength = paddedLength == 0 ? 0 : 3 - paddedLength;
		String paddedSpace = "  ";
		String paddedName = paddedLength != 0 ? selfname
				+ paddedSpace.substring(paddedSpace.length() - paddedLength)
				: selfname;
		String base64Name = android.util.Base64.encodeToString(
				paddedName.getBytes(), Base64.DEFAULT);
		String ssid = SdkConst.AP_PREFIX + base64Name;
		return ssid.trim();
	}
	
	public static String decodeWifiAPName(String apName) {
		String base64Name = apName.substring(SdkConst.AP_PREFIX.length());
		byte[] nameArr = android.util.Base64.decode(
				base64Name, Base64.DEFAULT);
		String userName = new String(nameArr);
		userName = userName.trim();
		return userName;
	}
	
	/**
	 * Get IP address
	 * @return
	 */
	public String[] getWifiIpAddressAndName() {
		WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
		int ip = wifiInfo.getIpAddress();
		String wifiName = wifiInfo.getSSID();
		if (ip != 0) {
			return new String[] {
					(ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "."
							+ ((ip >> 16) & 0xFF) + "." + (ip >> 24 & 0xFF),
					wifiName };
		}
		return null;

	}

}