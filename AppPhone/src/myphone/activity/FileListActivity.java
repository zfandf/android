package myphone.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import myphone.utils.MyPhoneAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class FileListActivity extends ActionBarActivity implements OnClickListener {

	private static ArrayList<HashMap<String, Object>> filelist; // 文件列表
	
	private static MyPhoneAdapter fileadapter; // 文件列表适配器
	
	private static String path = File.separator; // 文件分隔符
	
	private static ListView mGridView; // 文件列表list
	
	private static int tvNavId;
	
	private static TextView mNav; // 头部展示处于位置信息
	
	private final static int SUCCESS = 0; // 常量， 成功表示0
	
	private static Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == SUCCESS) {
				mGridView.setAdapter(fileadapter);
			}
		};
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_list);
		
		mGridView = (ListView)findViewById(R.id.gridView1);
		mNav = (TextView)findViewById(R.id.id_nav_header);
		
		setFileList();
		
		// 设置list头部返回上一级
		TextView tvNav = new TextView(this);
		tvNav.setText("返回上一级");
		tvNav.setOnClickListener(this);
		mGridView.addHeaderView(tvNav);
		tvNavId = tvNav.getId();
		
		// 初始化list列表点击事件
		mGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long row) {
				// TODO Auto-generated method stub
				Log.i("<<<>>>", "hahaha");
				TextView v = (TextView)parent.getChildAt(position).findViewById(R.id.id_file_name);
				path = path + v.getText().toString() + File.separator;
				Log.i("<<<>>>", path);
				mNav.setText(path);
				setFileList();
			}
		});
	}
	
	private void setFileList() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				if (filelist == null) {
					filelist = new ArrayList<HashMap<String, Object>>();
				} else {
					filelist.clear();
				}
				
				// TODO Auto-generated method stub
				File file = new File(path);
				File[] files = file.listFiles();
				for (File f:files) {
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put("filename", f.getName());
					if (f.isFile()) {
						map.put("fileicon", R.drawable.file);
					} else {
						map.put("fileicon", R.drawable.folder);
					}
					
					map.put("filesize", "10M");
//					Log.i("<<<filename>>>", f.getName());
					filelist.add(map);
				}
				renderList();
			}
		}).start();
	}
	
	private void renderList() {
		if (!(fileadapter == null)) {
			fileadapter.notify();
		}
		fileadapter = new MyPhoneAdapter(this, filelist, R.layout.item_file_list, new String[]{"filename", "filesize", "fileicon"}, new int[]{R.id.id_file_name, R.id.id_file_size, R.id.id_file_icon});
		Message msg = new Message();
		msg.what = SUCCESS;
		handler.sendMessage(msg);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.file_list, menu);
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
			View rootView = inflater.inflate(R.layout.fragment_file_list,
					container, false);
			return rootView;
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		if (id == tvNavId) {
			File file = new File(path);
			File parent = file.getParentFile();
			path = parent + File.separator;
			Log.i("<<<>>>", path);
			mNav.setText(path);
			setFileList();
		}
	}
}
