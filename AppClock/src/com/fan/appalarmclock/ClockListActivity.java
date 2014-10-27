package com.fan.appalarmclock;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.fan.appalarmclock.R;

public class ClockListActivity extends ActionBarActivity implements OnClickListener {

	// 所有应用
	private List<PackageInfo> allPackageInfos;

	// 系统应用
	private List<PackageInfo> sysPackageInfos;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_clock_list);
		
	    startSystemAlarm();
		Button btn1 = (Button) findViewById(R.id.tv_list_btn_back);
		btn1.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case R.id.tv_list_btn_back:
				Intent intent = new Intent(this, MainActivity.class);
				startActivity(intent);
		}
	}
	
	/*
	 * 过滤系统应用
	 */
	private void getSystemApp() {
		PackageManager packageManager = getPackageManager();
		allPackageInfos = packageManager.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES | PackageManager.GET_ACTIVITIES);
		sysPackageInfos = new ArrayList<PackageInfo>();
		
		if (allPackageInfos != null && !allPackageInfos.isEmpty()) {
			for (PackageInfo apckageInfo: allPackageInfos) {
				ApplicationInfo appInfo = apckageInfo.applicationInfo;// 得到每个软件信息
				if ((appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0 || (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
					sysPackageInfos.add(apckageInfo);
				}
			}
			Log.i("<<<all package count>>>", "" + allPackageInfos.size());
			Log.i("<<<system package count>>>", "" + sysPackageInfos.size());
		}
	}
	
	/*
	 * 启动系统闹钟
	 */
	private void startSystemAlarm() {
		getSystemApp();
		String activityName = null, alarmPackageName = null, packageName;
		for (PackageInfo packageInfo: sysPackageInfos) {
			// 包名中包含 clock,
			packageName = packageInfo.packageName;
			if (packageName.indexOf("clock") != -1) {
				if (!(packageName.indexOf("widget") != -1)) {
					ActivityInfo activityInfo = packageInfo.activities[0];
					// activity 名中包含 Alarm 和 DeskClock 大部分的闹钟程序名中都是按照这种命名规则, 不能保证所有的闹钟都是这样
					if (activityInfo.name.indexOf("Alarm") != -1 || activityInfo.name.indexOf("DeskClock") != -1) {
						activityName = activityInfo.name;
						alarmPackageName = packageName;
					}
				}
			}
		}
		if (!activityName.isEmpty() && !alarmPackageName.isEmpty()) {
			Intent intent = new Intent();
			intent.setComponent(new ComponentName(alarmPackageName, activityName));
			startActivity(intent);
		} else {
			Toast.makeText(this, "启动系统闹钟失败！", Toast.LENGTH_SHORT).show();
		}
	}

}
