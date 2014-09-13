package com.fan.appphonemanage;

import java.util.List;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class AppPackageManage {
	
	public void getAppList(PackageManager packageManager) {
		List<PackageInfo> allPackages = packageManager.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
		
	}

}
