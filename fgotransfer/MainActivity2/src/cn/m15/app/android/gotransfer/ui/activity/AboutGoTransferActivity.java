package cn.m15.app.android.gotransfer.ui.activity;

import android.os.Bundle;
import android.widget.TextView;
import cn.m15.app.android.gotransfer.GoTransferApplication;
import cn.m15.app.android.gotransfer.R;

public class AboutGoTransferActivity extends BaseActivity2 {

	private String mVersionName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		setVersion();
	}

	private void setVersion() {
		mVersionName = GoTransferApplication.getAppVersionName(this);
		TextView versionTv = (TextView) findViewById(R.id.tv_versionname);
		versionTv.setText(getString(R.string.version, mVersionName));
	}
}
