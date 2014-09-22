package myphone.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import myphone.activity.R;

public class MainActivity extends ActionBarActivity implements OnClickListener {

	private static Button tvAppBtn;
	private static Button tvFileBtn;
	private static Button tvContactBtn;
	
	private static String TAG = "main";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		tvAppBtn = (Button)findViewById(R.id.id_main_btn_applist);
		tvAppBtn.setOnClickListener(this);
		
		tvFileBtn = (Button)findViewById(R.id.id_main_btn_filelist);
		tvFileBtn.setOnClickListener(this);
		
		tvContactBtn = (Button)findViewById(R.id.id_main_btn_contacts);
		tvContactBtn.setOnClickListener(this);
		Log.i("<<start photo>>>", "hahahah");
		getPhotos();
	}
	
	public void getPhotos() {
		Log.i("<<start photo>>>", "start");
		Log.i("<<start photo>>>", "end");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		Log.i("<<<click>>>>", "" + id);
		Intent intent = new Intent();
		switch (id) {
			case R.id.id_main_btn_applist:
				intent.setClass(this, AppListActivity.class);
				startActivity(intent);
				break;
			case R.id.id_main_btn_filelist:
				Log.i("<<<click>>>", FileListActivity.class.getName());
				intent.setClass(this, FileListActivity.class);
				startActivity(intent);
				break;
			case R.id.id_main_btn_contacts:
				Log.i("<<<click>>>", ContactsActivity.class.getName());
				intent.setClass(this, ContactsActivity.class);
				startActivity(intent);
				break;
		}
	}
		
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.i(TAG, "hahaha");
		android.os.Debug.stopMethodTracing();
	}
}
