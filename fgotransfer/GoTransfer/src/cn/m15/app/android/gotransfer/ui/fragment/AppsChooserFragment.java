package cn.m15.app.android.gotransfer.ui.fragment;

import java.util.List;

import u.aly.co;
import u.aly.v;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.format.Formatter;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.m15.app.android.gotransfer.Const;
import cn.m15.app.android.gotransfer.R;
import cn.m15.app.android.gotransfer.ui.activity.MainActivity;
import cn.m15.app.android.gotransfer.ui.activity.MainActivity.TransferFilesChangeListener;
import cn.m15.app.android.gotransfer.ui.widget.AppListLoader;
import cn.m15.app.android.gotransfer.utils.AppEntry;
import cn.m15.gotransfer.sdk.entity.TransferFile;
import cn.m15.gotransfer.sdk.entity.TransferFileManager;
import cn.m15.gotransfer.sdk.net.ipmsg.MessageConst.FileType;

public class AppsChooserFragment extends Fragment implements
		LoaderManager.LoaderCallbacks<List<AppEntry>>, AdapterView.OnItemClickListener,
		TransferFilesChangeListener {

	private GridView mGridView;
	private ProgressBar mProgressbar;
	private AppListAdapter mAdapter;
	private boolean[] mChecked;
	private TransferFileManager mTransferFilesManager;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(Const.LOADER_APPS, null, this);

		mTransferFilesManager = TransferFileManager.getInstance();
		mProgressbar = (ProgressBar) getView().findViewById(R.id.progress_bar);
		mGridView = (GridView) getActivity().findViewById(R.id.grid_apps);
		mAdapter = new AppListAdapter(getActivity());
		mGridView.setAdapter(mAdapter);

		mGridView.setOnItemClickListener(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_apps, null);
	}

	public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
		AppEntry item = mAdapter.getItem(position);
		ImageView mImgChecked = (ImageView) view.findViewById(R.id.iv_checked);
		mChecked[position] = mChecked[position] == false ? true : false;
		if (position > mAdapter.getCount() - 5) {
			mGridView.setSelector(Color.TRANSPARENT);
			return;
		}
		if (mChecked[position]) {
			mImgChecked.setVisibility(View.VISIBLE);
			TransferFile transferFile = new TransferFile();
			transferFile.path = item.mApkFile.getPath();
			transferFile.fileType = FileType.APP;
			transferFile.name = item.getLabel() + ".apk";
			transferFile.size = item.mApkFile.length();
			mTransferFilesManager.put(transferFile.path, transferFile);
		} else {
			mImgChecked.setVisibility(View.INVISIBLE);
			mTransferFilesManager.remove(item.mApkFile.getPath());
		}
		onTransferFilesChangedListener();
	}

	@Override
	public Loader<List<AppEntry>> onCreateLoader(int arg0, Bundle arg1) {
		return new AppListLoader(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<List<AppEntry>> arg0, List<AppEntry> arg1) {
		AppEntry appEntry = new AppEntry("");
		for (int i = 0; i < 4; i++) {
			arg1.add(appEntry);
		}
		mChecked = new boolean[arg1.size()];
		mAdapter.setData(arg1);
		mProgressbar.setVisibility(View.GONE);
		mGridView.setVisibility(View.VISIBLE);
	}

	@Override
	public void onLoaderReset(Loader<List<AppEntry>> arg0) {
		mAdapter.setData(null);
	}

	@Override
	public void onTransferFilesChangedListener() {
		((MainActivity) getActivity()).notifyTransferFilesChanged();
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onTransferFilesCancelled() {
		Log.d("AppsChooserFragment2", "mAdapter >>> " + mAdapter);
		if (mAdapter != null) {
			for (int i = 0; i < mChecked.length; i++) {
				if (mChecked[i]) {
					mChecked[i] = false;
				}
			}
		}
		onTransferFilesChangedListener();
	}

	public static class PackageIntentReceiver extends BroadcastReceiver {
		final AppListLoader mLoader;

		public PackageIntentReceiver(AppListLoader loader) {
			mLoader = loader;
			IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
			filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
			filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
			filter.addDataScheme("package");
			mLoader.getContext().registerReceiver(this, filter);
			// Register for events related to sdcard installation.
			IntentFilter sdFilter = new IntentFilter();
			sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
			sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
			mLoader.getContext().registerReceiver(this, sdFilter);
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			// Tell the loader about the change.
			mLoader.onContentChanged();
		}
	}

	public class AppListAdapter extends ArrayAdapter<AppEntry> {

		private final LayoutInflater mInflater;
		private int mAdapterSize;

		public AppListAdapter(Context context) {
			super(context, R.layout.item_apps);
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public void setData(List<AppEntry> data) {
			if (data != null) {
				clear();
				for (AppEntry entry : data) {
					add(entry);
				}
				mAdapterSize = data.size();
			}
		}

		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}

		@Override
		public boolean isEnabled(int position) {

			if (position > mAdapterSize - 5) {
				return false;
			} else {
				return true;
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item_apps, parent, false);
				holder = new ViewHolder();
				holder.mFileImg = (ImageView) convertView.findViewById(R.id.iv_apps);
				holder.mFileNameTv = (TextView) convertView.findViewById(R.id.tv_appname);
				holder.mFileInfoTv = (TextView) convertView.findViewById(R.id.tv_appsize);
				holder.mImgChecked = (ImageView) convertView.findViewById(R.id.iv_checked);
				holder.mImgChecked.setVisibility(View.INVISIBLE);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			isEnabled(position);
			if (position > mAdapterSize - 5) {
				holder.mFileNameTv.setVisibility(View.INVISIBLE);
				holder.mFileImg.setVisibility(View.INVISIBLE);
				holder.mFileInfoTv.setVisibility(View.INVISIBLE);
				holder.mImgChecked.setVisibility(View.INVISIBLE);
				convertView.setBackgroundColor(Color.TRANSPARENT);
				return convertView;
			}
			holder.mFileNameTv.setVisibility(View.VISIBLE);
			holder.mFileImg.setVisibility(View.VISIBLE);
			holder.mFileInfoTv.setVisibility(View.VISIBLE);
			holder.mImgChecked.setVisibility(View.VISIBLE);
			AppEntry item = getItem(position);
			holder.mFileNameTv.setText(item.getLabel());
			holder.mFileImg.setImageDrawable(item.getIcon());
			holder.mFileInfoTv.setText(Formatter.formatFileSize(getContext(),
					item.mApkFile.length()));
			if (mChecked[position]) {
				convertView.setBackgroundResource(R.drawable.bg_app);
				holder.mImgChecked.setVisibility(View.VISIBLE);
			} else {
				convertView.setBackgroundColor(Color.TRANSPARENT);
				holder.mImgChecked.setVisibility(View.INVISIBLE);
			}
			return convertView;
		}

	}

	public static class ViewHolder {
		CheckBox mCheckBox;
		ImageView mFileImg;
		ImageView mImgChecked;
		View mShadeView;
		TextView mFileNameTv;
		TextView mFileInfoTv;
		TextView mFolderNameTv;
	}

}
