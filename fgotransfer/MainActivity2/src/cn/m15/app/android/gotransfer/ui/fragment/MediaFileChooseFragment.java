package cn.m15.app.android.gotransfer.ui.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.m15.app.android.gotransfer.Const;
import cn.m15.app.android.gotransfer.R;
import cn.m15.app.android.gotransfer.enity.TransferFile;
import cn.m15.app.android.gotransfer.enity.TransferFilePool;
import cn.m15.app.android.gotransfer.enity.TransferFilesManager;
import cn.m15.app.android.gotransfer.net.ipmsg2.MessageConst.FileType;
import cn.m15.app.android.gotransfer.ui.activity.MainActivity2;
import cn.m15.app.android.gotransfer.ui.activity.MainActivity2.TransferFilesChangeListener;
import cn.m15.app.android.gotransfer.ui.widget.FloatingGroupExpandableListView;
import cn.m15.app.android.gotransfer.ui.widget.WrapperExpandableListAdapter;
import cn.m15.app.android.gotransfer.utils.FileUtil;
import cn.m15.app.android.gotransfer.utils.ImageUtil;
import cn.m15.app.android.gotransfer.utils.LanguageComparatorCN;
import cn.m15.app.android.gotransfer.utils.ValueConvertUtil;
import cn.m15.app.android.gotransfer.utils.images.VedioImageFetcher;

