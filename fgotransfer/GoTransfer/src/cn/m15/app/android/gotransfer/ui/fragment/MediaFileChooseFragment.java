package cn.m15.app.android.gotransfer.ui.fragment;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CursorTreeAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.m15.app.android.gotransfer.Const;
import cn.m15.app.android.gotransfer.R;
import cn.m15.app.android.gotransfer.ui.activity.MainActivity;
import cn.m15.app.android.gotransfer.ui.activity.MainActivity.TransferFilesChangeListener;
import cn.m15.app.android.gotransfer.ui.widget.FloatingGroupExpandableListView;
import cn.m15.app.android.gotransfer.ui.widget.WrapperExpandableListAdapter;
import cn.m15.app.android.gotransfer.utils.ImageUtil;
import cn.m15.app.android.gotransfer.utils.ValueConvertUtil;
import cn.m15.app.android.gotransfer.utils.images.Utils;
import cn.m15.app.android.gotransfer.utils.images.VideoImageFetcher;
import cn.m15.gotransfer.sdk.entity.TransferFile;
import cn.m15.gotransfer.sdk.entity.TransferFileManager;
import cn.m15.gotransfer.sdk.entity.TransferFilePool;
import cn.m15.gotransfer.sdk.utils.SdkFileUtil;

public class MediaFileChooseFragment extends Fragment implements LoaderCallbacks<Cursor>,
		TransferFilesChangeListener, OnChildClickListener {

	public static final String TAG = "media_chooser";

	public static final int INDEX_ID = 0;
	public static final int INDEX_DISPLAY_NAME = 1;
	public static final int INDEX_SIZE = 2;
	public static final int INDEX_PATH = 3;
	public static final int INDEX_DATE_MODIFIED = 4;
	public static final int INDEX_MIME_TYPE = 5;
	public static final int INDEX_DURATION = 6;
	public static final int INDEX_ARTIST = 7;

	private static final String[] PROJECTION_VEDIO = { 
			MediaStore.Video.Media._ID,
			MediaStore.Video.Media.DISPLAY_NAME,
			MediaStore.Video.Media.SIZE,
			MediaStore.Video.Media.DATA,
			MediaStore.Video.Media.DATE_MODIFIED,
			MediaStore.Video.Media.MIME_TYPE,
			MediaStore.Video.Media.DURATION };
	private static final String[] PROJECTION_MUSIC = {
			MediaStore.Audio.Media._ID,
			MediaStore.Audio.Media.DISPLAY_NAME,
			MediaStore.Audio.Media.SIZE,
			MediaStore.Audio.Media.DATA,
			MediaStore.Audio.Media.DATE_MODIFIED,
			MediaStore.Audio.Media.MIME_TYPE,
			MediaStore.Audio.Media.DURATION,
			MediaStore.Video.Media.ARTIST };

	public static final int INDEX_PATH_LOADFINISH = 0;

	private static final String _id = "_id";
	private static final String name = "name";

	private Uri mMusicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
	private Uri mVideoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

	public static final int INDEX_ID_CHILD = 0;
	public static final int INDEX_NAME = 1;

	private boolean mLoadMusic, mLoadVedio;

	private String[] mMusicArray;
	private String[] mVideoArray;

	private Map<String, Boolean> mFileStatus;
	private Map<String, Integer> mChildCheckedCount;

	private MediaChooseAdapter mMediaFilesChooseAdapter;
	private ProgressBar mProgressbar;
	private FloatingGroupExpandableListView mExpandableListViewShowFiles;

	private TransferFileManager mTransferFilesManager;
	private TransferFilePool mPool;
	private VideoImageFetcher mImageFetcher;
	private int mImageWidth;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_mediafile_chooser, null);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		mImageWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50,
				getResources().getDisplayMetrics());
		mImageFetcher = ImageUtil.createVedioImageFetcher(getActivity(), mImageWidth);
		super.onCreate(savedInstanceState);

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mPool = new TransferFilePool(5);
		mTransferFilesManager = TransferFileManager.getInstance();

		mFileStatus = new HashMap<String, Boolean>();
		mChildCheckedCount = new HashMap<String, Integer>();

		mProgressbar = (ProgressBar) getView().findViewById(R.id.progress_bar);
		mExpandableListViewShowFiles = (FloatingGroupExpandableListView) getView().findViewById(
				R.id.lv_vedio_list);
		mExpandableListViewShowFiles.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView absListView, int scrollState) {
				// Pause fetcher to ensure smoother scrolling when flinging
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
					// Before Honeycomb pause image loading on scroll to help
					// with performance
					if (!Utils.hasHoneycomb()) {
						mImageFetcher.setPauseWork(true);
					}
				} else {
					mImageFetcher.setPauseWork(false);
				}
			}

			@Override
			public void onScroll(AbsListView absListView, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});
		View mFooterView = ((LayoutInflater) getActivity().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.item_blank, null, false);
		mFooterView.setLayoutParams(new AbsListView.LayoutParams(
				AbsListView.LayoutParams.MATCH_PARENT, (int) TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics())));
		mExpandableListViewShowFiles.addFooterView(mFooterView);

		mMediaFilesChooseAdapter = new MediaChooseAdapter(getActivity());
		WrapperExpandableListAdapter wrapperAdapter = new WrapperExpandableListAdapter(
				mMediaFilesChooseAdapter);
		mExpandableListViewShowFiles.setAdapter(wrapperAdapter);
		mExpandableListViewShowFiles.setOnChildClickListener(this);

		getLoaderManager().initLoader(Const.LOADER_VEDIO, null, this);
		getLoaderManager().initLoader(Const.LOADER_MUSIC, null, this);
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
			int childPosition, long id) {
		Cursor cursor = mMediaFilesChooseAdapter.getChild(groupPosition, childPosition);
		String path = cursor.getString(INDEX_PATH);
		boolean oldStatus = mFileStatus.get(path);
		String groupName = mMediaFilesChooseAdapter.getGroup(groupPosition).getString(INDEX_NAME);
		int count = mChildCheckedCount.get(groupName);
		mFileStatus.put(path, !oldStatus);
		if (!oldStatus) {
			String name = cursor.getString(INDEX_DISPLAY_NAME);
			TransferFile transferFile = mPool.newTransferFiles();
			transferFile.path = path;
			transferFile.fileType = SdkFileUtil.getFileType(name);
			transferFile.name = name;
			transferFile.size = cursor.getLong(INDEX_SIZE);
			mTransferFilesManager.put(path, transferFile);
			count++;
		} else {
			TransferFile removedFile = mTransferFilesManager.remove(path);
			mPool.free(removedFile);
			count--;
		}
		mChildCheckedCount.put(groupName, count);
		onTransferFilesChangedListener();
		return true;
	}

	@Override
	public void onResume() {
		super.onResume();
		mImageFetcher.setExitTasksEarly(false);
	}

	@Override
	public void onPause() {
		super.onPause();
		mImageFetcher.setPauseWork(false);
		mImageFetcher.setExitTasksEarly(true);
		mImageFetcher.flushCache();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		getLoaderManager().destroyLoader(Const.LOADER_VEDIO);
		getLoaderManager().destroyLoader(Const.LOADER_MUSIC);
		mImageFetcher.closeCache();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Log.d("media_chooser", "onCreateLoader:"+id);
		CursorLoader loader = null;
		switch (id) {
		case Const.LOADER_VEDIO:
			loader = new CursorLoader(getActivity(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
					new String[] { MediaStore.Video.Media.DATA }, MediaStore.Video.Media.SIZE
							+ " > 0 ", null, MediaStore.Video.Media.DISPLAY_NAME);
			break;
		case Const.LOADER_MUSIC:
			loader = new CursorLoader(getActivity(), MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
					new String[] { MediaStore.Audio.Media.DATA }, MediaStore.Audio.Media.SIZE
							+ " > 0 ", null, MediaStore.Audio.Media.DISPLAY_NAME);
			break;
		}
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		Log.d("media_chooser", "onLoadFinished:"+cursor);
		switch (arg0.getId()) {
		case Const.LOADER_VEDIO:
			mLoadVedio = true;
			break;
		case Const.LOADER_MUSIC:
			mLoadMusic = true;
			break;
		}
		if (cursor != null) {
			try {
				String name = "";
				String _id = "";
				int count = 0;
				switch (arg0.getId()) {
				case Const.LOADER_VEDIO:
					name = getResources().getString(R.string.video);
					_id = "0";
					mVideoArray = new String[] { _id, name };
					break;
				case Const.LOADER_MUSIC:
					name = getResources().getString(R.string.music);
					_id = "1";
					mMusicArray = new String[] { _id, name };
					break;
				default:
					break;
				}

				if (mChildCheckedCount.containsKey(name)) {
					count = mChildCheckedCount.get(name);
				}
				while (cursor.moveToNext()) {
					String path = cursor.getString(INDEX_PATH_LOADFINISH);
					if (mTransferFilesManager.isFileSelected(path)) {
						mFileStatus.put(path, true);
						count++;
					} else {
						mFileStatus.put(path, false);
						count--;
					}
				}
				if (count < 0) {
					count = 0;
				}
				mChildCheckedCount.put(name, count);
			} finally {
				cursor.close();
			}
		}

		if (mLoadMusic && mLoadVedio) {
			Log.d("media_chooser", "onLoadFinished all");
			String[] columnNames = { _id, name };
			MatrixCursor matrixCursor = new MatrixCursor(columnNames, columnNames.length);
			if (mMusicArray != null) {
				matrixCursor.addRow(mMusicArray);
			}
			if (mVideoArray != null) {
				matrixCursor.addRow(mVideoArray);
			}
			mMediaFilesChooseAdapter.changeCursor(matrixCursor);
			for (int i = 0; i < columnNames.length; i++) {
				mExpandableListViewShowFiles.expandGroup(i);
			}
			mProgressbar.setVisibility(View.GONE);
			mExpandableListViewShowFiles.setVisibility(View.VISIBLE);
		}

	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		switch (arg0.getId()) {
		case Const.LOADER_VEDIO:
		case Const.LOADER_MUSIC:
			if (mMediaFilesChooseAdapter != null) {
				mMediaFilesChooseAdapter.changeCursor(null);
			}
			break;
		default:
			break;
		}
	}

	public class MediaChooseAdapter extends CursorTreeAdapter {

		private Context mContext;
		private LayoutInflater mInlater;

		public MediaChooseAdapter(Context context) {
			super(null, context);
			this.mContext = context;
			mInlater = LayoutInflater.from(context);

		}

		@Override
		protected Cursor getChildrenCursor(Cursor groupCursor) {
			Cursor childrenCursor = queryData(groupCursor.getString(INDEX_NAME));
			// SortCursorUtil sortCursor = new SortCursorUtil(childrenCursor,
			// MediaStore.MediaColumns.DISPLAY_NAME);
			// sortCursor.move(-1);
			// return sortCursor;
			return childrenCursor;
		}

		public Cursor queryData(String name) {
			Cursor childrenCursor = null;
			if (TextUtils.equals(name, getResources().getString(R.string.music))) {
				childrenCursor = mContext.getContentResolver().query(mMusicUri, PROJECTION_MUSIC,
						MediaStore.Audio.Media.SIZE + " > 0 ", null,
						MediaStore.Audio.Media.DISPLAY_NAME);
			} else if (TextUtils.equals(name, getResources().getString(R.string.video))) {
				childrenCursor = mContext.getContentResolver().query(mVideoUri, PROJECTION_VEDIO,
						MediaStore.Video.Media.SIZE + " > 0", null,
						MediaStore.Video.Media.DISPLAY_NAME);
			}
			return childrenCursor;
		}

		@Override
		protected View newGroupView(Context context, Cursor cursor, boolean isExpanded,
				ViewGroup parent) {
			View convertView = mInlater.inflate(R.layout.item_mediafile_group, parent, false);
			ViewHolder holder = new ViewHolder();
			holder.mGroupNameTv = (TextView) convertView.findViewById(R.id.tv_media_group_name);
			holder.mSelectedGroupView = convertView.findViewById(R.id.view_group_choose_all);
			convertView.setTag(holder);
			return convertView;
		}

		@Override
		protected void bindGroupView(View view, Context context, final Cursor cursor,
				boolean isExpanded) {
			final ViewHolder holder = (ViewHolder) view.getTag();
			final String groupName = cursor.getString(INDEX_NAME);
			final Cursor childCursor = queryData(groupName);

			if (childCursor != null) {
				if (childCursor.getCount() > 0) {
					view.setVisibility(View.VISIBLE);
					holder.mGroupNameTv.setText(groupName);
				} else {
					view.setVisibility(View.GONE);
				}
				try {
					if (mChildCheckedCount.get(groupName) == childCursor.getCount()
							&& childCursor.getCount() > 0) {
						holder.mSelectedGroupView.setSelected(true);
					} else {
						holder.mSelectedGroupView.setSelected(false);
					}
				} finally {
					childCursor.close();
				}
			}

			final int resId = isExpanded ? R.drawable.ic_subtract : R.drawable.ic_add;
			holder.mGroupNameTv.setCompoundDrawablesWithIntrinsicBounds(
					getResources().getDrawable(resId), null, null, null);
			holder.mSelectedGroupView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Cursor cursor = queryData(groupName);
					boolean isChecked = mChildCheckedCount.get(groupName) == cursor.getCount()
							&& cursor.getCount() > 0;
					if (cursor != null) {
						try {
							if (!isChecked) {
								while (cursor.moveToNext()) {
									String path = cursor.getString(INDEX_PATH);
									mFileStatus.put(path, true);
									String name = cursor.getString(INDEX_DISPLAY_NAME);
									TransferFile transferFile = mPool.newTransferFiles();
									transferFile.path = path;
									transferFile.fileType = SdkFileUtil.getFileType(name);
									transferFile.name = name;
									transferFile.size = cursor.getLong(INDEX_SIZE);
									mTransferFilesManager.put(path, transferFile);
									mChildCheckedCount.put(groupName, cursor.getCount());
								}
							} else {
								while (cursor.moveToNext()) {
									mFileStatus.put(cursor.getString(INDEX_PATH), false);
									TransferFile removedFile = mTransferFilesManager.remove(cursor
											.getString(INDEX_PATH));
									mPool.free(removedFile);
									mChildCheckedCount.put(groupName, 0);
								}
							}
						} finally {
							cursor.close();
						}
						onTransferFilesChangedListener();
					}
				}
			});
		}

		@Override
		protected View newChildView(Context context, Cursor cursor, boolean isLastChild,
				ViewGroup parent) {
			View convertView = mInlater.inflate(R.layout.item_mediafile_child, parent, false);
			ViewHolder holder = new ViewHolder();
			holder.mIbChlid = (ImageButton) convertView.findViewById(R.id.cb_mediafile);
			holder.mIvFile = (ImageView) convertView.findViewById(R.id.img_mediafile);
			holder.mTvFileName = (TextView) convertView.findViewById(R.id.tv_meidafile_name);
			holder.mTvVideoSize = (TextView) convertView.findViewById(R.id.tv_mediafile_size);
			holder.mTvFileInfo = (TextView) convertView.findViewById(R.id.tv_mediafile_info);
			convertView.setTag(holder);
			return convertView;
		}

		@Override
		protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
			ViewHolder holder = (ViewHolder) view.getTag();
			String name = cursor.getString(INDEX_DISPLAY_NAME);
			if (cursor.getColumnCount() == 8) {
				holder.mTvFileName.setText(name);
				holder.mIvFile.setImageResource(R.drawable.bg_music);
				holder.mTvFileInfo.setText(mContext.getString(R.string.file_artist,
						cursor.getString(INDEX_ARTIST)));
				holder.mTvVideoSize.setText(mContext.getString(R.string.file_size,
						Formatter.formatFileSize(mContext, cursor.getLong(INDEX_SIZE))));
			} else if (cursor.getColumnCount() == 7) {
				holder.mTvFileName.setText(name);
				mImageFetcher.loadImage(cursor.getString(INDEX_PATH), holder.mIvFile);
				long tempTime = cursor.getLong(INDEX_DURATION);
				String hms = ValueConvertUtil.formatMilSToHMS(tempTime);
				if (!TextUtils.isEmpty(hms)) {
					holder.mTvFileInfo.setText(mContext.getString(R.string.file_length, hms));
				}
				holder.mTvVideoSize.setText(mContext.getString(R.string.file_size,
						Formatter.formatFileSize(mContext, cursor.getLong(INDEX_SIZE))));
			}
			if (mFileStatus.containsKey(cursor.getString(INDEX_PATH))) {
				holder.mIbChlid.setSelected(mFileStatus.get(cursor.getString(INDEX_PATH)));
			} else {
				mFileStatus.put(cursor.getString(INDEX_PATH), false);
				holder.mIbChlid.setSelected(false);
			}
		}
	}

	public static class ViewHolder {
		ImageButton mIbChlid;
		ImageView mIvFile;
		TextView mTvFileName;
		TextView mTvVideoSize;
		TextView mTvFileInfo;

		TextView mGroupNameTv;
		View mSelectedGroupView;
	}

	@Override
	public void onTransferFilesChangedListener() {
		if (mMediaFilesChooseAdapter != null) {
			mMediaFilesChooseAdapter.notifyDataSetChanged();
		}
		((MainActivity) getActivity()).notifyTransferFilesChanged();
	}

	@Override
	public void onTransferFilesCancelled() {
		if (mMediaFilesChooseAdapter != null) {
			for (Entry<String, Boolean> entry : mFileStatus.entrySet()) {
				String path = entry.getKey();
				mFileStatus.put(path, false);
			}
			for (Entry<String, Integer> entry : mChildCheckedCount.entrySet()) {
				String key = entry.getKey();
				mChildCheckedCount.put(key, 0);
			}
		}
		onTransferFilesChangedListener();
	}

	public void restartLoader() {
		Log.d("media_chooser", "restartLoader");
		mLoadMusic = false;
		mLoadVedio = false;
		mProgressbar.setVisibility(View.VISIBLE);
		mExpandableListViewShowFiles.setVisibility(View.GONE);
		getLoaderManager().restartLoader(Const.LOADER_VEDIO, null, this);
		getLoaderManager().restartLoader(Const.LOADER_MUSIC, null, this);
	}
}
