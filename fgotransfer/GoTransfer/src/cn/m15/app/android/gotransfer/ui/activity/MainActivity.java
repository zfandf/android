package cn.m15.app.android.gotransfer.ui.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cn.m15.app.android.gotransfer.R;
import cn.m15.app.android.gotransfer.ui.fragment.AppsChooserFragment;
import cn.m15.app.android.gotransfer.ui.fragment.FileChooserFragment;
import cn.m15.app.android.gotransfer.ui.fragment.MediaFileChooseFragment;
import cn.m15.app.android.gotransfer.ui.fragment.PictureChooserFragment;
import cn.m15.app.android.gotransfer.ui.fragment.dialog.ChangeNameDialog;
import cn.m15.app.android.gotransfer.ui.fragment.dialog.CommonDialogFragment;
import cn.m15.app.android.gotransfer.ui.widget.FragmentPagerAdapter;
import cn.m15.app.android.gotransfer.ui.widget.HorizontalListView;
import cn.m15.app.android.gotransfer.ui.widget.PagerSlidingTabStrip;
import cn.m15.app.android.gotransfer.utils.DialogUtil;
import cn.m15.app.android.gotransfer.utils.ImageUtil;
import cn.m15.gotransfer.sdk.ConfigManager;
import cn.m15.gotransfer.sdk.SdkConst;
import cn.m15.gotransfer.sdk.entity.TransferFileManager;
import cn.m15.gotransfer.sdk.net.ipmsg.TransferManager;
import cn.m15.gotransfer.sdk.net.ipmsg.UdpThreadManager;
import cn.m15.gotransfer.sdk.net.ipmsg.UdpThreadManager.ConnectedUserChangedListener;
import cn.m15.gotransfer.sdk.net.ipmsg.TUser;
import cn.m15.gotransfer.sdk.net.wifi.WifiApConnector;
import cn.m15.gotransfer.sdk.net.wifi.WifiApConnector.ConnectApStatusListener;
import cn.m15.gotransfer.sdk.net.wifi.WifiApConnector.ConnectWifiListener;
import cn.m15.gotransfer.sdk.net.wifi.WifiApConnector.CreateWifiApResultListener;

import com.umeng.fb.FeedbackAgent;
import com.umeng.update.UmengUpdateAgent;

