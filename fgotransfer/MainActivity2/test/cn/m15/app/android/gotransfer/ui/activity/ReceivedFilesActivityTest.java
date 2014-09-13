package cn.m15.app.android.gotransfer.ui.activity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowEnvironment;
import org.robolectric.util.ActivityController;

import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.m15.app.android.gotransfer.Const;
import cn.m15.app.android.gotransfer.R;
import cn.m15.app.android.gotransfer.ui.activity.ReceivedFilesActivity.FileTypeAdapter;
import cn.m15.app.android.gotransfer.ui.fragment.ReceivedFilesFragment;
import cn.m15.app.android.gotransfer.ui.widget.PagerSlidingTabStrip;
import cn.m15.app.android.gotransfer.utils.ActionBarUtil;
import cn.m15.app.android.gotransfer.utils.DialogUtil;

@RunWith(RobolectricTestRunner.class)
public class ReceivedFilesActivityTest {
	
	private ActivityController<ReceivedFilesActivity> controller;
	private ReceivedFilesActivity activity;

	@Before
	public void setUp() {
		controller = Robolectric.buildActivity(ReceivedFilesActivity.class);
		activity = controller.create().get();
	}
	
	@Test
	public void testPreconditions() {
		assertNotNull(activity);
	}
	
	@Test
	public void testActionBarTitleCorrect() {
		CharSequence title = ActionBarUtil.getTitle(activity);
		assertNotNull(title);
		assertEquals(activity.getString(R.string.receive_history_files), title);
	}
	
	@Test
	public void testSDCardDisable() {
		ShadowEnvironment.setExternalStorageState(Environment.MEDIA_UNMOUNTED);
		
		controller.destroy();
		Intent intent = new Intent(Intent.ACTION_VIEW);
		activity = Robolectric.buildActivity(ReceivedFilesActivity.class).withIntent(intent).create().get();
		
		Fragment dialog = activity.getSupportFragmentManager()
				.findFragmentByTag(DialogUtil.DIALOG_CANNOT_CHOOSER_FILES);
		assertNotNull(dialog);
		assertTrue(dialog.isAdded());
	}
	
	@Test 
	public void testSDCardEnable() {
		ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);
		
		controller.destroy();
		Intent intent = new Intent(Intent.ACTION_VIEW);
		activity = Robolectric.buildActivity(ReceivedFilesActivity.class).withIntent(intent).create().get();
		
		
		String[] titles = activity.getResources().getStringArray(R.array.tab_file_type);
		PagerSlidingTabStrip tab = (PagerSlidingTabStrip) activity.findViewById(R.id.tab);
		
		LinearLayout ll = (LinearLayout) tab.getChildAt(0);
		if (Const.SHARE_APK) {
			assertTrue(ll.getChildCount() == 5);	
		} else {
			assertTrue(ll.getChildCount() == 4);
		}
		assertEquals(titles[0], ((TextView) ll.getChildAt(0)).getText());
		assertEquals(titles[1], ((TextView) ll.getChildAt(1)).getText());
		assertEquals(titles[2], ((TextView) ll.getChildAt(2)).getText());
		assertEquals(titles[3], ((TextView) ll.getChildAt(3)).getText());
	}
	
	@Test
	public void testFileTypeAdapter() {
		FileTypeAdapter adapter = new FileTypeAdapter(activity);
		
		if (Const.SHARE_APK) {
			assertTrue(adapter.getCount() == 5);	
		} else {
			assertTrue(adapter.getCount() == 4);
		}
		
		assertTrue(adapter.getItem(0) instanceof ReceivedFilesFragment);
		assertTrue(adapter.getItem(1) instanceof ReceivedFilesFragment);
		assertTrue(adapter.getItem(2) instanceof ReceivedFilesFragment);
		assertTrue(adapter.getItem(3) instanceof ReceivedFilesFragment);
		
		String[] titles = activity.getResources().getStringArray(R.array.tab_file_type);
		assertEquals(titles[0], adapter.getPageTitle(0));
		assertEquals(titles[1], adapter.getPageTitle(1));
		assertEquals(titles[2], adapter.getPageTitle(2));
		assertEquals(titles[3], adapter.getPageTitle(3));
	}
	
}
