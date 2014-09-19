package cn.m15.app.android.gotransfer.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.widget.Toast;
import cn.m15.app.android.gotransfer.GoTransferApplication;
//import android.util.Log;
import cn.m15.app.android.gotransfer.R;

public class AppUtil {
	private AppUtil() {
	}

	public static List<PackageInfo> getAllApps() {
		List<PackageInfo> apps = new ArrayList<PackageInfo>();
		PackageManager pManager = GoTransferApplication.getInstance()
				.getPackageManager();
		// 获取手机内所有应用
		List<PackageInfo> paklist = pManager.getInstalledPackages(0);
		for (int i = 0; i < paklist.size(); i++) {
			PackageInfo pak = (PackageInfo) paklist.get(i);
			// 判断是否为非系统预装的应用程序
			if ((pak.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
				// customs applications
				apps.add(pak);
			}
		}
		return apps;
	}
	
	public static PackageInfo getAppPackageInfo(String packageName) {
		PackageManager pManager = GoTransferApplication.getInstance()
				.getPackageManager();
		
		try {
			return pManager.getPackageInfo(packageName, 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String getAppAPKPath(PackageInfo packageInfo) {
		if (packageInfo == null) {
			return "";
		}
		return packageInfo.applicationInfo.publicSourceDir;
	}
	
	public static boolean isSystemApp(PackageInfo pInfo) {
		return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
	}

	public static void shareAPKWithBluetooth(Context ctx, String apkPath) {
		BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
		if (ba != null) {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_SEND);
			intent.setType("*/*");
			intent.putExtra(Intent.EXTRA_STREAM,
					Uri.fromFile(new File(apkPath)));
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			PackageManager pm = ctx.getPackageManager();
			List<ResolveInfo> appsList = pm.queryIntentActivities(intent, 0);
			boolean found = false;
			if (appsList.size() > 0) {
				for (ResolveInfo info : appsList) {
					found = false;
					try {
						PackageInfo pmInfo = pm.getPackageInfo(
								info.activityInfo.packageName,
								PackageManager.GET_PERMISSIONS);
						boolean isSystemApp = isSystemApp(pmInfo);
						if (info.activityInfo.packageName.endsWith("bluetooth")
								&& isSystemApp) {
							for (String permission : pmInfo.requestedPermissions) {
								if (android.Manifest.permission.BLUETOOTH_ADMIN
										.equals(permission)) {
									found = true;
									break;
								}
							}
							if (found) {
								intent.setClassName(
										info.activityInfo.packageName,
										info.activityInfo.name);
								break;
							}
						}
					} catch (NameNotFoundException e) {
					}
				}
			}
			if (!found) {
				Toast.makeText(ctx, R.string.share_by_others,
						Toast.LENGTH_SHORT).show();
				ctx.startActivity(Intent.createChooser(intent,
						ctx.getString(R.string.share_go_transfer)));
			} else {
				ctx.startActivity(intent);
			}
		}
	}
}
