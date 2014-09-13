package cn.m15.app.android.gotransfer.ui.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
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
import cn.m15.app.android.gotransfer.Const;
import cn.m15.app.android.gotransfer.GoTransferApplication;
import cn.m15.app.android.gotransfer.R;
import cn.m15.app.android.gotransfer.enity.TransferFilesManager;
import cn.m15.app.android.gotransfer.net.ipmsg.NetUdpThreadHelper.ConnectedUserChangedListener;
import cn.m15.app.android.gotransfer.net.ipmsg2.User;
import cn.m15.app.android.gotransfer.net.wifi.WifiApConnector;
import cn.m15.app.android.gotransfer.net.wifi.WifiApConnector.ConnectApStatusListener;
import cn.m15.app.android.gotransfer.net.wifi.WifiApManager.CreateWifiApResultListener;
import cn.m15.app.android.gotransfer.ui.fragment.AppsChooserFragment2;
import cn.m15.app.android.gotransfer.ui.fragment.FileChooserFragment;
import cn.m15.app.android.gotransfer.ui.fragment.MediaFileChooseFragment2;
import cn.m15.app.android.gotransfer.ui.fragment.PictureChooserFragment3;
import cn.m15.app.android.gotransfer.ui.fragment.dialog.ChangeNameDialog;
import cn.m15.app.android.gotransfer.ui.fragment.dialog.CommonDialogFragment;
import cn.m15.app.android.gotransfer.ui.widget.FragmentPagerAdapter;
import cn.m15.app.android.gotransfer.ui.widget.HorizontalListView;
import cn.m15.app.android.gotransfer.ui.widget.PagerSlidingTabStrip;
import cn.m15.app.android.gotransfer.utils.DialogUtil;

