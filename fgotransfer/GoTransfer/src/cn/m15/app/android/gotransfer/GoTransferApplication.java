package cn.m15.app.android.gotransfer;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v4.app.LoaderManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import cn.m15.app.android.gotransfer.utils.AppLog;
import cn.m15.app.android.gotransfer.utils.ConfigureLogback;
import cn.m15.gotransfer.sdk.ConfigManager;
import cn.m15.gotransfer.sdk.net.wifi.WifiApConnector;
import cn.m15.gotransfer.sdk.utils.LogManager;

import com.umeng.analytics.MobclickAgent;

public class GoTransferApplication extends Application {

	private static final String DEFAULT_USER_AGENT = "1000";

	private String userAgent = DEFAULT_USER_AGENT;

	private static GoTransferApplication sInstance;
	
	@Override
	public void onCreate() {
		super.onCreate();
		Context context = getApplicationContext();
		
		// 初始化LogMananger, ConfigManager
		LogManager.setLogInstance(AppLog.getInstance(Const.DEBUG));
		
		ConfigManager manager = ConfigManager.getInstance();
		manager.init(context);
		manager.setStringForNotEnoughStorage(context.getResources().getString(R.string.storage_too_small));
		
		sInstance = this;
		
		initUserAgent();

		MobclickAgent.setDebugMode(true);

		// 初始化WIFI网络 
		WifiApConnector.getInstance().closeWifiConnection();
		
		ConfigureLogback.configureLogbackDirectly();
		
		if (Const.DEBUG) {
			LoaderManager.enableDebugLogging(true);
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

	public String getUserAgent() {
		return userAgent;
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
	
	
	
}