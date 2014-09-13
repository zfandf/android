package cn.m15.app.android.gotransfer.ui.fragment;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.util.FragmentTestUtil;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import cn.m15.app.android.gotransfer.Const;
import cn.m15.app.android.gotransfer.R;
import cn.m15.app.android.gotransfer.enity.TransferFile;
import cn.m15.app.android.gotransfer.net.ipmsg2.User;
import cn.m15.app.android.gotransfer.ui.widget.ExpandableHeightListView;

@RunWith(RobolectricTestRunner.class)
public class CompletedFragmentTest {
	
	private CompletedFragment fragment;
	private ArrayList<User> users;
	private User user;
	private ArrayList<TransferFile> transferFiles;
	private String packetNo;
	
	@Before
	public void setUp() throws IOException {
		fragment = new CompletedFragment();
		FragmentTestUtil.startFragment(fragment);
		
		users = new ArrayList<User>();
		user = new User("test test", "test", "test", "127.0.0.1", "localhost", "abcdefghijk");
		users.add(user);
		
		transferFiles = new ArrayList<TransferFile>();
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
		
		packetNo = "dasdsadsadasdsadsad";
	}
	
	@Test
	@Ignore
	@Deprecated
	public void testBroadCastReceiverRegistered() throws NameNotFoundException {
		List<ShadowApplication.Wrapper> registeredReceivers = 
				Robolectric.getShadowApplication().getRegisteredReceivers();
		
		assertFalse(registeredReceivers.isEmpty());
		boolean receiverFound = false;
		for (ShadowApplication.Wrapper wrapper : registeredReceivers) {
	    	if (!receiverFound) {
	    		receiverFound = CompletedFragment.TransferEndReceiver.class.getSimpleName().equals(
	                                         wrapper.broadcastReceiver.getClass().getSimpleName());
			}
		}
		assertTrue(receiverFound); 
	}
	
	@Test
	public void test_broadcast_action_transfer_finish() {
		Intent intent = new Intent();
		Bundle args = new Bundle();
		intent.setAction(Const.BROADCAST_ACTION_TRANSFER_FINISH);
		args.putParcelableArrayList("files", transferFiles);
		args.putParcelable("user", user);
		args.putString("packetNo", packetNo);
		args.putBoolean("is_transfer", true);
		intent.putExtras(args);
		
		fragment.mTransferEndReceiver.onReceive(Robolectric.application, intent);
		
		View finishView = fragment.mViewMap.get(packetNo);
		assertNotNull(finishView);
		
		// 取消按钮
		Button btnCancel = (Button) finishView.findViewById(R.id.btn_cancel);
		assertTrue(btnCancel.getVisibility() != View.VISIBLE);
		
		// 接收人名称
		TextView tvUsername = (TextView) finishView.findViewById(R.id.tv_username);
		assertEquals(user.getAlias(), tvUsername.getText().toString());
		
		// 发送文件列表
		ListView lvFiles = (ExpandableHeightListView) finishView.findViewById(R.id.elv_file_list);
		assertTrue(lvFiles.getCount() == transferFiles.size());
		
	}
	
}
