package myphone.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener {
	
	private Button mBtnApp;
	private Button mBtnFile;
	private Button mBtnContact;
	private Button mBtnPicture;
	
	private static String TAG = "main";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Log.i(TAG, "onCreate");
		mBtnApp = (Button)findViewById(R.id.id_main_btn_applist);
		mBtnApp.setOnClickListener(this);
		
		mBtnFile = (Button)findViewById(R.id.id_main_btn_filelist);
		mBtnFile.setOnClickListener(this);
		
		mBtnContact = (Button)findViewById(R.id.id_main_btn_contacts);
		mBtnContact.setOnClickListener(this);
		
		mBtnPicture = (Button)findViewById(R.id.id_main_btn_picture);
		mBtnPicture.setOnClickListener(this);
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
				intent.setClass(this, FileListActivity.class);
				startActivity(intent);
				break;
			case R.id.id_main_btn_contacts:
				intent.setClass(this, ContactsActivity.class);
				startActivity(intent);
				break;
			case R.id.id_main_btn_picture:
				intent.setClass(this, PictureActivity.class);
				startActivity(intent);
				break;
		}
	}
}