public class MainActivity2 extends BaseActivity2 implements View.OnClickListener,
		AdapterView.OnItemClickListener, CommonDialogFragment.DialogButtonClickListener,
		CreateWifiApResultListener, ConnectedUserChangedListener, ConnectApStatusListener {

	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private IconArrayAdapter mNavigationAdapter;

	private PagerSlidingTabStrip mTab;
	private ViewPager mViewPager;
	private FileTypeAdapter mFileTypeAdapter;

	private View mShareButtonsView;
	private View mConnectFriendsView;
	private View mFileOperationsView;
	private View mConnectionView;

	private Button mSendBtn;
	private Button mCloseBtn;
	private TransferFilesManager mManager;

	private HorizontalListView mListView;
	private boolean[] mChecked;
	private UserAdapter mUserAdapter;
	private ArrayList<User> mUserList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main2);
		mManager = TransferFilesManager.getInstance();

		// initialize navigation drawer
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_actionbar_close, // TODO: need to replace
				R.string.app_name, R.string.app_name);
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setHomeButtonEnabled(true);
		String[] navigationNames = getResources().getStringArray(R.array.menu);
		ListView navigationListView = (ListView) findViewById(R.id.lv_navigation);
		mNavigationAdapter = new IconArrayAdapter(this, R.layout.item_navigation,
				R.id.tv_navigation, navigationNames);
		navigationListView.setAdapter(mNavigationAdapter);
		navigationListView.setOnItemClickListener(this);

		// initialize content view
		mViewPager = (ViewPager) findViewById(R.id.vp_content);
		mViewPager.setOffscreenPageLimit(3);
		mFileTypeAdapter = new FileTypeAdapter(this);
		mViewPager.setAdapter(mFileTypeAdapter);
		mTab = (PagerSlidingTabStrip) findViewById(R.id.tab);
		mTab.setViewPager(mViewPager);

		// initialize buttons click event
		findViewById(R.id.btn_hot_selling).setOnClickListener(this);
		findViewById(R.id.btn_invite_friends).setOnClickListener(this);
		mConnectFriendsView = findViewById(R.id.btn_connect_friends);
		mConnectFriendsView.setOnClickListener(this);

		// initialize views
		mShareButtonsView = findViewById(R.id.ll_share_buttons);

		mApManager.addCreateWifiApResultListener(this);
		netThreadHelper.addConnectedUserChangedListener(this);
		mApConnector.addConnectApStatusListener(this);

		mUserList = new ArrayList<User>();
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
		case R.id.btn_connect_friends:
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
			if (mApConnector.isCreateOrConnectMine()) {
				showConnectionView();
				ArrayList<User> selectedUsers = new ArrayList<User>();
				if (mChecked != null) {
					int length = mChecked.length;
					for (int i = 0; i < length; i++) {
						if (mChecked[i]) {
							selectedUsers.add(mUserList.get(i));
						}
					}
				}
				if (selectedUsers.size() > 0) {
					startTransferFiles(this, selectedUsers, TransferFilesManager.getInstance()
							.toList());
					clearTransferFiles();
				} else {
					Toast.makeText(this, R.string.select_user_hint, Toast.LENGTH_SHORT).show();
				}
			} else {
				Intent intent = new Intent(this, CreateConnectionActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra("is_from_send_btn", true);
				intent.putParcelableArrayListExtra("transfer_file_list", TransferFilesManager
						.getInstance().toList());
				startActivity(intent);
				clearTransferFiles();
			}
			break;
		case R.id.btn_break:
			DialogUtil.showCloseConnectionDialog(this);
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
		case 0: // user name
			DialogUtil.showChangeNameDialog(this);
			break;
		case 1: // connect iphone
			startActivity(new Intent(this, ConnectAppleActivity.class));
			break;
		case 2: // connect pc
//			Toast.makeText(this, "connect pc", Toast.LENGTH_SHORT).show();
			startActivity(new Intent(this, ConnectPcActivity.class));
			break;
		case 3: // feedback
			gotoFeedback();
			break;
		case 4: // settings
			startActivity(new Intent(this, SettingsActivity.class));
			break;
		case 5: // about
			startActivity(new Intent(this, AboutGoTransferActivity.class));
			break;
		}
	}

	private void gotoFeedback() {
		if (WifiApConnector.getInstance().isWifiConnected()) {
			String notic_title = getResources().getString(R.string.feed_back_title);
			String link = Const.FEEDBACK_URL;
			String imei = GoTransferApplication.getInstance().getUniqueId();
			String version = GoTransferApplication.getAppVersionName(this);
			link += "?action=feedback&imei=" + imei + "&app_id=" + Const.APP_ID + "&app_version="
					+ version + "&language="
					+ getResources().getConfiguration().locale.getCountry();
			Log.d("main", "link >>> " + link);
			Intent intent = new Intent(this, CommonWebViewActivity.class);
			intent.putExtra("notice_title", notic_title);
			intent.putExtra("link", link);
			startActivity(intent);
		} else {
			Toast.makeText(this, R.string.no_wifi_connected, Toast.LENGTH_SHORT).show();
		}
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
		if (FileTypeAdapter.appFragment != null) {
			FileTypeAdapter.appFragment.onTransferFilesCancelled();
		}
		if (FileTypeAdapter.pictureFragment != null) {
			FileTypeAdapter.pictureFragment.onTransferFilesCancelled();
		}
		if (FileTypeAdapter.vedioFragment != null) {
			FileTypeAdapter.vedioFragment.onTransferFilesCancelled();
		}
		if (FileTypeAdapter.fileFragment != null) {
			FileTypeAdapter.fileFragment.onTransferFilesCancelled();
		}

		if (mSendBtn != null) {
			mSendBtn.setText(getString(R.string.send_with_number, 0));
		}
		hideFileOperationsView();
	}

	public void showFileOperationsView() {
		if (mShareButtonsView.getVisibility() == View.VISIBLE) {
			mShareButtonsView.setVisibility(View.GONE);
		}
		if (mFileOperationsView == null) {
			ViewStub fileOperationsVs = (ViewStub) findViewById(R.id.vs_file_operations);
			mFileOperationsView = fileOperationsVs.inflate();
			mFileOperationsView.findViewById(R.id.btn_cancel).setOnClickListener(this);
			mSendBtn = (Button) mFileOperationsView.findViewById(R.id.btn_send);
			mSendBtn.setOnClickListener(this);
		}
		mSendBtn.setText(getString(R.string.send_with_number, mManager.size()));
		if (mShareButtonsView.getVisibility() != View.VISIBLE) {
			mFileOperationsView.setVisibility(View.VISIBLE);
		}
	}

	public void hideFileOperationsView() {
		if (mFileOperationsView != null && mFileOperationsView.getVisibility() == View.VISIBLE) {
			mShareButtonsView.setVisibility(View.VISIBLE);
			mFileOperationsView.setVisibility(View.GONE);
		}
	}

	public void showConnectionView() {
		mConnectFriendsView.setVisibility(View.GONE);
		if (mConnectionView == null) {
			ViewStub fileOperationsVs = (ViewStub) findViewById(R.id.vs_connection);
			mConnectionView = fileOperationsVs.inflate();
		}
		mConnectionView.setVisibility(View.VISIBLE);
		mCloseBtn = (Button) mConnectionView.findViewById(R.id.btn_break);
		mCloseBtn.setOnClickListener(this);
		mListView = (HorizontalListView) findViewById(R.id.hlv_receivers);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ImageView ivChecked = (ImageView) view.findViewById(R.id.iv_user_checked);
				mChecked[position] = !mChecked[position];
				ivChecked.setVisibility(mChecked[position] ? View.VISIBLE : View.GONE);
			}
		});
	}

	public void hideConnectionView() {
		if (mConnectionView != null && mConnectionView.getVisibility() == View.VISIBLE) {
			mConnectFriendsView.setVisibility(View.VISIBLE);
			mConnectionView.setVisibility(View.GONE);
			mConnectFriendsView.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onDestroy() {
		netThreadHelper.removeConnectedUserChangedListener(this);
		mApManager.removeCreateWifiApResultListener(this);
		mApConnector.removeConnectApStatusListener(this);
		mManager.clear();
		closeWifiConnection();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main2, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		switch (item.getItemId()) {
		case R.id.menu:
			Intent intent = new Intent(this, ConversationActivity2.class);
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
			GoTransferApplication.getInstance().setSelfName(modifiedName);
			mNavigationAdapter.notifyUsernameChanged();
		} else if (DialogUtil.DIALOG_CLOSE_CONNECTION.equals(tag)
				&& which == DialogInterface.BUTTON_POSITIVE) {
			closeWifiConnection();
			hideConnectionView();
		} else if (DialogUtil.DIALOG_CLEAR_SELECTED_FILES.equals(tag)
				&& which == DialogInterface.BUTTON_POSITIVE) {
			clearTransferFiles();
		}
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
		public static AppsChooserFragment2 appFragment;
		public static PictureChooserFragment3 pictureFragment;
		// public static MediaFileChooseFragment vedioFragment;
		public static MediaFileChooseFragment2 vedioFragment;
		public static FileChooserFragment fileFragment;

		public FileTypeAdapter(FragmentActivity activity) {
			super(activity.getSupportFragmentManager());
			titles = activity.getResources().getStringArray(R.array.tab_file_type);
			if (!Const.SHARE_APK) {
				titles = Arrays.copyOfRange(titles, 1, titles.length);
			}
		}

		@Override
		public Fragment getItem(int position) {
			if (Const.SHARE_APK) {
				switch (position) {
				case 0: // application
					appFragment = new AppsChooserFragment2();
					return appFragment;
				case 1: // picture
					pictureFragment = new PictureChooserFragment3();
					return pictureFragment;
				case 2: // video & audio
					vedioFragment = new MediaFileChooseFragment2();
					return vedioFragment;
				default: // all files
					fileFragment = new FileChooserFragment();
					return fileFragment;
				}
			} else {
				switch (position) {
				case 0: // picture
					pictureFragment = new PictureChooserFragment3();
					return pictureFragment;
				case 1: // video & audio
					vedioFragment = new MediaFileChooseFragment2();
					return vedioFragment;
				default: // all files
					fileFragment = new FileChooserFragment();
					return fileFragment;
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

	}

	private static class IconArrayAdapter extends ArrayAdapter<String> {
		private String[] items;
		private int[] iconIds;

		public IconArrayAdapter(Context context, int resource, int textViewResourceId,
				String[] objects) {
			super(context, resource, textViewResourceId, objects);
			items = objects;

			TypedArray ta = context.getResources().obtainTypedArray(R.array.nav_icons);
			int size = ta.length();
			iconIds = new int[size];
			for (int i = 0; i < size; i++) {
				iconIds[i] = ta.getResourceId(i, R.drawable.ic_nav_name);
			}
			ta.recycle();

			notifyUsernameChanged();
		}

		public void notifyUsernameChanged() {
			items[0] = GoTransferApplication.getInstance().getSelfName();
			notifyDataSetChanged();
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
		showConnectionView();
		TextView tvStatus = (TextView) findViewById(R.id.tv_connection_status);
		tvStatus.setVisibility(View.VISIBLE);
		tvStatus.setText(R.string.creating_wifi_text);
		mCloseBtn.setVisibility(View.GONE);
	}

	@Override
	public void createWifiSuccess() {
		showConnectionView();
		TextView tvStatus = (TextView) findViewById(R.id.tv_connection_status);
		tvStatus.setVisibility(View.VISIBLE);
		tvStatus.setText(R.string.waiting_join_text);
		netThreadHelper.connectSocket();
		mCloseBtn.setVisibility(View.VISIBLE);
	}

	@Override
	public void createWifiFailed() {
		showConnectionView();
		TextView tvStatus = (TextView) findViewById(R.id.tv_connection_status);
		tvStatus.setVisibility(View.VISIBLE);
		tvStatus.setText(R.string.creat_wifi_failed_text);
	}

	@Override
	public void connectedUserChanged(Map<String, User> user) {
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
			mUserAdapter=new UserAdapter(this, R.layout.item_selcet_user, mUserList);
			mListView.setAdapter(mUserAdapter);
		} else if (user.size() == 0) {
			if (!mApManager.isWifiApEnabled()) {
				hideConnectionView();
			}
		}
	}

	@Override
	public void connectApStarted() {
	}

	@Override
	public void connectApSuccess() {
		showConnectionView();
		netThreadHelper.connectSocket();
	}

	private class UserAdapter extends ArrayAdapter<User> {
		private LayoutInflater mInflater;

		public UserAdapter(Context context, int resource, ArrayList<User> userList) {
			super(context, R.layout.item_selcet_user, userList);
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item_selcet_user, parent, false);
				holder = new ViewHolder();
				holder.mIvUserChecked = (ImageView) convertView.findViewById(R.id.iv_user_checked);
				holder.mIvUserPic = (ImageView) convertView.findViewById(R.id.iv_user);
				holder.mTvUserName = (TextView) convertView.findViewById(R.id.tv_wifi_spot_name);
				holder.mIvUserChecked.setVisibility(View.INVISIBLE);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			User user = getItem(position);
			holder.mTvUserName.setText(user.getAlias());
			if (mChecked[position]) {
				holder.mIvUserChecked.setVisibility(View.VISIBLE);
			} else {
				holder.mIvUserChecked.setVisibility(View.INVISIBLE);
			}
			return convertView;
		}
	}

	public static class ViewHolder {
		ImageView mIvUserChecked;
		ImageView mIvUserPic;
		TextView mTvUserName;
	}

	public interface TransferFilesChangeListener {

		// 传输文件数量改变
		public void onTransferFilesChangedListener();

		// 取消传输文件
		public void onTransferFilesCancelled();

	}
}
