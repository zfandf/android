package cn.m15.app.android.gotransfer.ui.fragment.dialog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowListView;
import org.robolectric.shadows.ShadowToast;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.widget.ListView;
import cn.m15.app.android.gotransfer.R;
import cn.m15.app.android.gotransfer.enity.FileInfo;
import cn.m15.app.android.gotransfer.logic.ReceivedFilesPresenter;
import cn.m15.app.android.gotransfer.utils.DialogUtil;

@RunWith(RobolectricTestRunner.class)
public class FileOptionsDialogTest {
	private FragmentActivity activity;
	private FileOptionsDialog dialog;
	private File testFile;

	@Before
	public void setUp() throws IOException {
		activity = Robolectric.buildActivity(FragmentActivity.class).create().start().resume().get();
		
		FileInfo fileInfo = new FileInfo();
		fileInfo.date = System.currentTimeMillis();
		fileInfo.name = "test.txt";
		fileInfo.path = "E:/download/test.txt";
		fileInfo.size = 1014 * 13;
		
		testFile = new File(fileInfo.path);
		if (!testFile.exists()) {
			testFile.createNewFile();
		}
		
		ReceivedFilesPresenter presenter = Mockito.mock(ReceivedFilesPresenter.class);
		
		dialog = DialogUtil.showFileOptionsDialog(activity, fileInfo, presenter);
	}
	
	@Test
	public void testUI() {
		ListView listView = (ListView) dialog.getDialog().findViewById(android.R.id.list);
		assertNotNull(listView);
		assertTrue(listView.getCount() == 3);
		
		String[] fileOptions = activity.getResources().getStringArray(R.array.file_options);
		assertEquals(fileOptions[0], listView.getAdapter().getItem(0));
		assertEquals(fileOptions[1], listView.getAdapter().getItem(1));
		assertEquals(fileOptions[2], listView.getAdapter().getItem(2));
	}
	
	@Test
	public void testOpenFile() {
		ListView listView = (ListView) dialog.getDialog().findViewById(android.R.id.list);
		ShadowListView sListView = Robolectric.shadowOf(listView);
		sListView.performItemClick(0);
		
		Intent startedIntent = Robolectric.shadowOf(activity).getNextStartedActivity();
		assertNotNull(startedIntent);
		
		testFile.delete();
		dialog.show(activity);
		sListView.performItemClick(0);
		assertEquals(activity.getString(R.string.file_not_exist), ShadowToast.getTextOfLatestToast());
	}
	
	@Test
	public void testDeleteFile() {
		ListView listView = (ListView) dialog.getDialog().findViewById(android.R.id.list);
		ShadowListView sListView = Robolectric.shadowOf(listView);
		sListView.performItemClick(1);
		
		assertFalse(testFile.exists());
	}
	
	@Test
	public void testShowFileDetailDialog() {
		ListView listView = (ListView) dialog.getDialog().findViewById(android.R.id.list);
		ShadowListView sListView = Robolectric.shadowOf(listView);
		sListView.performItemClick(2);
		
		Fragment detailDialog = activity.getSupportFragmentManager().findFragmentByTag(DialogUtil.DIALOG_FILE_DETAIL);
		assertNotNull(detailDialog);
		assertTrue(detailDialog.isAdded());
	}
}
