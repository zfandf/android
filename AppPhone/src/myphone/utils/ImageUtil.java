package myphone.utils;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

/**
 * 图片处理
 * @author fan
 *
 */
public class ImageUtil {
	
	private static Context sContext;
	private static Bitmap sPlaceHolderBitmap;
	
	private ImageUtil() {}
	
	public static void init(Context context, Bitmap holderBitmap) {
		sContext = context;
		sPlaceHolderBitmap = holderBitmap;
	}
	
	/*
	 * 根据参数获得位图
	 */
	public static Bitmap getBitmapImage(Resources resources, int resId, int reqWidth, int reqHeight) {
		Log.i("main", "getBitmapImage");
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(resources, resId, options);

		options.inSampleSize = getSampleSize(options, reqWidth, reqHeight);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(resources, resId, options);
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
	
	private class BitmapTask extends AsyncTask<Integer, Void, Bitmap> {

		private final WeakReference<ImageView> mImageViewReference;
		private int data = 0;
		
		public BitmapTask(ImageView imageView) {
			mImageViewReference = new WeakReference<ImageView>(imageView);
		}
		
		@Override
		protected Bitmap doInBackground(Integer... resIds) {
			data = resIds[0];
			return getBitmapImage(sContext.getResources(), data, 1000, 1000);
		}
		
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (isCancelled()) {
				bitmap = null;
			}
			if (mImageViewReference != null && bitmap != null) {
				final ImageView imageView = mImageViewReference.get();
				final BitmapTask bitmapTask = getBitmapTask(imageView);
				if (this == bitmapTask && imageView != null) {
					imageView.setImageBitmap(bitmap);
				}
			}
		}
		
	}

	private static class AsyncDrawable extends BitmapDrawable {
		
		private final WeakReference<BitmapTask> bitmapTaskReference;
		
		public AsyncDrawable(Resources res, Bitmap bitmap, BitmapTask bitmapTask) {
			super(res, bitmap);
			bitmapTaskReference = new WeakReference<BitmapTask>(bitmapTask);
		}
		
		public BitmapTask getBitmapTask() {
			return bitmapTaskReference.get();
		}		
		
	}
	
	public void loadBitmap(int resId, ImageView imageView) {
		if (cancelPotentialWork(resId, imageView)) {
			final BitmapTask task = new BitmapTask(imageView);
			final AsyncDrawable asyncDrawable = new AsyncDrawable(sContext.getResources(), sPlaceHolderBitmap, task);
			imageView.setImageDrawable(asyncDrawable);
			task.execute(resId);
		}
	}
	
	private static boolean cancelPotentialWork(int data, ImageView imageView) {
		final BitmapTask bitmapTask = getBitmapTask(imageView);
		if (bitmapTask != null) {
			final int bitmapData = bitmapTask.data;
			if (bitmapData == 0 || bitmapData != data) {
				bitmapTask.cancel(true);
			} else {
				return false;
			}
		}
		return true;
	}
	
	private static BitmapTask getBitmapTask(ImageView imageView) {
		if (imageView != null) {
			final Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				final AsyncDrawable asyncDrawable = (AsyncDrawable)drawable;
				return asyncDrawable.getBitmapTask();
			}
		}
		return null;
	}
}