public class MainActivity extends BaseActivity implements View.OnClickListener,
		AdapterView.OnItemClickListener, CommonDialogFragment.DialogButtonClickListener,
		CreateWifiApResultListener, ConnectedUserChangedListener, ConnectApStatusListener,
		ConnectWifiListener {

	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private IconArrayAdapter mNavigationAdapter;

	private PagerSlidingTabStrip mTab;
	private ViewPager mViewPager;
	private FileTypeAdapter mFileTypeAdapter;

	private View mConnectFriendsView;
	private View mFileOperationsView;
	private View mConnectionView;

	private Button mHotSellingBtn;
	private Button mInviteFriendBtn;

	private Button mSendBtn;
	private Button mCloseBtn;
	private Button mAbolishBtn;
	private TransferFileManager mManager;

	private HorizontalListView mListView;
	private boolean[] mChecked;
	private UserAdapter mUserAdapter;
	private ArrayList<TUser> mUserList;

	private TextView mDiviceNameTv;

	private Timer mTimer;

	private SDCardMountReceiver mSDCardMountReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		WifiApConnector.deleteUselessMyWifis(this);
		setContentView(R.layout.activity_main2);
		// umeng
		FeedbackAgent agent = new FeedbackAgent(this);
		agent.sync();

	    UmengUpdateAgent.update(this);


		mManager = TransferFileManager.getInstance();

		// initialize navigation drawer
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.transparent,
				R.string.app_name, R.string.app_name);
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		initDiviceName();

		// 更改名称按钮
		mDiviceNameTv = (TextView) findViewById(R.id.tv_name);
		String[] navigationNames = getResources().getStringArray(R.array.menu);
		ListView navigationListView = (ListView) findViewById(R.id.lv_navigation);
		mNavigationAdapter = new IconArrayAdapter(this, R.layout.item_navigation,
				R.id.tv_navigation, navigationNames);
		navigationListView.setAdapter(mNavigationAdapter);
		navigationListView.setOnItemClickListener(this);

		// initialize content view
		mViewPager = (ViewPager) findViewById(R.id.vp_content);
		mViewPager.setOffscreenPageLimit(4);
		mFileTypeAdapter = new FileTypeAdapter(this);
		mViewPager.setAdapter(mFileTypeAdapter);
		mTab = (PagerSlidingTabStrip) findViewById(R.id.tab);
		mTab.setViewPager(mViewPager);

		// initialize buttons click event
		mHotSellingBtn = (Button) findViewById(R.id.btn_hot_selling);
		mHotSellingBtn.setOnClickListener(this);
		mInviteFriendBtn = (Button) findViewById(R.id.btn_invite_friends);
		mInviteFriendBtn.setOnClickListener(this);
		mConnectFriendsView = findViewById(R.id.tv_connect_friends);
		mConnectFriendsView.setOnClickListener(this);

		UdpThreadManager.getInstance().addConnectedUserChangedListener(this);
		mUserList = new ArrayList<TUser>();

		mSDCardMountReceiver = new SDCardMountReceiver();
		registerSDCardMountReceiver();
	}

	public void initDiviceName() {
		mDiviceNameTv = (TextView) findViewById(R.id.tv_name);
		mDiviceNameTv.setText(ConfigManager.getInstance().getSelfName());
		mDiviceNameTv.setOnClickListener(this);
		findViewById(R.id.iv_name_editor).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_hot_selling:
			Toast.makeText(this, "热榜", Toast.LENGTH_SHORT).show();
			break;
		case R.id.btn_invite_friends:
			startActivity(new Intent(this, InviteFriendsActivity.class));
			break;
		case R.id.tv_connect_friends:
			startActivity(new Intent(this, ConnectFriendsActivity.class));
			break;
		case R.id.btn_cancel:
			if (mManager.size() > 10) {
				DialogUtil.showClearSelectedFilesDialog(this);
			} else {
				clearTransferFiles();
			}
			break;
		case R.id.btn_send:
			if (mApConnector.getCreateStatus() == WifiApConnector.CREATE_WIFI_AP_SUCCESS
					|| mApConnector.getConnectStatus() == WifiApConnector.CONNECT_AP_CONNECTED) {
				ArrayList<TUser> selectedUsers = new ArrayList<TUser>();
				if (mChecked != null) {
					int length = mChecked.length;
					for (int i = 0; i < length; i++) {
						if (mChecked[i]) {
							selectedUsers.add(mUserList.get(i));
						}
					}
					if (selectedUsers.size() > 0) {
						TransferManager manager = TransferManager.getInstance();
						manager.startTransferFiles(this, selectedUsers, TransferFileManager
								.getInstance().toList());

						clearTransferFiles();
					} else {
						Toast.makeText(this, R.string.select_user_hint, Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(this, R.string.wait_friend_join, Toast.LENGTH_SHORT).show();
				}
			} else {
				Intent intent = new Intent(this, CreateConnectionActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra("is_from_send_btn", true);
				intent.putParcelableArrayListExtra("transfer_file_list", TransferFileManager
						.getInstance().toList());
				startActivity(intent);
				clearTransferFiles();
			}
			break;
		case R.id.btn_break:
		case R.id.btn_abolish:
			DialogUtil.showCloseConnectionDialog(this);
			break;
		case R.id.tv_name:
		case R.id.iv_name_editor:
			DialogUtil.showChangeNameDialog(this);
			break;
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		boolean isClose = intent.getBooleanExtra("isCloseConnection", false);
		if (isClose) {
			hideConnectionView();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		switch (position) {
		case 0: // connect iphone
			startActivity(new Intent(this, ConnectAppleActivity.class));
			break;
		case 1: // connect pc
			startActivity(new Intent(this, ConnectPcActivity.class));
			break;
		case 2: // feedback
			gotoFeedback();
			break;
		case 3: // settings
			startActivity(new Intent(this, SettingsActivity.class));
			break;
		case 4: // about
			startActivity(new Intent(this, AboutGoTransferActivity.class));
			break;
		}
	}

	private void gotoFeedback() {
		Intent intent = new Intent(MainActivity.this, UmConversationActivity.class);
		startActivity(intent);
	}

	public void notifyTransferFilesChanged() {
		int transferFilesNum = mManager.size();
		if (transferFilesNum > 0) {
			showFileOperationsView();
		} else {
			hideFileOperationsView();
		}
	}

	private void clearTransferFiles() {
		mManager.clear();
		if (mFileTypeAdapter != null && SdkConst.SHARE_APK) {
			Fragment fragment = getSupportFragmentManager().findFragmentByTag(
					mFileTypeAdapter.getFragmentTag(0));
			((AppsChooserFragment) fragment).onTransferFilesCancelled();
		}
		if (mFileTypeAdapter != null) {
			int position = 0;
			if (SdkConst.SHARE_APK)
				position = 1;
			Fragment fragment = getSupportFragmentManager().findFragmentByTag(
					mFileTypeAdapter.getFragmentTag(position));
			((PictureChooserFragment) fragment).onTransferFilesCancelled();
		}
		if (mFileTypeAdapter != null) {
			int position = 1;
			if (SdkConst.SHARE_APK)
				position = 2;
			Fragment fragment = getSupportFragmentManager().findFragmentByTag(
					mFileTypeAdapter.getFragmentTag(position));
			((MediaFileChooseFragment) fragment).onTransferFilesCancelled();
		}
		if (mFileTypeAdapter != null) {
			int position = 2;
			if (SdkConst.SHARE_APK)
				position = 3;
			Fragment fragment = getSupportFragmentManager().findFragmentByTag(
					mFileTypeAdapter.getFragmentTag(position));
			((FileChooserFragment) fragment).onTransferFilesCancelled();
		}

		if (mSendBtn != null) {
			mSendBtn.setText(getString(R.string.send_with_number, 0));
		}
		hideFileOperationsView();
	}

	public void showFileOperationsView() {
		if (mFileOperationsView == null) {
			ViewStub fileOperationsVs = (ViewStub) findViewById(R.id.vs_file_operations);
			mFileOperationsView = fileOperationsVs.inflate();
			mFileOperationsView.findViewById(R.id.btn_cancel).setOnClickListener(this);

			mSendBtn = (Button) mFileOperationsView.findViewById(R.id.btn_send);
			mSendBtn.setOnClickListener(this);
		}
		mSendBtn.setText(getString(R.string.send_with_number, mManager.size()));
		mFileOperationsView.setVisibility(View.VISIBLE);
	}

	public void hideFileOperationsView() {
		if (mFileOperationsView != null && mFileOperationsView.getVisibility() == View.VISIBLE) {
			mFileOperationsView.setVisibility(View.GONE);
		}
	}

	public void showConnectionView() {
		mConnectFriendsView.setVisibility(View.GONE);
		if (mConnectionView == null) {
			ViewStub fileOperationsVs = (ViewStub) findViewById(R.id.vs_connection);
			mConnectionView = fileOperationsVs.inflate();
		}
		mHotSellingBtn.setVisibility(View.GONE);
		mInviteFriendBtn.setVisibility(View.GONE);

		mConnectionView.setVisibility(View.VISIBLE);
		mCloseBtn = (Button) mConnectionView.findViewById(R.id.btn_break);
		mAbolishBtn = (Button) mConnectionView.findViewById(R.id.btn_abolish);
		mCloseBtn.setOnClickListener(this);
		mAbolishBtn.setOnClickListener(this);
		mListView = (HorizontalListView) findViewById(R.id.hlv_receivers);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (mChecked == null)
					return;
				ImageView ivUserPic = (ImageView) view.findViewById(R.id.iv_user);
				mChecked[position] = !mChecked[position];

				if (mChecked[position]) {
					ivUserPic.setImageResource(R.drawable.favicon50);
				} else {
					Bitmap bitmap = ImageUtil.grayScaleImage(BitmapFactory.decodeResource(
							getResources(), R.drawable.favicon50));
					ivUserPic.setImageBitmap(bitmap);
				}

			}
		});
	}

	public void hideConnectionView() {
		if (mConnectionView != null && mConnectionView.getVisibility() == View.VISIBLE) {
			TextView tvStatus = (TextView) mConnectionView.findViewById(R.id.tv_connection_status);
			tvStatus.setVisibility(View.GONE);
			mConnectFriendsView.setVisibility(View.VISIBLE);
			mConnectionView.setVisibility(View.GONE);
			mHotSellingBtn.setVisibility(View.VISIBLE);
			mInviteFriendBtn.setVisibility(View.VISIBLE);
			mConnectFriendsView.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onDestroy() {
		UdpThreadManager.getInstance().removeConnectedUserChangedListener(this);
		mManager.clear();
		unregisterReceiver(mSDCardMountReceiver);
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		switch (item.getItemId()) {
		case R.id.menu:
			Intent intent = new Intent(this, ConversationActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;
		}
		return true;
	}

	@Override
	public void onDialogButtonClick(CommonDialogFragment dialog, int which, String tag) {
		dialog.dismiss();
		if (DialogUtil.DIALOG_CHANGE_NAME.equals(tag) && which == DialogInterface.BUTTON_POSITIVE) {
			String modifiedName = ((ChangeNameDialog) dialog).getEditText().trim();
			ConfigManager.getInstance().setSelfName(modifiedName);
			initDiviceName();
		} else if (DialogUtil.DIALOG_CLOSE_CONNECTION.equals(tag)
				&& which == DialogInterface.BUTTON_POSITIVE) {
			closeWifiConnection();
			hideConnectionView();
		} else if (DialogUtil.DIALOG_CLEAR_SELECTED_FILES.equals(tag)
				&& which == DialogInterface.BUTTON_POSITIVE) {
			clearTransferFiles();
		} else if (DialogUtil.DIALOG_QUIT_APP.equals(tag)
				&& which == DialogInterface.BUTTON_POSITIVE) {
			finish();
		} else if (DialogUtil.DIALOG_WAIT_JOIN_TIME_TOO_LONG.equals(tag)
				&& which == DialogInterface.BUTTON_POSITIVE) {
			disconnect();
		}
	}

	public void onWaitTimeout() {
		if (mTimer == null) {
			mTimer = new Timer();
		}
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				if ((mUserList == null || mUserList.size() == 0) && mApConnector.isCreatedAp()) {
					DialogUtil.showWaitJoinTimeTooLongDialog(MainActivity.this);
				}
			}
		};
		mTimer.schedule(task, WifiApConnector.WAIT_CONNECT_TIMEOUT);
	}

	private void disconnect() {
		mTimer.cancel();
		mTimer.purge();
		mTimer = null;
		closeWifiConnection();
		hideConnectionView();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	public static class FileTypeAdapter extends FragmentPagerAdapter {
		private String[] titles;

		public FileTypeAdapter(FragmentActivity activity) {
			super(activity.getSupportFragmentManager());
			titles = activity.getResources().getStringArray(R.array.tab_file_type);
			if (!SdkConst.SHARE_APK) {
				titles = Arrays.copyOfRange(titles, 1, titles.length);
			}
		}

		@Override
		public Fragment getItem(int position) {
			if (SdkConst.SHARE_APK) {
				switch (position) {
				case 0: // application
					return new AppsChooserFragment();
				case 1: // picture
					return new PictureChooserFragment();
				case 2: // video & audio
					return new MediaFileChooseFragment();
				default: // all files
					return new FileChooserFragment();
				}
			} else {
				switch (position) {
				case 0: // picture
					return new PictureChooserFragment();
				case 1: // video & audio
					return new MediaFileChooseFragment();
				default: // all files
					return new FileChooserFragment();
				}
			}
		}

		@Override
		public int getCount() {
			return titles.length;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return titles[position];
		}

		public String getFragmentTag(int position) {
			return makeFragmentName(getItemId(position));
		}

	}

	private static class IconArrayAdapter extends ArrayAdapter<String> {
		private int[] iconIds;

		public IconArrayAdapter(Context context, int resource, int textViewResourceId,
				String[] objects) {
			super(context, resource, textViewResourceId, objects);

			TypedArray ta = context.getResources().obtainTypedArray(R.array.nav_icons);
			int size = ta.length();
			iconIds = new int[size];
			for (int i = 0; i < size; i++) {
				iconIds[i] = ta.getResourceId(i, 0);
			}
			ta.recycle();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);
			((TextView) view).setCompoundDrawablesWithIntrinsicBounds(iconIds[position], 0, 0, 0);
			return view;
		}
	}

	@Override
	public void createWifiApStarted() {
		if (mUserAdapter != null) {
			mUserAdapter.clear();
		}
		showConnectionView();
		TextView tvStatus = (TextView) findViewById(R.id.tv_connection_status);
		tvStatus.setVisibility(View.VISIBLE);
		tvStatus.setText(R.string.creating_wifi);
		mAbolishBtn.setVisibility(View.VISIBLE);
		mCloseBtn.setVisibility(View.GONE);
	}

	@Override
	protected void onResume() {
		if (mApConnector != null) {
			Log.d("MainActivity", mApConnector.getCreateStatus()+", "+mApConnector.getConnectStatus());
			if (mApConnector.getCreateStatus() == WifiApConnector.CREATE_WIFI_AP_STARTED) {
				createWifiApStarted();
			} else if (mApConnector.getCreateStatus() == WifiApConnector.CREATE_WIFI_AP_SUCCESS) {
				createWifiApSuccess();
				Map<String, TUser> userMap = UdpThreadManager.getInstance().getUsers();
				if (userMap != null && userMap.size() > 0) {
					connectedUserChanged(userMap);
				}
			} else if (mApConnector.getCreateStatus() == WifiApConnector.CREATE_WIFI_AP_FAILED) {
				createWifiApFailed();
			} else {
//				不能使用mApConnector.isConnectedAp("")去判断是否已连接
				if (mApConnector.getConnectStatus() == WifiApConnector.CONNECT_AP_CONNECTED) {
					showConnectionView();
					Map<String, TUser> userMap = UdpThreadManager.getInstance().getUsers();
					if (userMap != null && userMap.size() > 0) {
						connectedUserChanged(userMap);
					}
				} else {
					closeWifiConnection();
					hideConnectionView();
				}
			}
		}
		mApConnector.addCreateWifiApResultListener(this);
		mApConnector.addConnectApStatusListener(this);
		mApConnector.addConnectWifiListener(this);
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		mApConnector.removeCreateWifiApResultListener(this);
		mApConnector.removeConnectApStatusListener(this);
		mApConnector.removeConnectWifiListener(this);
		super.onPause();
	}

	@Override
	public void createWifiApSuccess() {
		if (mUserAdapter != null) {
			mUserAdapter.clear();
		}
		showConnectionView();
		TextView tvStatus = (TextView) findViewById(R.id.tv_connection_status);
		tvStatus.setVisibility(View.VISIBLE);
		tvStatus.setText(R.string.waiting_join);
		UdpThreadManager.getInstance().connectSocket();
		if (mApConnector.mWaitConnectTimeoutNum == 0) {
			onWaitTimeout();			
		}
		mAbolishBtn.setVisibility(View.GONE);
		mCloseBtn.setVisibility(View.VISIBLE);
	}

	@Override
	public void createWifiApFailed() {
		hideConnectionView();
		Toast.makeText(this, R.string.create_wifi_failed, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void connectedUserChanged(Map<String, TUser> user) {
		if (mUserAdapter != null) {
			mChecked = null;
			mUserAdapter.clear();
		}
		if (user.size() > 0) {
			showConnectionView();
			TextView tvStatus = (TextView) findViewById(R.id.tv_connection_status);
			tvStatus.setVisibility(View.GONE);
			mUserList.addAll(user.values());
			mChecked = new boolean[user.size()];
			Arrays.fill(mChecked, true);
			mUserAdapter = new UserAdapter(this, R.layout.item_select_user2, mUserList);
			mListView.setAdapter(mUserAdapter);
		} else if (user.size() == 0) {
			mChecked = null;
			if (mApConnector.isCreatedAp()) { // 创建者显示等待加入
				TextView tvStatus = (TextView) findViewById(R.id.tv_connection_status);
				tvStatus.setVisibility(View.VISIBLE);
				tvStatus.setText(R.string.waiting_join);
			} else { // 没有连接、没有创建、连接者获取的用户为空
				wifiDisConnected();
			}
		}

	}

	@Override
	public void connectApStarted() {
	}

	@Override
	public void connectApSuccess() {
		showConnectionView();
		UdpThreadManager.getInstance().connectSocket();
	}

	@Override
	public void onBackPressed() {
		DialogUtil.showQuitAppDialog(this);
	}

	private class UserAdapter extends ArrayAdapter<TUser> {
		private LayoutInflater mInflater;

		public UserAdapter(Context context, int resource, ArrayList<TUser> userList) {
			super(context, R.layout.item_select_user2, userList);
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item_select_user2, parent, false);
				holder = new ViewHolder();
				holder.mIvUserPic = (ImageView) convertView.findViewById(R.id.iv_user);
				holder.mTvUserName = (TextView) convertView.findViewById(R.id.tv_wifi_spot_name);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			TUser user = getItem(position);
			holder.mTvUserName.setText(user.getAlias());
			if (mChecked[position]) {
				holder.mIvUserPic.setImageResource(R.drawable.favicon50);
			} else {
				Bitmap bitmap = ImageUtil.grayScaleImage(BitmapFactory.decodeResource(
						getResources(), R.drawable.favicon50));
				holder.mIvUserPic.setImageBitmap(bitmap);
			}
			return convertView;
		}
	}

	public static class ViewHolder {
		ImageView mIvUserPic;
		TextView mTvUserName;
	}

	public interface TransferFilesChangeListener {

		// 传输文件数量改变
		public void onTransferFilesChangedListener();

		// 取消传输文件
		public void onTransferFilesCancelled();

	}

	@Override
	public void wifiDisConnected() {
		DialogUtil.showNetworkDisconnectedDialog(this);
		closeWifiConnection();
		hideConnectionView();
	}

	public class SDCardMountReceiver extends BroadcastReceiver {
		private Handler handler = new Handler();

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						restartLoaders();						
					}
				}, 500);
			} else if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
				restartLoaders();
			}
		}
	}

	private void registerSDCardMountReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		intentFilter.addDataScheme("file");
		registerReceiver(mSDCardMountReceiver, intentFilter);
	}

	private void restartLoaders() {
		if (mFileTypeAdapter != null) {
			int position = 0;
			if (SdkConst.SHARE_APK)
				position = 1;
			Fragment fragment = getSupportFragmentManager().findFragmentByTag(
					mFileTypeAdapter.getFragmentTag(position));
//			((PictureChooserFragment) fragment).restartLoader();
		}
		if (mFileTypeAdapter != null) {
			int position = 1;
			if (SdkConst.SHARE_APK)
				position = 2;
			Fragment fragment = getSupportFragmentManager().findFragmentByTag(
					mFileTypeAdapter.getFragmentTag(position));
			((MediaFileChooseFragment) fragment).restartLoader();
		}
		if (mFileTypeAdapter != null) {
			int position = 2;
			if (SdkConst.SHARE_APK)
				position = 3;
			Fragment fragment = getSupportFragmentManager().findFragmentByTag(
					mFileTypeAdapter.getFragmentTag(position));
//			getSupportLoaderManager().destroyLoader(Const.LOADER_FILES);
//			getSupportLoaderManager().initLoader(Const.LOADER_FILES, null,
//					(FileChooserFragment) fragment);
//			((FileChooserFragment) fragment).restartLoad();
		}
	}

}
