package cn.m15.app.android.gotransfer.ui.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import cn.m15.app.android.gotransfer.Const;
import cn.m15.app.android.gotransfer.R;
import cn.m15.app.android.gotransfer.ui.activity.MainActivity;
import cn.m15.app.android.gotransfer.ui.activity.MainActivity.TransferFilesChangeListener;
import cn.m15.app.android.gotransfer.ui.widget.FloatingGroupExpandableListView;
import cn.m15.app.android.gotransfer.ui.widget.PictureCursorTreeAdapter;
import cn.m15.app.android.gotransfer.ui.widget.WrapperExpandableListAdapter;
import cn.m15.app.android.gotransfer.utils.ImageUtil;
import cn.m15.app.android.gotransfer.utils.images.LocalImageFetcher;
import cn.m15.app.android.gotransfer.utils.images.Utils;
import cn.m15.gotransfer.sdk.entity.TransferFile;
import cn.m15.gotransfer.sdk.entity.TransferFileManager;
import cn.m15.gotransfer.sdk.entity.TransferFilePool;
import cn.m15.gotransfer.sdk.net.ipmsg.MessageConst.FileType;
import cn.m15.gotransfer.sdk.utils.SdkFileUtil;

public class PictureChooserFragment extends Fragment implements LoaderCallbacks<Cursor>,
		TransferFilesChangeListener {
	public static final String TAG = "picture_chooser";

	private static final String[] GROUP_PROJECTION = new String[] {
			MediaStore.Images.Media.BUCKET_ID + " AS _id",
			MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATA };

	private static final String[] CHILDREN_PROJECTION = { MediaStore.Images.Media._ID,
			MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.DISPLAY_NAME,
			MediaStore.Images.Media.SIZE, MediaStore.Images.Media.DATA,
			MediaStore.Images.Media.MIME_TYPE };

	public static final int GROUP_BUCKET_ID = 0;
	public static final int GROUP_BUCKET_NAME = 1;
	public static final int GROUP_BUCKET_PATH = 2;

	public static final int CHILDREN_PIC_ID = 0;
	public static final int CHILDREN_BUCKET_ID = 1;
	public static final int CHILDREN_DISPLAY_NAME = 2;
	public static final int CHILDREN_SIZE = 3;
	public static final int CHILDREN_PATH = 4;
	public static final int CHILDREN_MIME_TYPE = 5;

	private ProgressBar mProgressbar;

	private FloatingGroupExpandableListView mPictureList;
	private PictureChooserAdapter mPictureChooserAdapter;
	protected TransferFilePool mPool;
	private TransferFileManager mTransferFilesManager;

	private int mImageSize;
	protected LocalImageFetcher mImageFetcher;

	protected HashMap<String, Boolean> mGroupCheckMap;
	private HashMap<String, Integer> mChildrenCountMap;
	private HashMap<String, Integer> mCheckCountMap;

	private String[] mCameraPaths;
	private String mDefaultCameraPath;

	private int mExpandGroupPos;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DisplayMetrics dm = getActivity().getResources().getDisplayMetrics();
		mImageSize = dm.widthPixels
				- (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, dm);
		mImageSize /= 4;
		mImageFetcher = ImageUtil.createLocalImageFetcher(getActivity(), mImageSize);
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
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_picture_chooser, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mPool = new TransferFilePool(5);
		mTransferFilesManager = TransferFileManager.getInstance();
		mGroupCheckMap = new HashMap<String, Boolean>();
		mChildrenCountMap = new HashMap<String, Integer>();
		mCheckCountMap = new HashMap<String, Integer>();

		mProgressbar = (ProgressBar) getView().findViewById(R.id.progress_bar);
		mPictureList = (FloatingGroupExpandableListView) getView().findViewById(
				R.id.lv_picture_list);
		mPictureList.setOnScrollListener(new AbsListView.OnScrollListener() {
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
		mPictureList.addFooterView(mFooterView);
		mPictureChooserAdapter = new PictureChooserAdapter(getActivity());
		WrapperExpandableListAdapter wrapperExpandableListAdapter = new WrapperExpandableListAdapter(
				mPictureChooserAdapter);
		mPictureList.setAdapter(wrapperExpandableListAdapter);

		File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
		if (dcim != null) {
			mDefaultCameraPath = dcim.getAbsolutePath();
			mDefaultCameraPath += File.separator + "Camera";
			Log.d(TAG, "default dcim path >>> " + mDefaultCameraPath);
		}

		initCameraPaths();
		getLoaderManager().initLoader(Const.LOADER_CAMERA, null, this);
	}

	private void initCameraPaths() {
		mCameraPaths = SdkFileUtil.getVolumePaths(getActivity());
		if (mCameraPaths != null && mCameraPaths.length > 0) {
			boolean hasDefaultCameraPath = false;
			for (int i = 0; i < mCameraPaths.length; i++) {
				mCameraPaths[i] = mCameraPaths[i] + "/DCIM/Camera";
				if (mCameraPaths[i].equalsIgnoreCase(mDefaultCameraPath)) {
					hasDefaultCameraPath = true;
				}
			}
			if (hasDefaultCameraPath) {
				int length = mCameraPaths.length;
				String[] newCameraPaths = new String[length + 1];
				newCameraPaths[length] = mDefaultCameraPath;
				System.arraycopy(mCameraPaths, 0, newCameraPaths, 0, mCameraPaths.length);
			}
		} else if (mDefaultCameraPath != null) {
			mCameraPaths = new String[] { mDefaultCameraPath };
		}
	}
	
	public void restartLoader() {
		mProgressbar.setVisibility(View.VISIBLE);
		mPictureList.setVisibility(View.GONE);
		getLoaderManager().restartLoader(Const.LOADER_CAMERA, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				GROUP_PROJECTION, "0==0) GROUP BY (" + MediaStore.Images.Media.BUCKET_ID, null,
				MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		Log.e(TAG, "onLoadFinished:" + c);
		if (c != null) {
			try {
				ArrayList<String[]> dataList = new ArrayList<String[]>();

				String[] columnNames = c.getColumnNames();
				MatrixCursor matrixCursor = new MatrixCursor(columnNames, c.getCount());
				while (c.moveToNext()) {
					String id = c.getString(GROUP_BUCKET_ID);
					String name = c.getString(GROUP_BUCKET_NAME);
					String path = c.getString(GROUP_BUCKET_PATH);
					boolean hasInsert = false;
					if (mCameraPaths != null && mCameraPaths.length > 0) {
						for (String cameraPath : mCameraPaths) {
							if (path.startsWith(cameraPath)) {
								hasInsert = true;
								dataList.add(0, new String[] { id, name, path });
								break;
							}
						}
						if (!hasInsert) {
							dataList.add(new String[] { id, name, path });
						}
					} else {
						dataList.add(new String[] { id, name, path });
					}
				}

				for (String[] rowData : dataList) {
					matrixCursor.addRow(rowData);
				}

				mPictureChooserAdapter.changeCursor(matrixCursor);

				mPictureList.expandGroup(0);
				mExpandGroupPos = 0;

				mPictureList.setOnGroupExpandListener(new OnGroupExpandListener() {

					@Override
					public void onGroupExpand(int groupPosition) {
						if (groupPosition != mExpandGroupPos) {
							mPictureList.collapseGroup(mExpandGroupPos);
						}
						mExpandGroupPos = groupPosition;
					}
				});
			} finally {
				c.close();
			}
		}
		mProgressbar.setVisibility(View.GONE);
		mPictureList.setVisibility(View.VISIBLE);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		Log.e(TAG, "onLoaderReset:");
		mPictureChooserAdapter.changeCursor(null);
	}

	@Override
	public void onDestroy() {
		getLoaderManager().destroyLoader(Const.LOADER_CAMERA);
		super.onDestroy();
	}

	@Override
	public void onTransferFilesChangedListener() {
		if (mPictureChooserAdapter != null) {
			mPictureChooserAdapter.notifyDataSetChanged(false);
		}
		((MainActivity) getActivity()).notifyTransferFilesChanged();
	}

	@Override
	public void onTransferFilesCancelled() {
		mGroupCheckMap.clear();
		mChildrenCountMap.clear();
		mCheckCountMap.clear();
		onTransferFilesChangedListener();
	}

	public class PictureChooserAdapter extends PictureCursorTreeAdapter implements
			View.OnClickListener {
		private Context mContext;
		private LayoutInflater mInlater;

		public PictureChooserAdapter(Context context) {
			super(null, context);
			mContext = context;
			mInlater = LayoutInflater.from(context);
		}

		@Override
		protected void bindChildView(View view, Context context, Cursor cursor,
				boolean isLastChild, int groupPosition) {
			int childPosition = cursor.getPosition();
			if (childPosition % 4 != 0) {
				view.setVisibility(View.GONE);
				return;
			}

			view.setVisibility(View.VISIBLE);
			ViewHolder holder = (ViewHolder) view.getTag();
			setChildView(holder.mCloView, cursor, groupPosition);
			if (cursor.moveToNext()) {
				setChildView(holder.mClo2View, cursor, groupPosition);
			} else {
				holder.mClo2View.setVisibility(View.INVISIBLE);
			}
			if (cursor.moveToNext()) {
				setChildView(holder.mClo3View, cursor, groupPosition);
			} else {
				holder.mClo3View.setVisibility(View.INVISIBLE);
			}
			if (cursor.moveToNext()) {
				setChildView(holder.mClo4View, cursor, groupPosition);
			} else {
				holder.mClo4View.setVisibility(View.INVISIBLE);
			}
		}

		private void setChildView(View view, Cursor cursor, int groupPosition) {
			view.setTag(cursor.getString(CHILDREN_PIC_ID));
			view.setVisibility(View.VISIBLE);
			ImageView imageView = (ImageView) view.findViewById(R.id.img_picture);
			imageView.getLayoutParams().width = mImageSize;
			imageView.getLayoutParams().height = mImageSize;
			String path = cursor.getString(CHILDREN_PATH);
			mImageFetcher.loadImage(path, imageView);

			boolean selected = (mTransferFilesManager.get(path) != null);

			view.findViewById(R.id.check_box).setSelected(selected);

			view.findViewById(R.id.shade).setVisibility(selected ? View.VISIBLE : View.GONE);
		}

		@Override
		public void onClick(View v) {
			String key = (String) v.getTag(); // pic id
			Cursor c = MediaStore.Images.Media.query(mContext.getContentResolver(),
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, CHILDREN_PROJECTION,
					MediaStore.Images.Media._ID + " = " + key, null);
			if (c != null && c.moveToNext()) {
				try {
					String path = c.getString(CHILDREN_PATH);
					String childrenBucketId = c.getString(CHILDREN_BUCKET_ID);
					if (!new File(path).exists()) {
						Toast.makeText(mContext, R.string.file_deleted, Toast.LENGTH_SHORT).show();
						return;
					}

					boolean selected = mTransferFilesManager.get(path) == null;

					v.findViewById(R.id.check_box).setSelected(selected);
					v.findViewById(R.id.shade).setVisibility(selected ? View.VISIBLE : View.GONE);
					if (selected) {
						String name = c.getString(CHILDREN_DISPLAY_NAME);
						long size = c.getLong(CHILDREN_SIZE);
						TransferFile item = mPool.newTransferFiles();
						item.path = path;
						item.fileType = FileType.PICTURE;
						item.name = name;
						item.size = size;
						mTransferFilesManager.put(item.path, item);
						if (mCheckCountMap.get(childrenBucketId) != null) {
							mCheckCountMap.put(childrenBucketId,
									mCheckCountMap.get(childrenBucketId) + 1);
						} else {
							mCheckCountMap.put(childrenBucketId, 1);
						}
					} else {
						TransferFile removedFile = mTransferFilesManager.remove(path);
						mPool.free(removedFile);
						if (mCheckCountMap.get(childrenBucketId) != null
								&& mCheckCountMap.get(childrenBucketId) > 0) {
							mCheckCountMap.put(childrenBucketId,
									mCheckCountMap.get(childrenBucketId) - 1);
						} else {
							mCheckCountMap.put(childrenBucketId, 0);
						}
					}

					if (mCheckCountMap.get(childrenBucketId) != null
							&& mChildrenCountMap.get(childrenBucketId) != null) {
						if (mCheckCountMap.get(childrenBucketId).equals(
								mChildrenCountMap.get(childrenBucketId))) {
							mGroupCheckMap.put(childrenBucketId, true);
						} else {
							mGroupCheckMap.remove(childrenBucketId);
						}
					}
					notifyDataSetChanged();
					onTransferFilesChangedListener();
				} finally {
					c.close();
				}
			}
		}

		@Override
		protected void bindGroupView(View view, Context context, final Cursor cursor,
				boolean isExpanded) {
			final ViewHolder holder = (ViewHolder) view.getTag();
			final String groupName = cursor.getString(GROUP_BUCKET_NAME);
			final String group_bucket_id = cursor.getString(GROUP_BUCKET_ID);
			ImageView imageView = (ImageView) view.findViewById(R.id.left_icon);
			if (isExpanded) {
				imageView.setImageResource(R.drawable.ic_minus);
			} else {
				imageView.setImageResource(R.drawable.ic_add);
			}
			holder.mGroupIconIv = imageView;
			holder.mGroupNameTv.setText(groupName);

			boolean group_selected = mGroupCheckMap.containsKey(group_bucket_id);
			holder.mGroupCheckAll.setSelected(group_selected);
			holder.mGroupCheckAll.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Cursor childrenCursor = queryPictures(group_bucket_id);
					childrenCursor.moveToFirst();
					childrenCursor.moveToPrevious();
					boolean group_checked = !mGroupCheckMap.containsKey(group_bucket_id);

					holder.mGroupCheckAll.setSelected(group_checked);

					if (group_checked) {
						while (childrenCursor != null & childrenCursor.moveToNext()) {
							TransferFile item = mPool.newTransferFiles();
							item.path = childrenCursor.getString(CHILDREN_PATH);
							item.fileType = FileType.PICTURE;
							item.name = childrenCursor.getString(CHILDREN_DISPLAY_NAME);
							item.size = childrenCursor.getLong(CHILDREN_SIZE);
							mTransferFilesManager.put(item.path, item);
							mGroupCheckMap.put(group_bucket_id, true);
							mCheckCountMap.put(group_bucket_id, childrenCursor.getCount());
						}
					} else {
						while (childrenCursor != null & childrenCursor.moveToNext()) {
							TransferFile removedFile = mTransferFilesManager.remove(childrenCursor
									.getString(CHILDREN_PATH));
							mPool.free(removedFile);
							mGroupCheckMap.remove(group_bucket_id);
							mCheckCountMap.remove(group_bucket_id);
						}
					}
					childrenCursor.close();
					notifyDataSetChanged();
					onTransferFilesChangedListener();
				}
			});
		}

		public Cursor queryPictures(String groupBucketId) {
			Cursor childrenCursor = MediaStore.Images.Media.query(mContext.getContentResolver(),
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, CHILDREN_PROJECTION,
					MediaStore.Images.Media.SIZE + " > 0 AND " + MediaStore.Images.Media.BUCKET_ID
							+ " = " + groupBucketId, MediaStore.Images.Media.DATE_MODIFIED
							+ " DESC");
			if (mChildrenCountMap == null) {
				mChildrenCountMap = new HashMap<String, Integer>();
			}
			if (childrenCursor == null) {
				mChildrenCountMap.put(groupBucketId, 0);
			} else {
				mChildrenCountMap.put(groupBucketId, childrenCursor.getCount());				
			}
			return childrenCursor;
		}

		@Override
		protected Cursor getChildrenCursor(Cursor groupCursor) {
			Cursor childrenCursor = queryPictures(groupCursor.getString(GROUP_BUCKET_ID));
			return childrenCursor;
		}

		@Override
		protected View newChildView(Context context, Cursor cursor, boolean isLastChild,
				ViewGroup parent) {
			View view = mInlater.inflate(R.layout.item_picture_raw4x, parent, false);
			ViewHolder holder = new ViewHolder();
			holder.mCloView = view.findViewById(R.id.include_picture_raw1);
			holder.mClo2View = view.findViewById(R.id.include_picture_raw2);
			holder.mClo3View = view.findViewById(R.id.include_picture_raw3);
			holder.mClo4View = view.findViewById(R.id.include_picture_raw4);
			holder.mCloView.setOnClickListener(this);
			holder.mClo2View.setOnClickListener(this);
			holder.mClo3View.setOnClickListener(this);
			holder.mClo4View.setOnClickListener(this);
			view.setTag(holder);
			return view;
		}

		@Override
		protected View newGroupView(Context context, final Cursor cursor, boolean isExpanded,
				ViewGroup parent) {
			View view = mInlater.inflate(R.layout.item_picture_group2, parent, false);
			ViewHolder holder = new ViewHolder();
			ImageView imageView = (ImageView) view.findViewById(R.id.left_icon);
			if (isExpanded) {
				imageView.setImageResource(R.drawable.ic_minus);
			} else {
				imageView.setImageResource(R.drawable.ic_add);
			}
			holder.mGroupIconIv = imageView;
			holder.mGroupNameTv = (TextView) view.findViewById(R.id.tv_group_name2);
			holder.mGroupCheckAll = view.findViewById(R.id.tv_check_all);
			view.setTag(holder);
			return view;
		}
	}

	static class ViewHolder {
		ImageView mGroupIconIv;
		TextView mGroupNameTv;
		View mGroupCheckAll;
		View mCloView;
		View mClo2View;
		View mClo3View;
		View mClo4View;
	}

}