public class MediaFileChooseFragment extends Fragment implements LoaderCallbacks<Cursor>,
		TransferFilesChangeListener {

	public static final String TAG = "media_chooser";

	public static final int INDEX_ID = 0;
	public static final int INDEX_DISPLAY_NAME = 1;
	public static final int INDEX_SIZE = 2;
	public static final int INDEX_PATH = 3;
	public static final int INDEX_DATE_MODIFIED = 4;
	public static final int INDEX_MIME_TYPE = 5;
	public static final int INDEX_DURATION = 6;
	public static final int INDEX_TITLE = 7;

	private boolean mLoadMusic, mLoadVedio;

	private static final String[] PROJECTION_VEDIO = { MediaStore.Video.Media._ID,
			MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.SIZE,
			MediaStore.Video.Media.DATA, MediaStore.Video.Media.DATE_MODIFIED,
			MediaStore.Video.Media.MIME_TYPE, MediaStore.Video.Media.DURATION,
			MediaStore.Video.Media.DURATION };
	private static final String[] PROJECTION_MUSIC = { MediaStore.Audio.Media._ID,
			MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.SIZE,
			MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DATE_MODIFIED,
			MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.DURATION,
			MediaStore.Audio.Media.TITLE };

	private MediaChooseAdapter mMediaFilesChooseAdapter;
	private ProgressBar mProgressbar;
	private FloatingGroupExpandableListView mExpandableListViewShowFiles;
	private List<GroupInfo> mMediaFilesList;

	private TransferFilesManager mTransferFilesManager;
	private TransferFilePool mPool;
	private VedioImageFetcher mImageFetcher;
	private int mImageWidth;

	private LanguageComparatorCN mCnSort = new LanguageComparatorCN();

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
		mTransferFilesManager = TransferFilesManager.getInstance();

		mProgressbar = (ProgressBar) getView().findViewById(R.id.progress_bar);
		mExpandableListViewShowFiles = (FloatingGroupExpandableListView) getView().findViewById(
				R.id.lv_vedio_list);

		mMediaFilesList = new ArrayList<GroupInfo>();
		mExpandableListViewShowFiles.setOnChildClickListener(new ChildClick());

		getLoaderManager().initLoader(Const.LOADER_VEDIO, null, this);
		Log.d(TAG, "VEDIO---initLoader");
		getLoaderManager().initLoader(Const.LOADER_MUSIC, null, this);
		Log.d(TAG, "MUSIC---initLoader");
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

	private void addFileToManager(int groupPosition) {
		GroupInfo group = mMediaFilesList.get(groupPosition);
		if (group.isChecked) {
			for (MediaInfo mediaInfo : mMediaFilesList.get(groupPosition).childList) {
				TransferFile item = mPool.newTransferFiles();
				item.path = mediaInfo.path;
				item.fileType = FileUtil.getFileType(mediaInfo.name);
				item.name = mediaInfo.name;
				item.size = mediaInfo.size;
				mTransferFilesManager.put(item.path, item);
			}
		} else {
			for (MediaInfo mediaInfo : mMediaFilesList.get(groupPosition).childList) {
				if (mediaInfo.isChecked) {
					TransferFile item = mPool.newTransferFiles();
					item.path = mediaInfo.path;
					item.fileType = FileUtil.getFileType(mediaInfo.name);
					item.name = mediaInfo.name;
					item.size = mediaInfo.size;
					mTransferFilesManager.put(item.path, item);
				} else {
					TransferFile removedFile = mTransferFilesManager.remove(mediaInfo.path);
					mPool.free(removedFile);
				}
			}
		}
		Log.d(TAG + "manamger size", String.valueOf(mTransferFilesManager.size()));
		onTransferFilesChangedListener();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Log.d(TAG, "onCreateLoader");
		CursorLoader loader = null;
		switch (id) {
		case Const.LOADER_VEDIO:
			loader = new CursorLoader(getActivity(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
					PROJECTION_VEDIO, MediaStore.Video.Media.SIZE + " > 0", null,
					MediaStore.Video.Media.DISPLAY_NAME);
			Log.d(TAG, "VEDIO---onCreateLoader");
			break;
		case Const.LOADER_MUSIC:
			loader = new CursorLoader(getActivity(), MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
					PROJECTION_MUSIC, MediaStore.Audio.Media.SIZE + " > 0 ", null,
					MediaStore.Audio.Media.DISPLAY_NAME);
			Log.d(TAG, "MUSIC---onCreateLoader");
			break;
		}
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		Log.d(TAG, "onLoadFinished");
		List<MediaInfo> mVedioList;
		List<MediaInfo> mMusitList;
		switch (arg0.getId()) {
		case Const.LOADER_VEDIO:
			mLoadVedio = true;
			if (cursor != null) {
				mVedioList = new ArrayList<MediaInfo>();
				MediaInfo mediaInfo;
				while (cursor.moveToNext()) {
					mediaInfo = new MediaInfo();
					mediaInfo.path = cursor.getString(INDEX_PATH);
					mediaInfo.name = cursor.getString(INDEX_DISPLAY_NAME);
					mediaInfo.size = cursor.getLong(INDEX_SIZE);
					mediaInfo.date = cursor.getLong(INDEX_DATE_MODIFIED);
					mediaInfo.length = cursor.getLong(INDEX_DURATION);
					mediaInfo.isChecked = false;
					mediaInfo.fileType = FileType.VIDEO;
					mVedioList.add(mediaInfo);
					mediaInfo = null;
				}

				Collections.sort(mVedioList, mCnSort);

				GroupInfo groupInfo = new GroupInfo();
				groupInfo.name = getResources().getString(R.string.video);
				groupInfo.isChecked = false;
				groupInfo.childList = mVedioList;
				mMediaFilesList.add(groupInfo);
				groupInfo = null;
				mVedioList = null;
				Log.d(TAG, " VEDIO  onLoadFinished >>> " + cursor.getCount());
			}
			break;
		case Const.LOADER_MUSIC:
			mLoadMusic = true;
			if (cursor != null) {
				mMusitList = new ArrayList<MediaInfo>();
				MediaInfo mediaInfo;
				while (cursor.moveToNext()) {
					mediaInfo = new MediaInfo();
					mediaInfo.path = cursor.getString(INDEX_PATH);
					mediaInfo.name = cursor.getString(INDEX_TITLE);
					mediaInfo.size = cursor.getLong(INDEX_SIZE);
					mediaInfo.date = cursor.getLong(INDEX_DATE_MODIFIED);
					mediaInfo.length = cursor.getLong(INDEX_DURATION);
					mediaInfo.isChecked = false;
					mediaInfo.fileType = FileType.MUSIC;
					mMusitList.add(mediaInfo);
					mediaInfo = null;
				}
				Collections.sort(mMusitList, mCnSort);

				GroupInfo groupInfo = new GroupInfo();
				groupInfo.name = getResources().getString(R.string.music);
				groupInfo.isChecked = false;
				groupInfo.childList = mMusitList;
				mMediaFilesList.add(groupInfo);
				groupInfo = null;
				mMusitList = null;
				Log.d(TAG, "MUSIC  onLoadFinished >>> " + cursor.getCount());
			}
			break;
		default:
			break;
		}
		if (mLoadMusic && mLoadVedio && mMediaFilesList.size() == 2) {
			if (!mMediaFilesList.get(0).name.equals(getResources().getString(R.string.video))) {
				GroupInfo tempMusicInfo = mMediaFilesList.get(0);
				GroupInfo tempVedioInfo = mMediaFilesList.get(1);
				mMediaFilesList.clear();
				mMediaFilesList.add(0, tempVedioInfo);
				mMediaFilesList.add(1, tempMusicInfo);
			}
		}
		mExpandableListViewShowFiles.setChildDivider(new ColorDrawable(R.color.c10));
		mMediaFilesChooseAdapter = new MediaChooseAdapter(getActivity(), mMediaFilesList);
		WrapperExpandableListAdapter wrapperAdapter = new WrapperExpandableListAdapter(
				mMediaFilesChooseAdapter);
		mExpandableListViewShowFiles.setAdapter(wrapperAdapter);
		mProgressbar.setVisibility(View.GONE);
		mExpandableListViewShowFiles.setVisibility(View.VISIBLE);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		Log.d(TAG, "onLoaderReset");
		switch (arg0.getId()) {
		case Const.LOADER_VEDIO:
			if (mMediaFilesList != null) {
				mMediaFilesList.clear();
				mMediaFilesChooseAdapter.notifyDataSetChanged();
			}
			break;
		case Const.LOADER_MUSIC:
			if (mMediaFilesList != null) {
				mMediaFilesList.clear();
				mMediaFilesChooseAdapter.notifyDataSetChanged();
			}
			break;
		default:
			break;
		}
	}

	class ChildClick implements OnChildClickListener {

		@Override
		public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
				int childPosition, long id) {

			mMediaFilesList.get(groupPosition).getChildItem(childPosition).toggle();
			int childrenCount = mMediaFilesList.get(groupPosition).getChildrenCount();
			boolean childrenAllIsChecked = true;
			for (int i = 0; i < childrenCount; i++) {
				if (!mMediaFilesList.get(groupPosition).getChildItem(i).isChecked)
					childrenAllIsChecked = false;
			}
			mMediaFilesList.get(groupPosition).isChecked = childrenAllIsChecked;
			mMediaFilesChooseAdapter.notifyDataSetChanged();
			addFileToManager(groupPosition);
			return true;
		}

	}

	public class MediaChooseAdapter extends BaseExpandableListAdapter {

		private Context mContext;
		private List<GroupInfo> mFileList;
		private LayoutInflater mInflater;

		public MediaChooseAdapter(Context context, List<GroupInfo> mFileList) {
			this.mContext = context;
			this.mFileList = mFileList;
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getGroupCount() {
			return mFileList != null ? mMediaFilesList.size() : 0;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return mFileList.get(groupPosition).childList.size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return mFileList.get(groupPosition);
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return mFileList.get(groupPosition).childList.get(childPosition);
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public View getGroupView(final int groupPosition, final boolean isExpanded,
				View convertView, ViewGroup parent) {
			final GroupInfo groupInfo = (GroupInfo) getGroup(groupPosition);
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item_mediafile_group, parent, false);
				holder = new ViewHolder();
				holder.mGroupNameTv = (TextView) convertView.findViewById(R.id.tv_media_group_name);
				holder.mSelectedGroupView = convertView.findViewById(R.id.view_group_choose_all);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.mGroupNameTv.setText(groupInfo.name);
			holder.mSelectedGroupView.setSelected(groupInfo.isChecked);
			holder.mSelectedGroupView.setOnClickListener(new GroupCheckBoxClick(Integer
					.valueOf(groupPosition)));

			final int resId = isExpanded ? R.drawable.ic_nav_receivefile
					: R.drawable.ic_nav_setting;
			holder.mGroupNameTv.setCompoundDrawablesWithIntrinsicBounds(
					getResources().getDrawable(resId), null, null, null);

			return convertView;
		}

		class GroupCheckBoxClick implements OnClickListener {
			private int mGroupPosition;

			GroupCheckBoxClick(int groupPosition) {
				this.mGroupPosition = groupPosition;
			}

			@Override
			public void onClick(View v) {
				mFileList.get(mGroupPosition).toggle();
				int childrenCount = mFileList.get(mGroupPosition).getChildrenCount();
				boolean groupIsChecked = mFileList.get(mGroupPosition).isChecked;
				for (int i = 0; i < childrenCount; i++)
					mFileList.get(mGroupPosition).getChildItem(i).isChecked = groupIsChecked;
				notifyDataSetChanged();
				addFileToManager(mGroupPosition);
			}
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
				View convertView, ViewGroup parent) {
			MediaInfo mediaInfo = (MediaInfo) getChild(groupPosition, childPosition);
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item_mediafile_child, parent, false);
				holder = new ViewHolder();
				holder.mChlidCheckBox = (CheckBox) convertView.findViewById(R.id.cb_mediafile);
				holder.mFileImg = (ImageView) convertView.findViewById(R.id.img_mediafile);
				holder.mFileNameTv = (TextView) convertView.findViewById(R.id.tv_meidafile_name);
				holder.mFileLenthTv = (TextView) convertView.findViewById(R.id.tv_mediafile_lenth);
				holder.mFileSizeTv = (TextView) convertView.findViewById(R.id.tv_mediafile_size);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			if (mediaInfo.fileType == FileType.MUSIC) {
				holder.mFileImg.setImageResource(R.drawable.img_music_default);
			} else if (mImageFetcher != null) {
				mImageFetcher.loadImage(mediaInfo.path, holder.mFileImg);
			}

			holder.mFileNameTv.setText(mediaInfo.name);
			long tempTime = mediaInfo.length;
			String hms = ValueConvertUtil.formatMilSToHMS(tempTime);
			if (!TextUtils.isEmpty(hms)) {
				holder.mFileLenthTv.setText(mContext.getString(R.string.file_length, hms));
			}
			holder.mFileSizeTv.setText(mContext.getString(R.string.file_size,
					Formatter.formatFileSize(mContext, mediaInfo.size)));
			holder.mChlidCheckBox.setChecked(mediaInfo.isChecked);
			holder.mChlidCheckBox.setOnClickListener(new ChildCheckBoxClick(Integer
					.valueOf(groupPosition), Integer.valueOf(childPosition)));
			if (mediaInfo.isChecked) {
				holder.mChlidCheckBox.setVisibility(View.VISIBLE);
			} else {
				holder.mChlidCheckBox.setVisibility(View.GONE);
			}
			return convertView;
		}

		class ChildCheckBoxClick implements OnClickListener {
			private int mGroupPosition;
			private int mChildPosition;

			ChildCheckBoxClick(int groupPosition, int childPosition) {
				this.mGroupPosition = groupPosition;
				this.mChildPosition = childPosition;
			}

			public void onClick(View v) {
				mFileList.get(mGroupPosition).getChildItem(mChildPosition).toggle();
				int childrenCount = mFileList.get(mGroupPosition).getChildrenCount();
				boolean childrenAllIsChecked = true;
				for (int i = 0; i < childrenCount; i++) {
					if (!mFileList.get(mGroupPosition).getChildItem(i).isChecked)
						childrenAllIsChecked = false;
				}
				mFileList.get(mGroupPosition).isChecked = childrenAllIsChecked;
				notifyDataSetChanged();
				addFileToManager(mGroupPosition);
			}
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

	}

	public class GroupInfo {
		public String name;
		public boolean isChecked;
		public List<MediaInfo> childList = new ArrayList<MediaInfo>();

		public void toggle() {
			this.isChecked = !this.isChecked;
		}

		public void addChildrenItem(MediaInfo child) {
			childList.add(child);
		}

		public int getChildrenCount() {
			return childList.size();
		}

		public MediaInfo getChildItem(int index) {
			return childList.get(index);
		}

	}

	public class MediaInfo {
		public String name;
		public String path;
		public long length;
		public long size;
		public long date;
		public int fileType;
		private boolean isChecked;

		public void toggle() {
			this.isChecked = !this.isChecked;
		}

	}

	public static class ViewHolder {
		CheckBox mChlidCheckBox;
		ImageView mFileImg;
		TextView mFileNameTv;
		TextView mFileLenthTv;
		TextView mFileSizeTv;

		TextView mGroupNameTv;
		View mSelectedGroupView;

	}

	@Override
	public void onTransferFilesChangedListener() {
		((MainActivity2) getActivity()).notifyTransferFilesChanged();
		if (mMediaFilesChooseAdapter != null) {
			mMediaFilesChooseAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onTransferFilesCancelled() {
		if (mMediaFilesChooseAdapter != null) {
			for (GroupInfo groupInfo : mMediaFilesList) {
				groupInfo.isChecked = false;
				for (MediaInfo mediaInfo : groupInfo.childList) {
					mediaInfo.isChecked = false;
				}
			}
		}
		onTransferFilesChangedListener();
	}

}
