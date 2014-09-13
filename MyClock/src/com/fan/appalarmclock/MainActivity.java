package com.fan.appalarmclock;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.fan.appalarmclock.R;

public class MainActivity extends ActionBarActivity implements OnClickListener {

	private Button tvBtnList, tvBtnAdd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		tvBtnList = (Button) findViewById(R.id.tv_main_btn_allclock);
		tvBtnAdd = (Button) findViewById(R.id.tv_main_btn_addclock);
		tvBtnList.setOnClickListener(this);
		tvBtnAdd.setOnClickListener(this); 
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();

		Intent intent = new Intent();
		switch (id) {
			case R.id.tv_main_btn_allclock:
				intent.setClassName(this, ClockListActivity.class.getName());
				startActivity(intent);
				break;
			case R.id.tv_main_btn_addclock:
				intent.setAction("android.intent.action.SHOW_ALARMS");
				intent.setComponent(getComponentName());
				break;
		}

		// TODO Auto-generated method stub
		// one
		// Intent intent = new Intent(this, RelativeActivity.class);

		// two
		// Intent intent = new Intent();
		// intent.setClass(this, RelativeActivity.class);

		// three
		// Intent intent = new Intent();
		// intent.setClassName(this, RelativeActivity.class.getName());
	}
}
