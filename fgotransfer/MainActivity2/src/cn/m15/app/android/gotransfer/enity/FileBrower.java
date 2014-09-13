package cn.m15.app.android.gotransfer.enity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import cn.m15.app.android.gotransfer.Const;
import cn.m15.app.android.gotransfer.R;
import cn.m15.app.android.gotransfer.ui.activity.SettingsActivity;
import cn.m15.app.android.gotransfer.utils.FileUtil;

public class FileBrower {
	public static final String TAG = "FileBrower";
	private static String ROOT_DIR = Environment.getExternalStorageDirectory().getPath();
	
	private static final Comparator<FileInfo> ALPH = new Comparator<FileInfo>() {
		
		@Override
		public int compare(FileInfo arg0, FileInfo arg1) {
			if (arg0.isDir && !arg1.isDir) {
				return -1;
			} else if (!arg0.isDir && arg1.isDir) {
				return 1;
			}
			return arg0.name.toLowerCase(Locale.getDefault()).compareTo(arg1.name.toLowerCase(Locale.getDefault()));
		}
		
	};
	
	private ArrayList<String> mPathStack;
	private ArrayList<FileInfo> mDirContent;
	private Context mContext;
	private String mInternalStoragePath;
	private String mVolumePathsStr;
	
	public FileBrower(Context ctx, Bundle saveInstanceState) {
		mContext = ctx;
		String[] volumePaths = FileUtil.getVolumePaths(ctx);
		if (volumePaths != null && volumePaths.length > 1) {
			mVolumePathsStr = Arrays.toString(volumePaths);
			mInternalStoragePath = FileUtil.getInternalStoragePath(mContext, volumePaths);
			Log.d(TAG, "volume paths >>> " + ROOT_DIR + "," + Arrays.toString(volumePaths)+","+Environment.isExternalStorageRemovable());
			ROOT_DIR  = volumePaths[0].substring(0, volumePaths[0].lastIndexOf(File.separator));
		}
		if (saveInstanceState != null) {
			mDirContent = saveInstanceState.getParcelableArrayList("dir_content");
			mPathStack = saveInstanceState.getStringArrayList("path_stack");
		} else {
			mDirContent = new ArrayList<FileInfo>();
			mPathStack = new ArrayList<String>();
		}
	}
	
	public void saveData(Bundle outState) {
		outState.putParcelableArrayList("dir_content", mDirContent);
		outState.putStringArrayList("path_stack", mPathStack);
	}
	
	public String getCurrentDir() {
		return mPathStack.get(0);
	}
	
	public ArrayList<FileInfo> getDirContent() {
		return mDirContent;
	}
	
	public ArrayList<FileInfo> goCurrentDir() {
		if (Const.DEBUG) Log.d(TAG, "goCurrentDir");
		if (mPathStack.size() == 0) {
			mPathStack.add(0, ROOT_DIR);			
		}
		return populateList();
	}
	
	public ArrayList<FileInfo> goPreviousDir() {
		if (Const.DEBUG) Log.d(TAG, "goPreviousDir");
		int size = mPathStack.size();
		
		if (size >= 2) {
			mPathStack.remove(0);
		}
		
		return populateList();
	}
	
	public ArrayList<FileInfo> goNextDir(String path) {
		if (Const.DEBUG) Log.d(TAG, "goNextDir");
		mPathStack.add(0, path);
		return populateList();
	}
	
	private ArrayList<FileInfo> populateList() {
		if(!mDirContent.isEmpty()) {
			mDirContent.clear();
		}
		
		SharedPreferences perferences = mContext.getSharedPreferences(SettingsActivity.SETTINGS, Context.MODE_PRIVATE);
		boolean showHiddenFiles = perferences.getBoolean(SettingsActivity.SHOW_HIDDEN_FILES, false);
		File file = new File(mPathStack.get(0));
		if(file.exists() && file.canRead()) {
			String[] list = file.list();
			int len = list.length;
			String currentDir = getCurrentDir();
			for (int i = 0; i < len; i++) {
				if (TextUtils.isEmpty(list[i])) {
					continue;
				}
				if (!showHiddenFiles && list[i].startsWith(".")) {
					continue;
				}
				File f = new File(currentDir, list[i]);
				FileInfo fileInfo = new FileInfo();
				if (!ROOT_DIR.equals(Environment.getExternalStorageDirectory().getPath()) && ROOT_DIR.equals(f.getParent())) {
					if (mVolumePathsStr != null && !mVolumePathsStr.contains(f.getPath())) {
						continue;
					}
					if (f.getPath().equals(mInternalStoragePath)) {
						fileInfo.name = mContext.getString(R.string.internal_storage);						
					} else {
						fileInfo.name = mContext.getString(R.string.external_storage);	
					}
				} else {
					fileInfo.name = f.getName();					
				}
				fileInfo.path = f.getPath();
				fileInfo.isDir = f.isDirectory();
				fileInfo.size = f.length();
				fileInfo.date = f.lastModified();
				if (!fileInfo.isDir) {
					fileInfo.type = FileUtil.getFileType(list[i]);					
				}
				mDirContent.add(fileInfo);
			}
		}
		
		if (!mDirContent.isEmpty()) {
			Collections.sort(mDirContent, ALPH);
		}
		if (Const.DEBUG) Log.d(TAG, "populate >>> " + file.getPath());
		if (!ROOT_DIR.equals(file.getPath())) {
			FileInfo upFileInfo = new FileInfo();
			upFileInfo.name = mContext.getString(R.string.back_up);
			upFileInfo.path = file.getPath();
			upFileInfo.isDir = true;
			upFileInfo.isBack = true;
			mDirContent.add(0, upFileInfo);
		}
		return mDirContent;
	}

	public boolean ifCurrentDirIsRoot() {
		if (Const.DEBUG) Log.d(TAG, "if current is root >>> " + mPathStack.get(0));
		return ROOT_DIR.equals(mPathStack.get(0));
	}
	
}