package myphone.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.util.LruCache;
import android.util.Log;

public class ImageCache {

	private static final String TAG = "main";
	
	private static int sAppVersion = 1;// 应用版本
	
	private LruCache<String, Bitmap> mMemoryCache;// 内存缓存
	
	private DiskLruCache mDiskLruCache;
	private boolean mDiskCacheStarting = false;// 是否开启缓存标记
	private final Object mDiskCacheLock = new Object();// 硬盘缓存锁
	private static final int DISK_CHCHE_SIZE = 1024 * 1024 * 10;// 10MB 硬盘缓存大小
	private static final String DISK_CHCHE_SUBDIR = "thumbnails";// 硬盘缓存目录
	
	private static final CompressFormat DEFAULT_COMPRESS_FORMAT = CompressFormat.JPEG;// 图片规格
	private static final int DEFAULT_COMPRESS_QUALITY = 70;// 图片质量 0-100, 
    private static final int DISK_CACHE_INDEX = 0;// 硬盘缓存每个键存储一个文件，位置为0
	
    private static ImageCache sInstance;
    
    public static ImageCache getInstance(Context context) {
    	if (sInstance == null) {
    		sInstance = new ImageCache(context);
    	}
    	return sInstance;
    }
    
    /*
     * 从缓存中获取位图
     */
    public Bitmap getBitmapFromCache(String key) {
    	Bitmap bitmap = getBitmapFromMemCache(key);
    	if (bitmap == null) {
    		bitmap = getBitmapFromDiskCache(key);
    	}
    	return bitmap;
    }
    /*
	 * 添加位图到缓存中
	 */
	public void addBitmapToCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null) {
			addBitmapToMemCache(key, bitmap);
		}
		addBitmapToDiskCache(key, bitmap);
	}
	
    private ImageCache(Context context) {
    	init(context);
    }
   
    private void init(Context context) {
    	initMemoryCache();
    	initDiskCache(context);
    }
	/*
	 * 初始化内存缓存 memory cache
	 */
	@SuppressLint("NewApi")
	private void initMemoryCache() {
		final int maxMemory = (int)(Runtime.getRuntime().maxMemory() / 1024);
		final int cacheSize = maxMemory / 8;
		Log.i(TAG, "maxMemory=" + maxMemory + ", cacheSize=" + cacheSize);
		
		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				return bitmap.getByteCount() / 1024;
			}
		};
	}
	
	/*
	 * 初始化硬盘缓存
	 */
	private void initDiskCache(Context context) {
		sAppVersion = getAppVersion(context);
		// 初始化硬盘缓存task
		File cacheDir = getDiskCacheDir(context, DISK_CHCHE_SUBDIR);
		new InitDiskCacheTask().execute(cacheDir);
	}
	
	/*
	 * 从硬盘缓存中获取位图
	 */
	private Bitmap getBitmapFromDiskCache(String key) {
		String imageKey = hashKeyByMd5(key);
		synchronized (mDiskCacheLock) {
			while (mDiskCacheStarting) {
				try {
					mDiskCacheLock.wait();
				} catch (InterruptedException e) {}
			}
			if (mDiskLruCache != null) {
				try {
					DiskLruCache.Snapshot snapshot;
					snapshot = mDiskLruCache.get(imageKey);
					if (snapshot != null) {
						InputStream inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
						if (inputStream != null) {
							return BitmapFactory.decodeStream(inputStream);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					Log.i(TAG, "DiskLruCache.Snapshot 读取数据流失败");
				}
			}
		}
		return null;
	}
	/*
	 * 添加位图到硬盘缓存中
	 */
	private void addBitmapToDiskCache(String key, Bitmap bitmap) {
		String imageKey = hashKeyByMd5(key);
		synchronized (mDiskCacheLock) {
			if (mDiskLruCache != null) {
				DiskLruCache.Snapshot snapshot;
				try {
					snapshot = mDiskLruCache.get(imageKey);
					if (snapshot == null) {
						final DiskLruCache.Editor editor = mDiskLruCache.edit(imageKey);
						if (editor != null) {
							OutputStream out = editor.newOutputStream(DISK_CACHE_INDEX);
							bitmap.compress(DEFAULT_COMPRESS_FORMAT, DEFAULT_COMPRESS_QUALITY, out);
							editor.commit();
							out.close();
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/*
	 * 添加图片进入memory cache
	 */
	public void addBitmapToMemCache(String key, Bitmap bitmap) {
		String imageKey = hashKeyByMd5(key);
		if (getBitmapFromMemCache(imageKey) == null) {
			mMemoryCache.put(imageKey, bitmap);
		}
	}
	/*
	 * 获得memory cache中的图片
	 */
	private Bitmap getBitmapFromMemCache(String key) {
		String imageKey = hashKeyByMd5(key);
		return mMemoryCache.get(imageKey);
	}
	
	/*
	 * 获取硬盘缓存目录
	 */
	@SuppressLint("NewApi")
	public File getDiskCacheDir(Context context, String uniqueName) {
		final String cachePath = Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())
				|| !Environment.isExternalStorageRemovable() ? context
				.getExternalCacheDir().getPath() : context.getCacheDir()
				.getPath();
		return new File(cachePath + File.separator + uniqueName);
	}
	
	/*
	 * 初始化硬盘缓存task
	 */
	class InitDiskCacheTask extends AsyncTask<File, Void, Void>  {

		@Override
		protected Void doInBackground(File... params) {
			synchronized (mDiskCacheLock) {
				File cacheDir = params[0];
				try {
					mDiskLruCache = DiskLruCache.open(cacheDir, sAppVersion, 1, DISK_CHCHE_SIZE);
				} catch (IOException e) {
					e.printStackTrace();
				}
				mDiskCacheStarting = false;
				mDiskCacheLock.notifyAll();
			}
			return null;
		}
	}
	
	/*
	 * 获取系统版本号
	 */
	private int getAppVersion(Context context) {
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return info.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return 1;
	}
	
	/*
	 * 对字符串进行MD5编码
	 */
	private String hashKeyByMd5(String key) {
		String cacheKey;
		MessageDigest mDigest;
		try {
			mDigest = MessageDigest.getInstance("MD5");
			mDigest.update(key.getBytes());
			cacheKey = bytesToHexString(mDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			cacheKey = String.valueOf(key.hashCode());
		}
		return cacheKey;
	}
	
	/*
	 * 将字节转换成字符串
	 */
	private String bytesToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1) {
				sb.append('0');
			}
			sb.append(hex);
		}
		return sb.toString();
	}
}
