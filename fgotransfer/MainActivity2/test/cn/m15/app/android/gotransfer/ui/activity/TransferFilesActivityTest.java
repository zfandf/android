package cn.m15.app.android.gotransfer.ui.activity;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.widget.RadioGroup;
import cn.m15.app.android.gotransfer.Const;
import cn.m15.app.android.gotransfer.R;
import cn.m15.app.android.gotransfer.enity.TransferFile;
import cn.m15.app.android.gotransfer.net.ipmsg2.User;
import cn.m15.app.android.gotransfer.utils.DialogUtil;

@RunWith(RobolectricTestRunner.class)
public class TransferFilesActivityTest {

	private TransferFileActivity activity;
	private Fragment[] mFragments;
	private RadioGroup mRadioGroup;
	
	@Before
	public void setUp() throws IOException {
		Intent intent = new Intent(Robolectric.application, TransferFileActivity.class); 
		
		ArrayList<User> users = new ArrayList<User>();
		User user = new User("test test", "test", "test", "127.0.0.1", "localhost", "abcdefghijk");
		users.add(user);
		
		ArrayList<TransferFile> transferFiles = new ArrayList<TransferFile>();
		TransferFile afile = new TransferFile();
		afile.name = "test.txt";
		afile.path = "E:/download/test.txt";
		afile.size = 13 * 1024;
		afile.fileType = Const.LOADER_FILES;
		transferFiles.add(afile);
		
		File testFile = new File(afile.path);
		if (!testFile.exists()) {
			testFile.createNewFile();
		}
		
		HashMap<User, String> packetNos = new HashMap<User, String>();
		
		intent.putExtra(Const.INTENT_EXTRA_USER_LIST, users);
		intent.putExtra(Const.INTENT_EXTRA_FILE_LIST, transferFiles);
		intent.putExtra(Const.INTENT_EXTRA_PACKETNO_LIST, packetNos);
		intent.putExtra(Const.INTENT_EXTRA_TRANSFER_TYPE, Const.INTENT_EXTRA_TRANSFER_TYPE_SEND);
		activity = Robolectric.buildActivity(TransferFileActivity.class).withIntent(intent).create().get();
		
		FragmentManager fm = activity.getSupportFragmentManager();
		mFragments = new Fragment[2];
		mFragments[0] = fm.findFragmentById(R.id.fragment_tranfering);
		mFragments[1] = fm.findFragmentById(R.id.fragment_completed);
		
		mRadioGroup = (RadioGroup) activity.findViewById(R.id.rg_tab);
	}
	
	@Test
	@Ignore
	public void testRadioGroupCheckChange() {
		mRadioGroup.check(R.id.rb_transfering);
		assertTrue(mFragments[0].isHidden() == false);
		assertTrue(mFragments[1].isHidden() == true);
		
		mRadioGroup.check(R.id.rb_completed);
		assertTrue(mFragments[0].isHidden() == true);
		assertTrue(mFragments[1].isHidden() == false);
	}
	
	@Test
	@Ignore
	public void testBack() {
		activity.onBackPressed();
		
		assertNotNull(activity.getSupportFragmentManager().findFragmentByTag(DialogUtil.DIALOG_DISCONNECT_ALL_TRANSFERS));
	}
}
