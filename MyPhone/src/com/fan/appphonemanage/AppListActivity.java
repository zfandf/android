package com.fan.appphonemanage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

public class AppListActivity extends ActionBarActivity {

	private static PackageManager packageManager;
	
	private ArrayList<HashMap<String, Object>> applist = new ArrayList<HashMap<String, Object>>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_app_list);
		getAppList();
	}
	
	public void getAppList() {
		packageManager = getPackageManager();
		List<PackageInfo> allPackages = packageManager.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
		for (PackageInfo pkginfo: allPackages) {
//			Log.i("<<<package name>>>", pkginfo.packageName);
			String pkgname = getApplicationName(pkginfo);

			Log.i("<<<package name>>>", pkginfo.applicationInfo.className + ">>>" + pkgname);
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("image", packageManager.getApplicationIcon(pkginfo.applicationInfo));
			map.put("pkgname", pkgname);
			applist.add(map);
		}
		GridView mGridView = (GridView)findViewById(R.id.gridView1);

		  
		// 使用HashMap将图片添加到一个数组中，注意一定要是HashMap<String,Object>类型的，因为装到map中的图片要是资源ID，而不是图片本身
		// 如果是用findViewById(R.drawable.image)这样把真正的图片取出来了，放到map中是无法正常显示的
		
		AppListAdapter simpleAdapter = new AppListAdapter(this, applist, R.layout.gridviewitem, new String[] {"pkgname", "image"}, new int[]{R.id.pkgname, R.id.image});
		mGridView.setAdapter(simpleAdapter);
	}
	
	/*
	 * 根据包名获取应用的名字
	 */
	public String getApplicationName(PackageInfo pkginfo) { 
		return (String) packageManager.getApplicationLabel(pkginfo.applicationInfo);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.app_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_app_list,
					container, false);
			return rootView;
		}
	}
}
