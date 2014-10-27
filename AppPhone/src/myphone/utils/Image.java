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
public class Image {
	
	public static Bitmap getBitmapImage(Resources resources, int resId, int reqWidth, int reqHeight) {
		Log.i("main", "getBitmapImage");
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(resources, resId, options);

		options.inSampleSize = getSampleSize(options, reqWidth, reqHeight);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(resources, resId, options);
	}
	
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
