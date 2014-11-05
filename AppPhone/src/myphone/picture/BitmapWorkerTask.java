package myphone.picture;

import java.lang.ref.WeakReference;

import myphone.utils.ImageUtil;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
	
	private Resources mResources;
	private final WeakReference<ImageView> imageViewReference;
	private int data = 0;
	
	public BitmapWorkerTask(ImageView imageView, Resources resource) {
		// use a WeakReference to ensure the ImageView can be garbage collected
		imageViewReference = new WeakReference<ImageView>(imageView);
		mResources = resource;
	}
	
	@Override
	protected Bitmap doInBackground(Integer... resIds) {
		data = resIds[0];
		return ImageUtil.getBitmapImage(mResources, data, 100, 100);
	}
	
	@Override
	protected void onPostExecute(Bitmap bitmap) {
		if (imageViewReference != null && bitmap != null) {
			final ImageView imageView = imageViewReference.get();
			if (imageView != null) {
				imageView.setImageBitmap(bitmap);
			}
		}
	}
}
