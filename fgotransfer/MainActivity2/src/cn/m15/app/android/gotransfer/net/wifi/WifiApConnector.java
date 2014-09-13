package cn.m15.app.android.gotransfer.net.wifi;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;
import cn.m15.app.android.gotransfer.Const;
import cn.m15.app.android.gotransfer.GoTransferApplication;

public class WifiApConnector {
	private List<ScanResult> wifiList;
	private WifiManager wifiManager;
	private WifiReceiver wifiReceiver;
	private WifiStatusReceiver wifiStatusReceiver;

	private ArrayList<ConnectApStatusListener> mListeners;
	
	private static WifiApConnector sInstance;

	public WifiApConnector() {
		wifiManager = (WifiManager) GoTransferApplication.getInstance()
				.getSystemService(Context.WIFI_SERVICE);
		wifiReceiver = new WifiReceiver();
		wifiStatusReceiver = new WifiStatusReceiver();
		mListeners = new ArrayList<ConnectApStatusListener>();
	}

	public static synchronized WifiApConnector getInstance() {
		if (sInstance == null) {
			sInstance = new WifiApConnector();
		}
		return sInstance;
	}
	
	public void closeWifi(){
		wifiManager.setWifiEnabled(false);
	}

	public void refreshAPList() {
		// if (!wifiManager.isWifiEnabled()) {
		wifiManager.setWifiEnabled(false);
		wifiManager.setWifiEnabled(true);
		// }
		GoTransferApplication.getInstance().registerReceiver(wifiReceiver,
				new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		wifiManager.startScan();
	}

	public boolean disconnectFromAP() {
		String ssid = wifiManager.getConnectionInfo().getSSID();
		if (ssid == null) {
			return false;
		}
		if (!ssid.contains(Const.APPREFIX)) {
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
	
	private final class WifiStatusReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
				if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
					WifiInfo info = wifiManager.getConnectionInfo();
					if (info.getSSID() != null
							&& info.getSSID().contains(Const.APPREFIX)) {
						int ip = info.getIpAddress();
						if (ip != 0) {
							try {
								GoTransferApplication.getInstance()
										.unregisterReceiver(wifiStatusReceiver);
							} catch (Exception e) {
								e.printStackTrace();
							}
							
							for (ConnectApStatusListener connectApStatusListener : mListeners) {
								if (connectApStatusListener != null) {
									connectApStatusListener.connectApSuccess();
								}
							}
						}
					}
				}
			}
		}
	}

	private final class WifiReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
				wifiList = wifiManager.getScanResults();
				if (wifiList == null || wifiList.size() == 0)
					return;
				onReceiveNewNetworks(wifiList);
			}
		}
	}

	public boolean isWifiConnected() {
		ConnectivityManager connectivityManager = (ConnectivityManager) GoTransferApplication
				.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
		return (connectivityManager.getNetworkInfo(
				ConnectivityManager.TYPE_WIFI).getState() == State.CONNECTED);
	}

	public void onReceiveNewNetworks(List<ScanResult> wifiList) {
		ArrayList<String> passableHotsPot = new ArrayList<String>();
		for (ScanResult result : wifiList) {
			if ((result.SSID).contains(Const.APPREFIX))
				passableHotsPot.add(result.SSID);
		}
		synchronized (this) {
			Intent intent = new Intent(Const.BROADCAST_ACTION_REFRESH_AP_LIST);
			intent.putExtra(Const.INTENT_EXTRA_AP_LIST, passableHotsPot);
			GoTransferApplication.getInstance().sendBroadcast(intent);
		}
	}

	public boolean connectToAP(String ssid) {
		WifiConfiguration wifiConfig = this.setWifiParams(ssid);

		WifiConfiguration tempConfig = this.IsExsits(ssid);
		if (tempConfig != null) {
			wifiManager.removeNetwork(tempConfig.networkId);
		}

		int wcgId = wifiManager.addNetwork(wifiConfig);
		boolean flag = wifiManager.enableNetwork(wcgId, true);

		try {
			GoTransferApplication.getInstance()
					.unregisterReceiver(wifiReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
		GoTransferApplication.getInstance().registerReceiver(
				wifiStatusReceiver,
				new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
		for (ConnectApStatusListener connectApStatusListener : mListeners) {
			if (connectApStatusListener != null) {
				connectApStatusListener.connectApStarted();
			}
		}
		return flag;
	}
	
	public void addConnectApStatusListener(ConnectApStatusListener listener) {
		if (!mListeners.contains(listener)) {
			mListeners.add(listener);
		}
	}
	
	public void removeConnectApStatusListener(ConnectApStatusListener listener) {
		if (mListeners.contains(listener)) {
			mListeners.remove(listener);
		}
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

		apConfig.hiddenSSID = true;
		apConfig.status = WifiConfiguration.Status.ENABLED;
		apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
		apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
		apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
		apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
		apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
		apConfig.allowedPairwiseCiphers
				.set(WifiConfiguration.PairwiseCipher.TKIP);
		apConfig.allowedPairwiseCiphers
				.set(WifiConfiguration.PairwiseCipher.CCMP);
		apConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
		apConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
		return apConfig;
	}
	
	public boolean isConnectToMine() {
		WifiInfo currentWifi = wifiManager.getConnectionInfo();
		if (currentWifi != null) {
			String ssid = currentWifi.getSSID();
			if (!TextUtils.isEmpty(ssid)) {
				return ssid.contains(Const.APPREFIX);
			}
		}
		return false;
	}
	
	public boolean isCreateOrConnectMine() {
		WifiApManager apManager = WifiApManager.getInstance();
		boolean isCreate = apManager.isWifiApEnabled() && apManager.isWifiApMine();
		boolean isConnect = isConnectToMine();
		return isCreate || isConnect;
	}
	
	public interface ConnectApStatusListener {
		
		public void connectApStarted();
		
		public void connectApSuccess();
		
	}
}
