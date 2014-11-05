package myphone.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import myphone.utils.MyPhoneAdapter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

public class AppListActivity extends ActionBarActivity {

	private static PackageManager packageManager = null;
	
	private static GridView mGridView;
	
	private static MyPhoneAdapter applistadapter; 
	
	// 应用列表
	private ArrayList<HashMap<String, Object>> applist = new ArrayList<HashMap<String, Object>>();
	
	private final static int SUCCESS = 0;
	
	private static Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == SUCCESS) {
				mGridView.setAdapter(applistadapter);
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_app_list);
		mGridView = (GridView)findViewById(R.id.gridView1);
		setPackageView();
	}
	
	private void setPackageView() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (packageManager == null) {
					packageManager = getPackageManager();
				}
				List<PackageInfo> allpkginfos = packageManager.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
				for (PackageInfo pkginfo: allpkginfos) {
					String pkgname = getApplicationName(pkginfo);
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put("image", packageManager.getApplicationIcon(pkginfo.applicationInfo));
					map.put("pkgname", pkgname);
					applist.add(map);
				}
				renderView();
			}
		}).start();
	}
	
	private void renderView() {
		applistadapter = new MyPhoneAdapter(this, applist, R.layout.item_app_list, new String[] {"pkgname", "image"}, new int[]{R.id.pkgname, R.id.image});
		
		Message msg = new Message();
		msg.what = SUCCESS;
		handler.sendMessage(msg);
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
