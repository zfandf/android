package cn.m15.app.android.gotransfer.ui.activity;

import cn.m15.app.android.gotransfer.R;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ConnectFriendsActivity extends BaseActivity 
		implements View.OnClickListener {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connect_friends);
		setTitle(R.string.connect_friends);
		
		findViewById(R.id.btn_create_connection).setOnClickListener(this);
		findViewById(R.id.btn_join_connection).setOnClickListener(this);
		findViewById(R.id.btn_connect_iphone).setOnClickListener(this);
		findViewById(R.id.btn_friends_install).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_create_connection:
			startActivity(new Intent(this, CreateConnectionActivity.class));
			finish();
			break;
		case R.id.btn_join_connection:
			startActivity(new Intent(this, JoinConnectionActivity.class));
			break;
		case R.id.btn_connect_iphone:
			startActivity(new Intent(this, ConnectAppleActivity.class));
			break;
		case R.id.btn_friends_install:
			startActivity(new Intent(this, InviteFriendsActivity.class));
			break;
		}
	}
	
}
