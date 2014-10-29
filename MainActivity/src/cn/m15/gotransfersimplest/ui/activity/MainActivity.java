package cn.m15.gotransfersimplest.ui.activity;

import java.io.File;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import cn.m15.gotransfersimplest.Const;
import cn.m15.gotransfersimplest.R;
import cn.m15.gotransfersimplest.entry.TransferFile;
import cn.m15.gotransfersimplest.utils.ValueConvertUtil;

public class MainActivity extends BaseActivity implements View.OnClickListener {
	private ArrayList<TransferFile> mTransferFileList;
	private Handler mHandler;
	private Runnable mCreateApRunnable;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViewById(R.id.btn_goto_play).setOnClickListener(this);
		
		mTransferFileList = new ArrayList<TransferFile>();
		
		LinearLayout contentLL = (LinearLayout) findViewById(R.id.ll_content);
		String[] sendFileTypes = getResources().getStringArray(R.array.send_file_types);
		for (int i = 0; i < sendFileTypes.length; i++) {
			Button aButton = new Button(this);
			aButton.setTag(i);
			aButton.setText(sendFileTypes[i]);
			aButton.setOnClickListener(this);
			contentLL.addView(aButton);
		}
		
		mHandler = new Handler();
		mCreateApRunnable = new Runnable() {
			
			@Override
			public void run() {
				if (mApConnector.isCreatedAp()) {
					mApConnector.createWifiAp();
				}
			}
		};
		mHandler.postDelayed(mCreateApRunnable, 5000);
	} 
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mHandler.removeCallbacks(mCreateApRunnable);
	}
	
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_goto_play) {
			// TODO: 			
		} else {
			mTransferFileList.clear();
			int fileType = (Integer) v.getTag();
			pickFiles(fileType);
		}
	}
	
	@SuppressLint("InlinedApi")
	private void pickFiles(int fileType) {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		switch (fileType) {
		case Const.PICTURE:
			intent.setType("image/*");
			break;
		case Const.VIDEO:
			intent.setType("video/*");
			break;
		case Const.MUSIC:
			intent.setType("audio/*");
			break;
		case Const.APP:
			intent.setType("application/vnd.android.package-archive");
			break;
		case Const.FILE:
			intent.setType("*/*");
			break;
		}
		// selected file can be as a stream
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		if (VERSION.SDK_INT >= 11) {
			intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);				
		}
		if (VERSION.SDK_INT >= 18) {
			intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);				
		}
		startActivityForResult(intent, fileType);
	}
	
	@SuppressLint("NewApi")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data != null && resultCode == RESULT_OK) {
			Uri selectedUri = data.getData();
			if (selectedUri != null) {
				handleUri(requestCode, selectedUri);
			} else if (VERSION.SDK_INT >= 16) {
				ClipData clipData = data.getClipData();
				if (clipData != null) {
					int itemCount = clipData.getItemCount();
					for (int i = 0; i < itemCount; i++) {
						ClipData.Item item = clipData.getItemAt(i);
						Uri uri = item.getUri();
						handleUri(requestCode, uri);
					}
				}
			}
			
			if (mTransferFileList.size() > 0) {
				Intent intent = new Intent(this, TransferGuideActivity.class);
				intent.putExtra("transfer_files", ValueConvertUtil.convertToJsonString(
						ValueConvertUtil.getFileTypeStr(this, requestCode), mTransferFileList));
				startActivity(intent);
			}
		}
	}
	
	private void handleUri(int fileType, Uri uri) {
		if (uri == null) return;
		if (uri.toString().startsWith(Const.FILE_SCHEME)) { // 文件
			int index = uri.toString().indexOf(Const.FILE_SCHEME);
			if (index >= 0) {
				String path = uri.toString().substring(index + Const.FILE_SCHEME.length());	
				addTransferFile(path, 0);
			}
		} else if (uri.toString().startsWith(Const.DATABASE_SCHEME)) { // 本地数据库
			String[] projection = null;
			switch (fileType) {
			case Const.PICTURE:
				projection = new String[] { MediaStore.Images.Media.DATA };
				break;
			case Const.VIDEO:
				projection = new String[] { 
						MediaStore.Video.Media.DATA, 
						MediaStore.Video.Media.DURATION  
				};
				break;
			case Const.MUSIC:
				projection = new String[] { 
						MediaStore.Audio.Media.DATA, 
						MediaStore.Audio.Media.DURATION
				};
				break;
			}
			if (projection != null) {
				Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
				if (cursor != null) {
					try {
						cursor.moveToNext();
						String path = cursor.getString(0);
						long duration = 0;
						if (cursor.getColumnNames().length >= 2) {
							duration = cursor.getLong(1);
						}
						addTransferFile(path, duration);
					} finally {
						cursor.close();
					}
				}
			}
		}
	}
	
	private void addTransferFile(String path, long duration) {
		if (!TextUtils.isEmpty(path)) {
			File file = new File(path);
			if (file.exists()) {
				TransferFile tf = new TransferFile();
				tf.name = file.getName();
				tf.size = Formatter.formatFileSize(this, file.length());
				tf.path = path;
				tf.duration = DateUtils.formatElapsedTime(duration);
				mTransferFileList.add(tf);
			}
		}
	}
	
}
