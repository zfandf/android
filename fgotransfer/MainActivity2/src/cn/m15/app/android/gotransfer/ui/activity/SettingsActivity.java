package cn.m15.app.android.gotransfer.ui.activity;

import org.jraf.android.backport.switchwidget.Switch;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import cn.m15.app.android.gotransfer.R;

public class SettingsActivity extends BaseActivity2 implements
		CompoundButton.OnCheckedChangeListener {
	public static final String SETTINGS = "gotransfer_settings";
	public static final String SHARE_3G = "gotransfer_settings_share_3G";
	public static final String SHOW_HIDDEN_FILES = "gotransfer_settings_show_hidden_files";

	private SharedPreferences mPerferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		// setCustomTitle(R.string.settings);

		Switch share3GSwitch = (Switch) findViewById(R.id.switch_share_3G);
		share3GSwitch.setOnCheckedChangeListener(this);
		Switch showHiddenFilesSwitch = (Switch) findViewById(R.id.switch_show_hidden_files);
		showHiddenFilesSwitch.setOnCheckedChangeListener(this);

		mPerferences = getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
		boolean share3G = mPerferences.getBoolean(SHARE_3G, false);
		share3GSwitch.setChecked(share3G);
		boolean showHiddenFiles = mPerferences.getBoolean(SHOW_HIDDEN_FILES,
				false);
		showHiddenFilesSwitch.setChecked(showHiddenFiles);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView.getId() == R.id.switch_share_3G) {
			mPerferences.edit().putBoolean(SHARE_3G, isChecked).apply();
		} else {
			mPerferences.edit().putBoolean(SHOW_HIDDEN_FILES, isChecked)
					.apply();
		}
	}
}
