/*
 * Copyright (C) 2013 Paul Burke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.m15.app.android.gotransfer.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.FileObserver;
import android.support.v4.content.AsyncTaskLoader;
import cn.m15.app.android.gotransfer.enity.TransferFile;
import cn.m15.app.android.gotransfer.net.ipmsg2.MessageConst.FileType;
import cn.m15.app.android.gotransfer.ui.activity.SettingsActivity;

/**
 * Loader that returns a list of Files in a given file path.
 * 
 * @version 2013-12-11
 * @author paulburke (ipaulpro)
 */
public class FileLoader extends AsyncTaskLoader<List<TransferFile>> {

	private static final int FILE_OBSERVER_MASK = FileObserver.CREATE
			| FileObserver.DELETE | FileObserver.DELETE_SELF
			| FileObserver.MOVED_FROM | FileObserver.MOVED_TO
			| FileObserver.MODIFY | FileObserver.MOVE_SELF;

	private FileObserver mFileObserver;

	private List<TransferFile> mData;
	private String mPath;

	public FileLoader(Context context, String path) {
		super(context);
		this.mPath = path;
	}

	@Override
	public List<TransferFile> loadInBackground() {

        ArrayList<File> list = new ArrayList<File>();

        // Current directory File instance
        final File pathDir = new File(mPath);

        SharedPreferences pre = getContext().getSharedPreferences(SettingsActivity.SETTINGS, Context.MODE_PRIVATE);
        boolean showHiddenFile = pre.getBoolean(SettingsActivity.SHOW_HIDDEN_FILES, false);
        	
        // List file in this directory with the directory filter
        final File[] dirs = pathDir.listFiles(showHiddenFile ? FileUtil.sDirFilterWithHidden : FileUtil.sDirFilter);
        if (dirs != null) {
            // Sort the folders alphabetically
            Arrays.sort(dirs, FileUtil.sComparator);
            // Add each folder to the File list for the list adapter
            for (File dir : dirs)
                list.add(dir);
        }

        // List file in this directory with the file filter
        final File[] files = pathDir.listFiles(showHiddenFile ? FileUtil.sFileFilterWithHidden : FileUtil.sFileFilter);
        if (files != null) {
            // Sort the files alphabetically
            Arrays.sort(files, FileUtil.sComparator);
            // Add each file to the File list for the list adapter
            for (File file : files)
                list.add(file);
        }

        return getTransfileList(list);
	}
	
	private ArrayList<TransferFile> getTransfileList(ArrayList<File> files) {
		ArrayList<TransferFile> tranferFiles = new ArrayList<TransferFile>();
		if (files == null) {
			return tranferFiles;
		}
		for (File file : files) {
			TransferFile transferFile = new TransferFile();
			if (file.isDirectory()) {
				transferFile.fileType = FileType.DIR;	
			} else {
				transferFile.fileType = FileUtil.getFileType(file.getName());
			}
			transferFile.name = file.getName();
			transferFile.path = file.getAbsolutePath();
			transferFile.size = FileUtil.getSize(file);
			tranferFiles.add(transferFile);
		}
		return tranferFiles;
	}

	@Override
	public void deliverResult(List<TransferFile> data) {
		if (isReset()) {
			onReleaseResources(data);
			return;
		}

		List<TransferFile> oldData = mData;
		mData = data;

		if (isStarted())
			super.deliverResult(data);

		if (oldData != null && oldData != data)
			onReleaseResources(oldData);
	}

	@Override
	protected void onStartLoading() {
		if (mData != null)
			deliverResult(mData);

		if (mFileObserver == null) {
			mFileObserver = new FileObserver(mPath, FILE_OBSERVER_MASK) {
				@Override
				public void onEvent(int event, String path) {
					onContentChanged();
				}
			};
		}
		mFileObserver.startWatching();

		if (takeContentChanged() || mData == null)
			forceLoad();
	}

	@Override
	protected void onStopLoading() {
		cancelLoad();
	}

	@Override
	protected void onReset() {
		onStopLoading();

		if (mData != null) {
			onReleaseResources(mData);
			mData = null;
		}
	}

	@Override
	public void onCanceled(List<TransferFile> data) {
		super.onCanceled(data);

		onReleaseResources(data);
	}

	protected void onReleaseResources(List<TransferFile> data) {

		if (mFileObserver != null) {
			mFileObserver.stopWatching();
			mFileObserver = null;
		}
	}
}