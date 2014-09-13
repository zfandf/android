package cn.m15.app.android.gotransfer.ui.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import cn.m15.app.android.gotransfer.R;
import cn.m15.app.android.gotransfer.enity.TransferFile;
import cn.m15.app.android.gotransfer.enity.TransferFilesManager;
import cn.m15.app.android.gotransfer.net.ipmsg2.MessageConst.FileType;
import cn.m15.app.android.gotransfer.ui.activity.MainActivity2;
import cn.m15.app.android.gotransfer.ui.activity.MainActivity2.TransferFilesChangeListener;
import cn.m15.app.android.gotransfer.ui.adapter.FileListAdapter;
import cn.m15.app.android.gotransfer.ui.widget.ClickableTextView;
import cn.m15.app.android.gotransfer.ui.widget.ClickableTextView.ClickableWord;
import cn.m15.app.android.gotransfer.utils.FileLoader;
import cn.m15.app.android.gotransfer.utils.FileUtil;

public class FileChooserFragment extends ListFragment implements
		LoaderCallbacks<List<TransferFile>>, OnKeyListener, TransferFilesChangeListener {

	public static final String TAG = "FileChooserFragment";
	private static final int LOADER_ID = 0;

	private ClickableTextView mCTvCurrentPath;
	private ProgressBar mProgressBar;
	private FileListAdapter mAdapter;

	private Stack<File> mPathStack;
	private List<TransferFile> mStoragesList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPathStack = new Stack<File>();
		mStoragesList = new ArrayList<TransferFile>();
		initStorages();
		mAdapter = new FileListAdapter(getActivity(), this, mStoragesList);
	}
	
	private void initStorages() {
		String[] paths = FileUtil.getVolumePaths(getActivity());
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
		View view = inflater.inflate(R.layout.fragment_file_chooser, null);
		view.setFocusableInTouchMode(true);
		view.requestFocus();
		view.setOnKeyListener(this);
		mCTvCurrentPath = (ClickableTextView) view.findViewById(R.id.tv_file_path);
		mProgressBar = (ProgressBar) view.findViewById(R.id.pb_loading);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setListAdapter(mAdapter);
		if (!mPathStack.isEmpty()) {
			getLoaderManager().initLoader(LOADER_ID, null, this);
		}

		if (mStoragesList != null && mStoragesList.size() > 1) {
			mProgressBar.setVisibility(View.GONE);
			mAdapter.setListItems(mStoragesList);
		}
	}

	private void updateCurrentDir(File file) {
		mCTvCurrentPath.setVisibility(View.VISIBLE);
		mCTvCurrentPath.setHighlightColor(Color.TRANSPARENT);
		String currentPath = "";
		int count = mStoragesList.size();
		for (int i = 0; i < count; i++) {
			String filePath = file.getAbsolutePath();
			String storagePath = mStoragesList.get(i).path;
			int index = filePath.indexOf(storagePath);
			if (index != -1) {
				currentPath = filePath.replace(storagePath,
						new File(mStoragesList.get(i).path).getName());
				break;
			}

		}
		final String[] words = currentPath.split(File.separator);
		int size = words.length;
		List<ClickableWord> clickable = new ArrayList<ClickableWord>();
		for (int i = 0; i < size; i++) {
			clickable.add(new ClickableWord(words[i], new ClickWordSpan(words[i])));
		}
		mCTvCurrentPath.setTextWithClickableWords(currentPath, clickable);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		FileListAdapter adapter = (FileListAdapter) l.getAdapter();
		TransferFile file = adapter.getItem(position);
		if (file.fileType == FileType.DIR
				&& !TransferFilesManager.getInstance().isFileSelected(file.path)) {
			mPathStack.push(new File(file.path));
			restartLoad();
		}
	}
	
	private void restartLoad() {
		getListView().setVisibility(View.GONE);
		mProgressBar.setVisibility(View.VISIBLE);
		getLoaderManager().restartLoader(LOADER_ID, null, this);
	}

	@Override
	public Loader<List<TransferFile>> onCreateLoader(int arg0, Bundle arg1) {
		return new FileLoader(getActivity(), mPathStack.lastElement().getAbsolutePath());
	}

	@Override
	public void onLoadFinished(Loader<List<TransferFile>> arg0, List<TransferFile> arg1) {
		mProgressBar.setVisibility(View.GONE);
		getListView().setVisibility(View.VISIBLE);
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
					mCTvCurrentPath.setVisibility(View.GONE);
					mAdapter.setListItems(mStoragesList);
					return true;
				}
			}
		}
		return false;
	}

	class ClickWordSpan extends ClickableSpan {

		private String mWord;

		public ClickWordSpan(String word) {
			mWord = word;
		}

		@Override
		public void updateDrawState(TextPaint ds) {
			ds.setUnderlineText(false);
		}

		@Override
		public void onClick(View widget) {
			Log.d(TAG, "click word is >>> " + mWord);
			for (TransferFile transferFile : mStoragesList) {
				File file = new File(transferFile.path);
				if (file.getName().equals(mWord)) {
					mWord = file.getAbsolutePath();
					break;
				}
			}
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
			backToClickedDirectory(mWord);
		}

	}

	private void backToClickedDirectory(String clickDirectory) {
		String currentDir = mPathStack.lastElement().getAbsolutePath();
		if (currentDir.endsWith(clickDirectory)) {
			if (isRootDir(currentDir) && mStoragesList.size() > 1) {
				mPathStack.clear();
				mAdapter.setListItems(mStoragesList);
				mCTvCurrentPath.setVisibility(View.GONE);
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
		((MainActivity2) getActivity()).notifyTransferFilesChanged();
	}

	@Override
	public void onTransferFilesCancelled() {
		mAdapter.notifyDataSetChanged();
	}

}
