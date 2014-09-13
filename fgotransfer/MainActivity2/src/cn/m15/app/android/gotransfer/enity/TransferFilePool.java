package cn.m15.app.android.gotransfer.enity;

import java.util.ArrayList;

import android.util.Log;
 
public class TransferFilePool {
	public int maxSize = 20;
	private ArrayList<TransferFile> freeTransferFiles;
	
	public TransferFilePool (int maxSize) {
		this.maxSize = maxSize;
		freeTransferFiles = new ArrayList<TransferFile>(maxSize);
	}
	
	public TransferFile newTransferFiles() {
		int size = freeTransferFiles.size();
		if (size == 0) {
			return new TransferFile();
		} else {
			return freeTransferFiles.remove(0);
		}
	}
	
	public void free(TransferFile object) {
		if (object != null) {
			freeTransferFiles.add(object);
			if (freeTransferFiles.size() > maxSize) {
				Log.d("pool", maxSize+"");
				maxSize = freeTransferFiles.size();
			}			
		}
	}
}
