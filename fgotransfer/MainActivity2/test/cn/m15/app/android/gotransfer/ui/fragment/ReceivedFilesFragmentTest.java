package cn.m15.app.android.gotransfer.ui.fragment;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowListView;
import org.robolectric.util.FragmentTestUtil;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.widget.ListView;
import cn.m15.app.android.gotransfer.Const;
import cn.m15.app.android.gotransfer.R;
import cn.m15.app.android.gotransfer.enity.FileInfo;
import cn.m15.app.android.gotransfer.ui.fragment.ReceivedFilesFragment.ReceivedFileItem;
import cn.m15.app.android.gotransfer.utils.DialogUtil;
import cn.m15.app.android.gotransfer.utils.images.LocalImageFetcher;
import cn.m15.app.android.gotransfer.utils.images.VedioImageFetcher;

@RunWith(RobolectricTestRunner.class)
public class ReceivedFilesFragmentTest {

	private ReceivedFilesFragment fragment;
	
	@Before
	public void setUp() {
		fragment = new ReceivedFilesFragment();
		fragment.setFileType(Const.LOADER_FILES);
		fragment.setLocalImageFetcher(Mockito.mock(LocalImageFetcher.class));
		fragment.setVedioImageFetcher(Mockito.mock(VedioImageFetcher.class));
		
		FragmentTestUtil.startFragment(fragment);
		
		assertNotNull(fragment.getView());
		ArrayList<ReceivedFileItem> data = new ArrayList<ReceivedFileItem>();
		fragment.hideProgressBar();
		fragment.setData(data);
	}
	
	@Test
	public void testAll() throws IOException {
		// test list UI
		ListView listView = (ListView) fragment.getView().findViewById(R.id.lv_received_files);
		assertTrue(listView.getCount() == 0);
		
		ReceivedFileItem item1 = new ReceivedFileItem(ReceivedFileItem.SECTION, "2013-09-01", null);
		
		FileInfo fileInfo = new FileInfo();
		fileInfo.type = Const.LOADER_FILES;
		fileInfo.date = System.currentTimeMillis();
		fileInfo.name = "test.txt";
		fileInfo.path = "E:/download/test.txt";
		fileInfo.size = 1014 * 13;
		
		File testFile = new File(fileInfo.path);
		if (!testFile.exists()) {
			testFile.createNewFile();
		}
		
		ArrayList<ReceivedFileItem> data = new ArrayList<ReceivedFileItem>();
		ReceivedFileItem item2 = new ReceivedFileItem(ReceivedFileItem.ITEM, null, fileInfo);
		data.add(item1);
		data.add(item2);
		fragment.setData(data);
		
		assertTrue(listView.getCount() == 2);
		
		// test item click
		ShadowListView sListView = Robolectric.shadowOf(listView);
		sListView.performItemClick(0);
		Fragment optionsDialog = fragment.getActivity().
				getSupportFragmentManager().findFragmentByTag(DialogUtil.DIALOG_FILE_OPTIONS);
		assertNull(optionsDialog);
		
		sListView.performItemClick(1);
		Intent startedIntent = Robolectric.shadowOf(fragment.getActivity()).getNextStartedActivity();
		assertNotNull(startedIntent);
		
		// test item long click
		// don't know.
	}
}
