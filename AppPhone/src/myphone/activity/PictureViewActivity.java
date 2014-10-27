package myphone.activity;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class PictureViewActivity extends Activity {
	
	public ImageView mImageView;
	public TextView mTextView;
	private Bitmap mPlaceHolderBitmap;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.item_picture);
		
		mImageView = (ImageView)findViewById(R.id.picture_image);
		mTextView = (TextView)findViewById(R.id.picture_text);
		mTextView.setText("hahaah");
		
		mPlaceHolderBitmap = getBitmap(getResources(), R.drawable.ic_wallpaper, 100, 100);
		
		loadBitmap(R.drawable.ic_bigimg, mImageView);
//		BitmapTask task = new BitmapTask(mImageView);
//		task.execute(R.drawable.ic_bigimg);
//		Bitmap bm = getBitmap(getResources(), R.drawable.ic_wallpaper, 100, 100);
//		Log.i("main", "width=" + bm.getWidth() + "height=" + bm.getHeight());
//		mImageView.setImageBitmap(bm);
	}
	
	/*
	 * 获得位图
	 */
	public Bitmap getBitmap(Resources res, int resid, int width, int height) {
		Bitmap bm;
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resid, options);
		
		options.inSampleSize = calculateInSampleSize(options, width, height);
		options.inJustDecodeBounds = false;
		bm = BitmapFactory.decodeResource(res, resid, options);
		return bm;
	}
	
	/*
	 * 计算略缩图的缩小倍数
	 */
	public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
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
		Log.i("main", "resWidth=" + resWidth + ", resHeight=" + resHeight
				+ ", inSampleSize=" + inSampleSize + ", reqWidth=" + reqWidth
				+ ", reqHeight=" + reqHeight);
		return inSampleSize;
	}
	
	public class BitmapTask extends AsyncTask<Integer, Void, Bitmap> {

		private final WeakReference<ImageView> mImageViewReference;
		private int data = 0;
		
		public BitmapTask(ImageView imageView) {
			mImageViewReference = new WeakReference<ImageView>(imageView);
		}
		
		@Override
		protected Bitmap doInBackground(Integer... params) {
			data = params[0];
			return getBitmap(getResources(), data, 1000, 1000);
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

	static class AsyncDrawable extends BitmapDrawable {
		
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
			final AsyncDrawable asyncDrawable = new AsyncDrawable(getResources(), mPlaceHolderBitmap, task);
			imageView.setImageDrawable(asyncDrawable);
			task.execute(resId);
		}
	}
	
	public static boolean cancelPotentialWork(int data, ImageView imageView) {
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
	
	public static BitmapTask getBitmapTask(ImageView imageView) {
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
