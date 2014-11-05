package myphone.utils;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
		
		private final WeakReference<ImageView> imageViewReference;
		private Context mContext;
		public String data;
		
		public BitmapWorkerTask(ImageView imageView, Context context) {
			// use a WeakReference to ensure the ImageView can be garbage collected
			imageViewReference = new WeakReference<ImageView>(imageView);
			mContext = context;
		}
		
		@Override
		protected Bitmap doInBackground(String... paths) {
			data = paths[0];
			
			final String imageKey = String.valueOf(data);
			Bitmap bitmap;
			bitmap = ImageCache.getInstance(mContext).getBitmapFromCache(imageKey);
			if (bitmap != null) {
				return bitmap;
			}
			bitmap = ImageUtil.getBitmapImage(data, 300, 300);
			try {
				ImageCache.getInstance(mContext).addBitmapToCache(imageKey, bitmap);
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
				final BitmapWorkerTask bitmapWorkerTask = ImageUtil.getBitmapWorkerTask(imageView); 
				if (this == bitmapWorkerTask && imageView != null) {
					imageView.setImageBitmap(bitmap);
				}
			}
		}
	}