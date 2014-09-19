package cn.m15.app.android.gotransfer.utils;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.util.Log;
import cn.m15.gotransfer.sdk.utils.SdkFileUtil;

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

	public static long getUsableSpace(Context ctx) {
		String[] volumePaths = SdkFileUtil.getVolumePaths(ctx);
		long result = 0;
		if (volumePaths != null) {
			for (String volumePath : volumePaths) {
				result += disk_free(volumePath);
			}
		}
		return result;
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
	
	public static String getReadableSize(long size) {
 	    if(size <= 0) return "0";
 	    final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
 	    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
 	    return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups))
 	            + " " + units[digitGroups];
 	}
 	
}
