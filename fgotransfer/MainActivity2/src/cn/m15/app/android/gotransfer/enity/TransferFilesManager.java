package cn.m15.app.android.gotransfer.enity;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class TransferFilesManager {
	private static TransferFilesManager sInstance;
	private Map<String, TransferFile> mTransferFilesMap;
	
	private TransferFilesManager() {
		mTransferFilesMap = new LinkedHashMap<String, TransferFile>();
	}
	
	public static TransferFilesManager getInstance() {
		if (sInstance == null) {
			sInstance = new TransferFilesManager();			
		}
		return sInstance;
	}
	
	public void put(String path, TransferFile file) {
		File realFile = new File(file.path);
		if (realFile.exists() && realFile.isDirectory()) {
			ArrayList<String> duplicateFiles = new ArrayList<String>();
			for (TransferFile f : mTransferFilesMap.values()) {
				if (f.path.startsWith(realFile.getAbsolutePath())) {
					duplicateFiles.add(f.path);
				}
			}
			for (String string : duplicateFiles) {
				mTransferFilesMap.remove(string);
			}
			duplicateFiles = null;
		}
		mTransferFilesMap.put(path, file);
	}
	
	public TransferFile get(String path) {
		return mTransferFilesMap.get(path);
	}
	
	public TransferFile remove(String path) {
		return mTransferFilesMap.remove(path);
	}
	
	public int size() {
		return mTransferFilesMap.size();
	}
	
	public void clear() {
		mTransferFilesMap.clear();
	}
	
	public boolean isFileSelected(String path) {
		return get(path) != null;
	}
	
	public ArrayList<TransferFile> toList() {
		ArrayList<TransferFile> transfersList = new ArrayList<TransferFile>();
		for (TransferFile file : mTransferFilesMap.values()) {
			transfersList.add(file);
		} 
		return transfersList;
	}
	
	
}
