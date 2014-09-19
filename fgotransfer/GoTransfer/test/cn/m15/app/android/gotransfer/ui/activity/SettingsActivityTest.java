package cn.m15.app.android.gotransfer.ui.activity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.jraf.android.backport.switchwidget.Switch;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.util.ActivityController;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.TextView;
import cn.m15.app.android.gotransfer.R;
import cn.m15.app.android.gotransfer.utils.ActionBarUtil;

@RunWith(RobolectricTestRunner.class)
public class SettingsActivityTest {
	
	private ActivityController<SettingsActivity> controller;
	private SettingsActivity activity;
	private Switch share3GSwitch;
	private Switch shareHiddenFilesSwitch;
	
	private SharedPreferences preferences;

	@Before
	public void setUp() {
		controller = Robolectric.buildActivity(SettingsActivity.class);
		activity = controller.create().get();
		share3GSwitch = (Switch) activity.findViewById(R.id.switch_share_3G);
		shareHiddenFilesSwitch = (Switch) activity.findViewById(R.id.switch_show_hidden_files);
		preferences = activity.getSharedPreferences(SettingsActivity.SETTINGS, Context.MODE_PRIVATE);
	}
	
	@Test
	public void testPreconditions() {
		assertNotNull(controller);
		assertNotNull(activity);
		assertNotNull(share3GSwitch);
		assertNotNull(shareHiddenFilesSwitch);
		assertNotNull(preferences);
	}
	
	@Test
	@Ignore
	public void testActionBarTitleCorrect() {
		CharSequence title = ActionBarUtil.getTitle(activity);
		assertNotNull(title);
		assertEquals(activity.getString(R.string.settings), title);
	}
	
	@Test
	@Ignore
	public void testSettingsItemsStyleCorrect() {
		// t2 & c2
		float t2 = activity.getResources().getDimension(R.dimen.t2);
		int c2 = activity.getResources().getColor(R.color.c2);
		
		TextView share3GTv = (TextView) activity.findViewById(R.id.tv_share_3G);
		// text size
		assertTrue(t2 == share3GTv.getTextSize());
		// text color
		assertTrue(c2 == share3GTv.getTextColors().getDefaultColor());
		// text
		assertEquals(activity.getString(R.string.share_3G), share3GTv.getText().toString());
		
		TextView showHiddenFilesTv = (TextView) activity.findViewById(R.id.tv_show_hidden_files);
		// text size
		assertTrue(t2 == showHiddenFilesTv.getTextSize());
		// text color
		assertTrue(c2 == showHiddenFilesTv.getTextColors().getDefaultColor());
		// text 
		assertEquals(activity.getString(R.string.show_hidden_files), showHiddenFilesTv.getText().toString());
	}
	
	@Test
	public void testChangeShare3GState() {
		share3GSwitch.performClick();
		boolean checked = share3GSwitch.isChecked();
		assertTrue(checked == preferences.getBoolean(SettingsActivity.SHARE_3G, false));
		share3GSwitch.performClick();
		checked = share3GSwitch.isChecked();
		assertTrue(checked == preferences.getBoolean(SettingsActivity.SHARE_3G, false));
	}
	
	@Test
	public void testChangeShowHiddenFilesState() {
		shareHiddenFilesSwitch.performClick();
		boolean checked = shareHiddenFilesSwitch.isChecked();
		assertTrue(checked == preferences.getBoolean(SettingsActivity.SHOW_HIDDEN_FILES, false));
		shareHiddenFilesSwitch.performClick();
		checked = shareHiddenFilesSwitch.isChecked();
		assertTrue(checked == preferences.getBoolean(SettingsActivity.SHOW_HIDDEN_FILES, false));
	}
	
	@Test
	public void testSwitchInitStates() {
		share3GSwitch.performClick();
		shareHiddenFilesSwitch.performClick();
		
		controller.destroy();
		Intent intent = new Intent(Intent.ACTION_VIEW);
		activity = Robolectric.buildActivity(SettingsActivity.class).withIntent(intent).create().get();
		share3GSwitch = (Switch) activity.findViewById(R.id.switch_share_3G);
		shareHiddenFilesSwitch = (Switch) activity.findViewById(R.id.switch_show_hidden_files);
		preferences = activity.getSharedPreferences(SettingsActivity.SETTINGS, Context.MODE_PRIVATE);
		
		boolean checked = share3GSwitch.isChecked();
		assertTrue(checked == preferences.getBoolean(SettingsActivity.SHARE_3G, false));
		checked = shareHiddenFilesSwitch.isChecked();
		assertTrue(checked == preferences.getBoolean(SettingsActivity.SHOW_HIDDEN_FILES, false));
	}
}
