package cn.m15.app.android.gotransfer.utils;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.util.Log;
import android.webkit.MimeTypeMap;
import cn.m15.app.android.gotransfer.Const;
import cn.m15.app.android.gotransfer.GoTransferApplication;
import cn.m15.app.android.gotransfer.enity.TransferFile;
import cn.m15.app.android.gotransfer.net.ipmsg2.MessageConst.FileType;

public class FileUtil {

	public static final String HIDDEN_PREFIX = ".";
	
	private FileUtil() {
	}

	/* Checks if external storage is available for read and write */
	public static boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		return Environment.MEDIA_MOUNTED.equals(state);
	}

	/* Checks if external storage is available to at least read */
	public static boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		return Environment.MEDIA_MOUNTED.equals(state)
				|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
	}

	public static long disk_free(String path) {
		StatFs stat = new StatFs(path);
		@SuppressWarnings("deprecation")
		long free_memory = (long) stat.getAvailableBlocks()
				* (long) stat.getBlockSize(); // return value is in bytes
		return free_memory;
	}

	public static long disk_total(String path) {
		StatFs stat = new StatFs(path);
		@SuppressWarnings("deprecation")
		long total_memory = (long) stat.getBlockCount()
				* (long) stat.getBlockSize(); // return value is in bytes
		return total_memory;
	}

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

	public static long getUsableSpace(Context ctx) {
		String[] volumePaths = getVolumePaths(ctx);
		long result = 0;
		if (volumePaths != null) {
			for (String volumePath : volumePaths) {
				result += disk_free(volumePath);
			}
		}
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
			if (Const.SHARE_APK) {
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
//		String parentPath = GoTransferApplication.getInstance().getStorePath();
//		if (!TextUtils.isEmpty(parentPath)
//				&& parentPath.toCharArray()[parentPath.length() - 1] != File.separatorChar) {
//			parentPath = parentPath + File.separatorChar;
//		}
		String resultPath = GoTransferApplication.getInstance().getStorePath() + subFolder;
		File file = new File(resultPath);
		if (!file.exists()) {
			file.mkdirs();
		}
		return resultPath;
	}

	public static void scanFile(Context ctx, String path) {
		Uri uri = Uri.fromFile(new File(path));
		Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
		ctx.sendBroadcast(intent);
	}

	public static boolean isInternalStoragePath(Context context, String path) {
		StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
		try {
			Method getPrimaryVolumeMethod = StorageManager.class.getMethod("getPrimaryVolume", null);
			getPrimaryVolumeMethod.setAccessible(true);
			Object volume = getPrimaryVolumeMethod.invoke(storageManager, null);
			boolean isRemovable = (Boolean) volume.getClass().getMethod("isRemovable", null).invoke(volume, null);
			String primaryPath = (String) volume.getClass().getMethod("getPath", null).invoke(volume, null);
			String desc = (String) volume.getClass().getMethod("toString", null).invoke(volume, null);
			Log.d("filemanager", "desc >>> " + desc);
			if (path != null && path.equals(primaryPath)) {
				return !isRemovable;
			}
		} catch (Exception e) {
			Log.e("filemanager", "getInternalStoragePath >>> " + e.toString());
		}
		return false;
	}

	public static String getInternalStoragePath(Context ctx,
			String[] volumePaths) {
		String defaultExternalStoragePath = Environment
				.getExternalStorageDirectory().getPath();
		String internalStoragePath = null;
		for (String p : volumePaths) {
			if (FileUtil.isInternalStoragePath(ctx, p)) {
				internalStoragePath = p;
				break;
			}
		}
		if (internalStoragePath == null) {
			if (FileUtil.isExternalStorageReadable()
					&& !Environment.isExternalStorageRemovable()) {
				internalStoragePath = new String(defaultExternalStoragePath);
			} else {
				for (String p : volumePaths) {
					if (!defaultExternalStoragePath.equals(p)) {
						internalStoragePath = p;
						break;
					}
				}
			}
		}
		Log.d("filemanager", "compute>>>" + internalStoragePath + "," 
				+ Environment.isExternalStorageRemovable());
		return internalStoragePath;
	}

	/**
	 * Get a file extension without the leading '.'
	 * 
	 * @param fileName
	 * @return extension
	 * 
	 */
	public static String getFileExtensionByName(String fileName) {
		int index = fileName.lastIndexOf(".");
		String extension = null;
		if (index > 0) {
			extension = fileName.substring(index + 1);
		}
		return extension;
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

	public static void deleteReceivedFileByName(String filename) {
		int fileType = getFileType(filename);
		String fileFolderPath = getSubFolderPath(fileType);
		File file = new File(fileFolderPath + filename);
		if (file.exists()) {
			file.delete();
		}
	}
	
	 /**
     * File and folder comparator. TODO Expose sorting option method
     *
     * @author paulburke
     */
    public static Comparator<File> sComparator = new Comparator<File>() {
        @Override
        public int compare(File f1, File f2) {
            // Sort alphabetically by lower case, which is much cleaner
            return f1.getName().toLowerCase().compareTo(
                    f2.getName().toLowerCase());
        }
    };

    /**
     * File (not directories) filter.
     *
     * @author paulburke
     */
    public static FileFilter sFileFilter = new FileFilter() {
        @Override
        public boolean accept(File file) {
            final String fileName = file.getName();
            // Return files only (not directories) and skip hidden files
            return file.isFile() && !fileName.startsWith(HIDDEN_PREFIX);
        }
    };
    
    /**
     * File (not directories) filter with hidden file.
     *
     */
    public static FileFilter sFileFilterWithHidden = new FileFilter() {
        @Override
        public boolean accept(File file) {
            // Return files only (not directories) and skip hidden files
            return file.isFile();
        }
    };

    /**
     * Folder (directories) filter.
     *
     * @author paulburke
     */
    public static FileFilter sDirFilter = new FileFilter() {
        @Override
        public boolean accept(File file) {
            final String fileName = file.getName();
            // Return directories only and skip hidden directories
            return file.isDirectory() && !fileName.startsWith(HIDDEN_PREFIX);
        }
    };
    
    /**
     * Folder (directories) filter with hidden file.
     *
     */
    public static FileFilter sDirFilterWithHidden = new FileFilter() {
        @Override
        public boolean accept(File file) {
            return file.isDirectory();
        }
    };
    
    // TODO: 缺少判断是否获取隐藏文件
 	public static void fetchFileListInDir(ArrayList<TransferFile> fileList, File dir) {
 		for (File f : dir.listFiles()) {
 			if (f.isDirectory()) {
 				fetchFileListInDir(fileList, f);
 			} else {
 				TransferFile tf = new TransferFile();
 				tf.path = f.getAbsolutePath();
 				tf.name = f.getName();
 				tf.size = f.length();
 				fileList.add(tf);
 			}
 		}
 	}
 	
 	public static String getStoreReceviedFilePath(String fileName) {
 		return FileUtil.getSubFolderPath(FileUtil.getFileType(fileName)) + fileName;
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
 		String result = FileUtil.getSubFolderPath(FileType.DIR) + relativePath;
 		return result;
 	}
 	
 	public static long getSize(File file) {
 		long size;
 	    if (file.isDirectory()) {
 	    	size = 0;
 	    	File[] files = file.listFiles();
 	    	if (files != null) {
 	    		for (File child : files) {
 	 	            size += getSize(child);
 	 	        } 	    		
 	    	}
 	    } else {
 	        size = file.length();
 	    }
 	    return size;
 	}
 	
 	public static String getReadableSize(long size) {
 	    if(size <= 0) return "0";
 	    final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
 	    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
 	    return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups))
 	            + " " + units[digitGroups];
 	}
 	
 	public static void sortList(File[] list) {
		Arrays.sort(list, new Comparator<File>() {
			@Override
			public int compare(File lhs, File rhs) {
				int lhsIsDir = lhs.isDirectory() ? 1 : 0;
				int rhsIsDir = rhs.isDirectory() ? 1 : 0;
				int tmp = rhsIsDir - lhsIsDir;
				return tmp == 0 ? lhs.getName().compareToIgnoreCase(
						rhs.getName()) : tmp;
			}
		});
	}

	public static String formatFileSize(long size) {
		if (size < 1024) {
			return size + "B";
		} else if (size < 1024 * 1024) {
			return Math.round(100 * size / 1024) / 100.0 + "K";
		} else if (size < Math.pow(1024, 3)) {
			return Math.round(100 * size / Math.pow(1024, 2)) / 100.0 + "M";
		} else {
			return Math.round(100 * size / Math.pow(1024, 3)) / 100.0 + "G";
		}
	}

	public static long getFileAndDirSize(File file) {
		long size = 0;
		if (file.isFile()) {
			size += file.length();
		} else {
			File[] list = file.listFiles();
			for (int i = 0; i < list.length; i++) {
				if (list[i].isFile()) {
					size += list[i].length();
				} else {
					size += getFileAndDirSize(list[i]);
				}
			}
		}
		return size;
	}

	public static int isNotEmptyDir(String dir, FileFilter filter) {
		File f = new File(dir);
		if (f.exists() && f.isDirectory()) {
			File[] list = f.listFiles(filter);
			return list.length > 0 ? 1 : 0;
		}
		return 0;
	}

	public static String renameDumplicatedFile(String name) {
		long time = new Date().getTime();
		int dotPos = name.lastIndexOf(".");
		if (-1 == dotPos) {
			return name + time;
		}
		return name.substring(0, dotPos) + "_" + time + "."
				+ name.substring(dotPos + 1);
	}

	public static boolean checkDiskSpace(String from, String to) {
		return FileUtil.disk_free(to) < FileUtil.getFileAndDirSize(new File(
				from)) ? false : true;
	}

	public static boolean checkDiskSpace(String[] from, String to) {
		long fromSize = 0;
		for (String fromFile : from) {
			fromSize += FileUtil.getFileAndDirSize(new File(fromFile));
		}
		return FileUtil.disk_free(to) < fromSize ? true : false;
	}
}
