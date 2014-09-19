package cn.m15.app.android.gotransfer.net.httpserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class CopyFile {
	
	public static int copyFile(String from, String to, int force) {
		return CopyFile.copyFile(from, to, force, "");
	}
	
	public static int copyFile(String from, String to, int force, String newName) {
		
		File fromFile = new File(from);
		File toFile = null;
		if (newName.length() == 0) {
			toFile = new File(to + File.separator + fromFile.getName());
		} else {
			toFile = new File(to + File.separator + newName);
		}
		if (fromFile.isFile()) {
			return copySingleFile(fromFile, toFile, force);
		} else {
			if (toFile.exists()) {
				if (force == 0) {
					return ServeAction.CONFIRM;
				}
			} else {
				toFile.mkdirs();
			}
			
			File[] list = fromFile.listFiles();
			for (int i = 0; i < list.length; i ++) {
				if (list[i].isFile()) {
					int ret = copySingleFile(list[i], new File(toFile.getAbsoluteFile()+File.separator + list[i].getName()), force);
					if (ret == 1) {
						DeleteFile.deleteDir(toFile.getAbsolutePath());
						return ret;
					}
				} else if (list[i].isDirectory()){
					copyFile(fromFile.getAbsolutePath()+File.separator + list[i].getName(), toFile.getAbsolutePath(), force);
				}
			}
			return ServeAction.SUCCESS;
		}
	}
	
	public static int copySingleFile(File from, File to, int force) {
		if (to.exists() && force == 0) {
			return ServeAction.CONFIRM;
		}
		int result = ServeAction.FAILED;
		BufferedInputStream inBuffer = null;
		BufferedOutputStream outBuffer = null;
		try {
			FileInputStream in = new FileInputStream(from);
			FileOutputStream out = new FileOutputStream(to);
			
			inBuffer = new BufferedInputStream(in);
			outBuffer = new BufferedOutputStream(out);
			
			byte[] buffer = new byte[1024*5];
			int length = 0;
			while ((length = inBuffer.read(buffer)) != -1) {
				outBuffer.write(buffer, 0, length);
			}
			outBuffer.flush();
			result = to.exists() ? ServeAction.SUCCESS : ServeAction.FAILED;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != inBuffer) {
				try {
					inBuffer.close();
				} catch (IOException e) {
				}
			}
			if (null != outBuffer) {
				try {
					outBuffer.close();
				} catch (IOException e) {
				}
			}
		}
		return result;
	}

}