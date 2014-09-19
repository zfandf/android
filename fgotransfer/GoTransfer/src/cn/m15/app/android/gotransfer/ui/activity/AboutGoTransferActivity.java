package cn.m15.app.android.gotransfer.ui.activity;

import com.umeng.update.UmengUpdateAgent;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import cn.m15.app.android.gotransfer.GoTransferApplication;
import cn.m15.app.android.gotransfer.R;

public class AboutGoTransferActivity extends BaseActivity {

	private String mVersionName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		setTitle(R.string.about);
		setVersion();

		findViewById(R.id.tv_rate).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Uri uri = Uri.parse("market://details?id=" + getPackageName());
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);

			}
		});

		findViewById(R.id.tv_check_version).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
					    UmengUpdateAgent.forceUpdate(AboutGoTransferActivity.this);
						Toast.makeText(
								AboutGoTransferActivity.this,
								getResources().getString(R.string.checkversion),
								Toast.LENGTH_SHORT).show();
					}
				});
	}

	private void setVersion() {
		mVersionName = GoTransferApplication.getAppVersionName(this);
		TextView versionTv = (TextView) findViewById(R.id.tv_versionname);
		versionTv.setText(getString(R.string.version, mVersionName));
	}
}
