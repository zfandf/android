package myphone.utils;

import java.lang.ref.WeakReference;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

/**
 * 图片处理
 * @author fan
 *
 */
public class ImageUtil {
	
	private ImageUtil() {}
	
	// cancel worktask
	public static boolean cancelPotentialWork(String path, ImageView imageView) {
		final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
		
		if (bitmapWorkerTask != null) {
			final String bitmapData = bitmapWorkerTask.data;
			// if bitmapData is yes set or it differs from the new data
			if (bitmapData == null  || bitmapData.equals(path)) {
				// cancel previous task
				bitmapWorkerTask.cancel(true);
			} else {
				// the same work is already in progress
				return false;
			}
		}
		// No task associated with the ImageView, or an existing task was canceled
		return true;
	}
	
	public static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
		if (imageView != null) {
			final Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				final AsyncDrawable asyncDrawable = (AsyncDrawable)drawable;
				return asyncDrawable.getBitmapWorkerTask();
			}
		}
		return null;
	}
	
	static class AsyncDrawable extends BitmapDrawable {
		private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;
		
		public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
			super(res, bitmap);
			bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
		}
		
		public BitmapWorkerTask getBitmapWorkerTask() {
			return bitmapWorkerTaskReference.get();
		}
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
