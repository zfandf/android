package cn.m15.app.android.gotransfer.ui.fragment;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import cn.m15.app.android.gotransfer.Const;
import cn.m15.app.android.gotransfer.R;
import cn.m15.app.android.gotransfer.enity.AppEntry;
import cn.m15.app.android.gotransfer.enity.TransferFile;
import cn.m15.app.android.gotransfer.enity.TransferFilesManager;
import cn.m15.app.android.gotransfer.net.ipmsg2.MessageConst.FileType;
import cn.m15.app.android.gotransfer.ui.activity.MainActivity2;
import cn.m15.app.android.gotransfer.ui.activity.MainActivity2.TransferFilesChangeListener;
import cn.m15.app.android.gotransfer.ui.widget.AppListLoader;

public class AppsChooserFragment2 extends Fragment implements
		LoaderManager.LoaderCallbacks<List<AppEntry>>, AdapterView.OnItemClickListener,
		TransferFilesChangeListener {

	private GridView mGridView;
	private AppListAdapter mAdapter;
	private boolean[] mChecked;
	private TransferFilesManager mTransferFilesManager;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(Const.LOADER_APPS, null, this);

		mTransferFilesManager = TransferFilesManager.getInstance();
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
		mAdapter.setData(arg1);
		mChecked = new boolean[arg1.size()];
	}

	@Override
	public void onLoaderReset(Loader<List<AppEntry>> arg0) {
		mAdapter.setData(null);
	}

	@Override
	public void onTransferFilesChangedListener() {
		((MainActivity2) getActivity()).notifyTransferFilesChanged();
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

			AppEntry item = getItem(position);
			holder.mFileNameTv.setText(item.getLabel());
			holder.mFileImg.setImageDrawable(item.getIcon());
			holder.mFileInfoTv.setText(Formatter.formatFileSize(getContext(), item.mApkFile.length()));
			if (mChecked[position]) {
				holder.mImgChecked.setVisibility(View.VISIBLE);
			} else {
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
