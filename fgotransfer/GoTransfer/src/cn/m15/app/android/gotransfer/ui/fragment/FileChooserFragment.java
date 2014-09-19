package cn.m15.app.android.gotransfer.ui.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import cn.m15.app.android.gotransfer.Const;
import cn.m15.app.android.gotransfer.R;
import cn.m15.app.android.gotransfer.ui.activity.MainActivity;
import cn.m15.app.android.gotransfer.ui.activity.MainActivity.TransferFilesChangeListener;
import cn.m15.app.android.gotransfer.ui.adapter.FileListAdapter;
import cn.m15.app.android.gotransfer.ui.widget.HorizontalListView;
import cn.m15.app.android.gotransfer.utils.FileLoader;
import cn.m15.app.android.gotransfer.utils.FileUtil;
import cn.m15.gotransfer.sdk.entity.TransferFile;
import cn.m15.gotransfer.sdk.entity.TransferFileManager;
import cn.m15.gotransfer.sdk.net.ipmsg.MessageConst.FileType;
import cn.m15.gotransfer.sdk.utils.SdkFileUtil;

public class FileChooserFragment extends ListFragment implements
		LoaderCallbacks<List<TransferFile>>, OnKeyListener, TransferFilesChangeListener {

	public static final String TAG = "FileChooserFragment";

	private ProgressBar mProgressBar;
	private FileListAdapter mAdapter;

	private Stack<File> mPathStack;
	private List<TransferFile> mStoragesList;
	private HorizontalListView mHLvDirs;
	private View mView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPathStack = new Stack<File>();
		mStoragesList = new ArrayList<TransferFile>();
		initStorages();
		mAdapter = new FileListAdapter(getActivity(), this, mStoragesList);
	}

	private void initStorages() {
		String[] paths = SdkFileUtil.getVolumePaths(getActivity());
		if (paths != null && paths.length > 0) {
			int count = paths.length;
			for (int i = 0; i < count; i++) {
				Log.d(TAG, "storage path is >> " + paths[i]);
				TransferFile file = new TransferFile();
				file.path = paths[i];
				file.fileType = FileType.DIR;
				mStoragesList.add(file);
			}
			if (count == 1) {
				mPathStack.push(new File(mStoragesList.get(0).path));
			}
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_file_chooser, null);
		mView.setFocusableInTouchMode(true);
		mView.requestFocus();
		mView.setOnKeyListener(this);
		mHLvDirs = (HorizontalListView) mView.findViewById(R.id.lv_dirs);
		mHLvDirs.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String word = (String) ((HorizontalListView) parent).getAdapter().getItem(position);
				backToClickedDirectory(word);
			}
		});
		mProgressBar = (ProgressBar) mView.findViewById(R.id.pb_loading);
		return mView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (getListView().getFooterViewsCount() == 0) {
			View view = getActivity().getLayoutInflater().inflate(R.layout.item_blank, null);
			view.setLayoutParams(new AbsListView.LayoutParams(
					AbsListView.LayoutParams.MATCH_PARENT, (int) TypedValue.applyDimension(
							TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics())));

			getListView().addFooterView(view);
		}
		setListAdapter(mAdapter);
		if (!mPathStack.isEmpty()) {
			getLoaderManager().initLoader(Const.LOADER_FILES, null, this);
		}

		if (mStoragesList != null && mStoragesList.size() > 1) {
			mProgressBar.setVisibility(View.GONE);
			mAdapter.setListItems(mStoragesList);
		} else {
			mProgressBar.setVisibility(View.GONE);
		}
	}

	private void updateCurrentDir(File file) {
		String currentPath = "";
		int count = mStoragesList.size();
		for (int i = 0; i < count; i++) {
			String filePath = file.getAbsolutePath();
			String storagePath = mStoragesList.get(i).path;
			int index = filePath.indexOf(storagePath);
			if (index != -1) {
				boolean isInternal = FileUtil.isInternalStoragePath(getActivity(), storagePath);
				currentPath = filePath.replace(
						storagePath,
						isInternal ? getString(R.string.internal_storage) : new File(mStoragesList
								.get(i).path).getName());
				break;
			}

		}
		final String[] words = currentPath.split(File.separator);
		// if (words[0].endsWith("0")) {
		// words[0] = getString(R.string.internal_storage);
		// }
		mHLvDirs.setVisibility(View.VISIBLE);
		mHLvDirs.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.item_dir_list,
				R.id.tv_dir, words));
		mHLvDirs.post(new Runnable() {

			@Override
			public void run() {
				mHLvDirs.scrollTo(Integer.MAX_VALUE);
			}
		});

		// int size = words.length;
		// List<ClickableWord> clickable = new ArrayList<ClickableWord>();
		// for (int i = 0; i < size; i++) {
		// clickable.add(new ClickableWord(words[i], new
		// ClickWordSpan(words[i])));
		// }
		// mCTvCurrentPath.setTextWithClickableWords(currentPath, clickable);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		FileListAdapter adapter = null;
		if (l.getAdapter() instanceof HeaderViewListAdapter) {
			adapter = (FileListAdapter) ((HeaderViewListAdapter) l.getAdapter())
					.getWrappedAdapter();
		} else {
			adapter = (FileListAdapter) l.getAdapter();
		}
		if (adapter.getCount() <= position) {
			return;
		}

		TransferFile file = adapter.getItem(position);
		if (file.fileType == FileType.DIR
				&& !TransferFileManager.getInstance().isFileSelected(file.path)) {
			mPathStack.push(new File(file.path));
			restartLoad();
		} else {
			FileListAdapter.ViewHolder holder = (FileListAdapter.ViewHolder) v.getTag();
			if (holder != null) {
				boolean isChecked = holder.mCheckBox.isChecked();
				holder.mCheckBox.setChecked(!isChecked);
			}
		}
		mView.requestFocus();
	}

	public void restartLoad() {
		getListView().setVisibility(View.GONE);
		mProgressBar.setVisibility(View.VISIBLE);
		getLoaderManager().restartLoader(Const.LOADER_FILES, null, this);
	}

	@Override
	public Loader<List<TransferFile>> onCreateLoader(int arg0, Bundle arg1) {
		if (mPathStack == null || mPathStack.isEmpty() || mPathStack.lastElement() == null) {
			initStorages();
		}
		if (!mPathStack.isEmpty()) {
			return new FileLoader(getActivity(), mPathStack.lastElement().getAbsolutePath());
		}
		Log.d(TAG, "create loader >>> ");
		return new FileLoader(getActivity(), null);
	}

	@Override
	public void onLoadFinished(Loader<List<TransferFile>> arg0, List<TransferFile> arg1) {
		Log.d(TAG, "loader finish >>> ");
		mProgressBar.setVisibility(View.GONE);
		getListView().setVisibility(View.VISIBLE);
		if (arg1 == null)
			return;
		if (!mPathStack.isEmpty()) {
			updateCurrentDir(mPathStack.lastElement());
		}
		mAdapter.setListItems(arg1);
		setSelection(0);
	}

	@Override
	public void onLoaderReset(Loader<List<TransferFile>> arg0) {
		mAdapter.clear();
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		Log.d(TAG, "keyCode: " + keyCode);
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
			if (mPathStack.size() > 1) {
				mPathStack.pop();
				restartLoad();
				return true;
			} else if (mPathStack.size() == 1) {
				mPathStack.pop();
				if (mStoragesList.size() == 1) {
					return false;
				} else {
					mHLvDirs.setVisibility(View.GONE);
					mAdapter.setListItems(mStoragesList);
					return true;
				}
			}
		}
		return false;
	}

	// class ClickWordSpan extends ClickableSpan {
	//
	// private String mWord;
	//
	// public ClickWordSpan(String word) {
	// mWord = word;
	// }
	//
	// @Override
	// public void updateDrawState(TextPaint ds) {
	// ds.setUnderlineText(false);
	// }
	//
	// @Override
	// public void onClick(View widget) {
	// Log.d(TAG, "click word is >>> " + mWord);
	// for (TransferFile transferFile : mStoragesList) {
	// File file = new File(transferFile.path);
	// if (file.getName().equals(mWord)) {
	// mWord = file.getAbsolutePath();
	// break;
	// }
	// }
	// if (mWord.startsWith("手机存储")) {
	// if (mWord.equals("手机存储")) {
	// mWord = mStoragesList.get(0).getAbsolutePath();
	// } else {
	// String strIndex = mWord.substring("手机存储".length(),
	// mWord.length());
	// int index = Integer.valueOf(strIndex);
	// mWord = mStoragesList.get(index).getAbsolutePath();
	// }
	// }
	// backToClickedDirectory(mWord);
	// }
	//
	// }

	private void backToClickedDirectory(String word) {
		String clickDirectory = word;
		for (TransferFile transferFile : mStoragesList) {
			File file = new File(transferFile.path);
			if ((FileUtil.isInternalStoragePath(getActivity(), file.getAbsolutePath()) && word
					.equals(getString(R.string.internal_storage))) || file.getName().equals(word)) {
				clickDirectory = file.getAbsolutePath();
				break;
			}
		}
		if (mPathStack.isEmpty()) {
			return;
		}
		String currentDir = mPathStack.lastElement().getAbsolutePath();
		if (currentDir.endsWith(clickDirectory)) {
			if (isRootDir(currentDir) && mStoragesList.size() > 1) {
				mPathStack.clear();
				mAdapter.setListItems(mStoragesList);
				mHLvDirs.setVisibility(View.GONE);
			}
		} else {
			int index = currentDir.indexOf(clickDirectory);
			String dir = currentDir.substring(0, index + clickDirectory.length());
			Stack<File> temp = new Stack<File>();
			for (File file : mPathStack) {
				temp.push(file);
				if (file.getAbsolutePath().equals(dir)) {
					break;
				}
			}
			mPathStack.clear();
			mPathStack.addAll(temp);
			restartLoad();
		}
	}

	private boolean isRootDir(String path) {
		if (mStoragesList.size() > 0) {
			for (TransferFile file : mStoragesList) {
				if (path.equals(file.path)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void onTransferFilesChangedListener() {
		((MainActivity) getActivity()).notifyTransferFilesChanged();
	}

	@Override
	public void onTransferFilesCancelled() {
		mAdapter.notifyDataSetChanged();
	}

}
