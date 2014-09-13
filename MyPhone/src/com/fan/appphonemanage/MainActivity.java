package com.fan.appphonemanage;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends ActionBarActivity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Log.i("<<<i>>>", "bbb");
		Button tvApplistBtn = (Button)findViewById(R.id.id_main_btn_applist);
		tvApplistBtn.setOnClickListener(this);
		
		Log.i("<<<i>>>", "haha");
		Button tvFilelistBtn = (Button)findViewById(R.id.id_main_btn_filelist);
		tvFilelistBtn.setOnClickListener(this);
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
		}
	}
}
