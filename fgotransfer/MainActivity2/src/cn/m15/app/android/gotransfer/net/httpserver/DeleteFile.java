package cn.m15.app.android.gotransfer.net.httpserver;

import java.io.File;

public class DeleteFile {

    public static boolean deleteDir(String dirpath) {
    	File dir = new File(dirpath);
    	if (dir.isFile()) {
    		return dir.delete();
    	} else {
    		File[] list = dir.listFiles();
    		for (int i = 0; i < list.length; i++) {
    			boolean result = deleteDir(new File(dir, list[i].getName()).getAbsolutePath());
    			if (!result) {
    				return false;
    			}
    		}
    	}
    	return dir.delete();
    }
}
