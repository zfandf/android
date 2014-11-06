package picture.cursor;

import java.lang.ref.WeakReference;

import myphone.utils.ImageCache;
import myphone.utils.ImageUtil;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

class ImageTask {

	private static Resources sResource;
	private static Bitmap sPlaceHolerBitmap;
	private static ImageTask sInstance;
	private static int mImageSize;
	 
	public static void initTask(Resources res, Bitmap bitmap, int imageSize) {
		sResource = res;
		sPlaceHolerBitmap = bitmap;
		mImageSize = imageSize;
	}
	
	public static ImageTask getsInstance() {
		if (sInstance == null) {
			sInstance = new ImageTask();
		}
		return sInstance;
	}
	
	private ImageTask() {}
	
	// to start loading the bitmap asynchronously, simply create a new task and execute it
	public void loadBitmap(String path, ImageView imageView) {
		if (cancelPotentialWork(path, imageView)) {
			BitmapWorkerTask task = new BitmapWorkerTask(imageView);
			final AsyncDrawable asyncDrawable = new AsyncDrawable(sResource, sPlaceHolerBitmap, task);
			imageView.setImageDrawable(asyncDrawable);
			task.execute(path);
		}
	}
	

	// cancel worktask
	private static boolean cancelPotentialWork(String path, ImageView imageView) {
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
	
	private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
		if (imageView != null) {
			final Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				final AsyncDrawable asyncDrawable = (AsyncDrawable)drawable;
				return asyncDrawable.getBitmapWorkerTask();
			}
		}
		return null;
	}
	
	private class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
		
		private final WeakReference<ImageView> imageViewReference;
		public String data;
		
		public BitmapWorkerTask(ImageView imageView) {
			// use a WeakReference to ensure the ImageView can be garbage collected
			imageViewReference = new WeakReference<ImageView>(imageView);
		}
		
		@Override
		protected Bitmap doInBackground(String... paths) {
			data = paths[0];
			
			final String imageKey = String.valueOf(data);
			Bitmap bitmap;
			bitmap = ImageCache.getInstance().getBitmapFromCache(imageKey);
			if (bitmap != null) {
				return bitmap;
			}
			bitmap = ImageUtil.getBitmapImage(data, mImageSize, mImageSize);
			try {
				ImageCache.getInstance().addBitmapToCache(imageKey, bitmap);
			} catch (Exception e) {
				Log.i("main", e.getMessage()+"");
			}
			return bitmap;
		}
		
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (isCancelled()) {
				bitmap = null;
			}
			if (imageViewReference != null && bitmap != null) {
				final ImageView imageView = imageViewReference.get();
				final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView); 
				if (this == bitmapWorkerTask && imageView != null) {
					imageView.setImageBitmap(bitmap);
				}
			}
		}
	}

	private static class AsyncDrawable extends BitmapDrawable {
		private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;
		
		public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
			super(res, bitmap);
			bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
		}
		
		public BitmapWorkerTask getBitmapWorkerTask() {
			return bitmapWorkerTaskReference.get();
		}
	}
}
