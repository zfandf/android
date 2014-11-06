package myphone.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * 图片处理
 * @author fan
 *
 */
public class ImageUtil {
	
	private ImageUtil() {}
	
	public static Bitmap getBitmapImage(String path, int reqWidth) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);

		options.inSampleSize = getSampleSize(options, reqWidth);
		options.inJustDecodeBounds = false;

		return BitmapFactory.decodeFile(path, options);
	}
	
	/*
	 * 获得bitmap, 根据图片途径
	 */
	public static Bitmap getBitmapImage(String path, int reqWidth, int reqHeight) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);

		options.inSampleSize = getSampleSize(options, reqWidth, reqHeight);
		options.inJustDecodeBounds = false;

		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeFile(path, options);
			return Bitmap.createScaledBitmap(bitmap, reqWidth, reqHeight, true);
		} catch (Exception e) {
			Log.i("main", e.getMessage() + "");
		}
		return bitmap;
	}
	
	/*
	 * 获得bitmap, 根据资源 ID
	 */
	public static Bitmap getBitmapImage(Resources resources, int resId, int reqWidth, int reqHeight) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(resources, resId, options);

		options.inSampleSize = getSampleSize(options, reqWidth, reqHeight);
		options.inJustDecodeBounds = false;
		
		Bitmap bitmap = BitmapFactory.decodeResource(resources, resId, options);
		return Bitmap.createScaledBitmap(bitmap, reqWidth, reqHeight, true);
	}
	
	/*
	 * 获得位图略缩倍数
	 */
	private static int getSampleSize(BitmapFactory.Options options, int reqWidth) {
		final int resWidth = options.outWidth;
		
		int inSampleSize = 1;
		if (resWidth > reqWidth) {
			final int halfWidth = resWidth / 2;
			while (halfWidth / inSampleSize > reqWidth) {
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}
	
	/*
	 * 获得位图略缩倍数
	 */
	private static int getSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		final int resWidth = options.outWidth;
		final int resHeight = options.outHeight;
		
		int inSampleSize = 1;
		if (resWidth > reqWidth || resHeight > reqHeight) {
			final int halfWidth = resWidth / 2;
			final int halfHeight = resHeight / 2;
			while ((halfWidth / inSampleSize > reqWidth) && (halfHeight / inSampleSize > reqHeight)) {
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}
}
