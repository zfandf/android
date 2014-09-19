
/**
 * SDK使用的文件工具类
 */
package cn.m15.gotransfer.sdk.utils;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import cn.m15.gotransfer.sdk.ConfigManager;
import cn.m15.gotransfer.sdk.SdkConst;
import cn.m15.gotransfer.sdk.net.ipmsg.MessageConst.FileType;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.util.Log;
import android.webkit.MimeTypeMap;

public class SdkFileUtil {
	
	private static String[] getVolumePathsFor14(Context context) {
		List<String> availablePaths = new ArrayList<String>();
		StorageManager storageManager = (StorageManager) context
				.getSystemService(Context.STORAGE_SERVICE);
		try {
			Method methodGetPaths = storageManager.getClass().getMethod(
					"getVolumePaths");
			Method methodGetStatus = storageManager.getClass().getMethod(
					"getVolumeState", String.class);
			String[] paths = (String[]) methodGetPaths.invoke(storageManager);

			for (String path : paths) {
				String status = (String) (methodGetStatus.invoke(
						storageManager, path));
				if (status.equals(Environment.MEDIA_MOUNTED)) {
					availablePaths.add(path);
				}
			}
		} catch (Exception e) {
			Log.e("filemanager", "getVolumePathsFor14 >>> " + e.toString());
		}
		if (availablePaths.size() > 0) {
			String[] strings = new String[availablePaths.size()];
			availablePaths.toArray(strings);
			return strings;
		} else {
			return null;
		}
	}

	public static String[] getVolumePaths(Context context) {
		if (android.os.Build.VERSION.SDK_INT >= 14) {
			return getVolumePathsFor14(context);
		} else if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			return new String[] { Environment.getExternalStorageDirectory()
					.getAbsolutePath() };
		}
		return null;
	}

 	public static String getStoreReceviedFilePath(String fileName) {
 		return getSubFolderPath(getFileType(fileName)) + fileName;
 	}
	
 	public static String getStoreReceviedFilePath(String srcDirPath, String srcFilePath) {
 		if (srcDirPath.endsWith(File.separator)) {
 			srcDirPath = srcDirPath.substring(0, srcDirPath.length() - 1);
 		}
 		int index = srcDirPath.lastIndexOf(File.separator);
 		if (index != -1) {
 			srcDirPath = srcDirPath.substring(0, index);			
 		} else {
 			srcDirPath = "";
 		}
 		String relativePath = srcFilePath.substring(srcDirPath.length(), srcFilePath.length());
 		String result = getSubFolderPath(FileType.DIR) + relativePath;
 		return result;
 	}
	
	public static String getSubFolderPath(int fileType) {
		String subFolder = "";
		switch (fileType) {
		case FileType.PICTURE:
			subFolder = "Images" + File.separator;
			break;
		case FileType.VIDEO:
			subFolder = "Videos" + File.separator;
			break;
		case FileType.MUSIC:
			subFolder = "Musics" + File.separator;
			break;
		case FileType.APP:
			if (SdkConst.SHARE_APK) {
				subFolder = "apps" + File.separator;
				break;
			}
		case FileType.OTHERS:
			subFolder = "Files" + File.separator;
			break;
		case FileType.DIR:
			subFolder = "Folders" + File.separator;
			break;
		default:
			subFolder = "Files" + File.separator;
			break;
		}

		String resultPath = ConfigManager.getInstance().getStorePath() + subFolder;
		File file = new File(resultPath);
		if (!file.exists()) {
			file.mkdirs();
		}
		return resultPath;
	}

	/**
	 * 获取文件类型，不包括目录！
	 * @param name 文件名称
	 * @return
	 */
	public static int getFileType(String name) {
		int lastDot = name.lastIndexOf(".");
		if (lastDot < 0) {
			return FileType.OTHERS;
		}
		String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
				name.substring(lastDot + 1));
		if (mimeType == null) {
			return FileType.OTHERS;
		} else if (mimeType.matches("image/.+")) {
			return FileType.PICTURE;
		} else if (mimeType.matches("audio/.+")) {
			return FileType.MUSIC;
		} else if (mimeType.matches("video/.+")) {
			return FileType.VIDEO;
		} else if (mimeType.equals("application/vnd.android.package-archive")) {
			return FileType.APP;
		} else {
			return FileType.OTHERS;
		}
	}
	
	public static String getDirName(String dirPath) {
		String[] array = dirPath.split(File.separator);
		return array[array.length - 1];
	}
	
}
