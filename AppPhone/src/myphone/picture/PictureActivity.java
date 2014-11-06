package myphone.picture;

import java.lang.ref.WeakReference;

import myphone.activity.R;
import myphone.utils.ImageCache;
import myphone.utils.ImageUtil;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class PictureActivity extends Activity {
	
	private static final String TAG = "main";
	
	private ListView mListView;
	private ListAdapter mAdapter;
	private Bitmap mPlaceHolerBitmap;// 默认图
	private final Integer[] redIds = new Integer[]{
			R.drawable.ic_bigimg,
			R.drawable.ic_reception_app,
			R.drawable.ic_reception_app_press,
			R.drawable.ic_wallpaper,
			R.drawable.ic_bigimg,
			R.drawable.ic_reception_app,
			R.drawable.ic_reception_app_press,
			R.drawable.ic_wallpaper,
			R.drawable.ic_bigimg,
			R.drawable.ic_reception_app,
			R.drawable.ic_reception_app_press,
			R.drawable.ic_wallpaper,
			R.drawable.ic_bigimg,
			R.drawable.ic_reception_app,
			R.drawable.ic_reception_app_press,
			R.drawable.ic_wallpaper,
			R.drawable.ic_bigimg,
			R.drawable.ic_reception_app,
			R.drawable.ic_reception_app_press,
			R.drawable.ic_wallpaper,
			R.drawable.ic_bigimg,
			R.drawable.ic_reception_app,
			R.drawable.ic_reception_app_press,
			R.drawable.ic_wallpaper,
			R.drawable.ic_bigimg,
			R.drawable.ic_reception_app,
			R.drawable.ic_reception_app_press,
			R.drawable.ic_wallpaper
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picture_list);
		
		ImageCache.setInstance(this);// 初始化ImageCache
		
		mPlaceHolerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
		mAdapter = new ImageAdapter(this, redIds, R.layout.item_picture, R.id.picture_image, R.id.picture_text);
		mListView = (ListView) findViewById(R.id.listview_picture);
		mListView.setAdapter(mAdapter);
	}
	
	// to start loading the bitmap asynchronously, simply create a new task and execute it
	public void loadBitmap(int resId, ImageView imageView) {
		if (cancelPotentialWork(resId, imageView)) {
			Log.i(TAG, "bitmap is null");
			BitmapWorkerTask task = new BitmapWorkerTask(imageView, getResources());
			final AsyncDrawable asyncDrawable = new AsyncDrawable(getResources(), mPlaceHolerBitmap, task);
			imageView.setImageDrawable(asyncDrawable);
			task.execute(resId);
		}
	}
	
	// cancel worktask
	public static boolean cancelPotentialWork(int data, ImageView imageView) {
		final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
		if (bitmapWorkerTask != null) {
			final int bitmapData = bitmapWorkerTask.data;
			// if bitmapData is yes set or it differs from the new data
			if (bitmapData == 0  || bitmapData != data) {
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
	
	public class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
		
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
			final String imageKey = String.valueOf(data);
			Bitmap bitmap;
			bitmap = ImageCache.getInstance().getBitmapFromCache(imageKey);
			if (bitmap != null) {
				Log.i(TAG, "bitmap is cache");
				return bitmap;
			}
			Log.i(TAG, "bitmap no cache");
			bitmap = ImageUtil.getBitmapImage(mResources, data, 100, 100);
			ImageCache.getInstance().addBitmapToCache(imageKey, bitmap);
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
	
	public class ImageAdapter extends BaseAdapter {
		
		private final Context mContext;
		private final Integer[] imageResIds;
		private final int itemViewId;
		private final int imageViewId;
		private final int textViewId;
		
		public ImageAdapter(Context context, Integer[] redIds, int viewId, int imageId, int textId) {
			super();
			mContext = context;
			imageResIds = redIds;
			itemViewId = viewId;
			imageViewId = imageId;
			textViewId = textId;
		}
		
		@Override
		public int getCount() {
			return imageResIds.length;
		}

		@Override
		public Object getItem(int position) {
			return imageResIds[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Log.i(TAG, "getView start");
			final View view;
			final ViewHolder viewHolder;
			final int resId = imageResIds[position];
			
			if (convertView == null) {// if it's not recycled, initialize some attribute
				view = LayoutInflater.from(mContext).inflate(itemViewId, null);
				viewHolder = new ViewHolder();
				viewHolder.imageView = (ImageView) view.findViewById(imageViewId);
				viewHolder.textView = (TextView) view.findViewById(textViewId);
				view.setTag(viewHolder);
			} else {
				view = convertView;
				viewHolder = (ViewHolder) view.getTag();
			}
			viewHolder.textView.setText("图片标题");
			loadBitmap(resId, viewHolder.imageView);
			Log.i(TAG, "getView end");
			return view;
		}
		
		class ViewHolder {
			ImageView imageView;
			TextView textView;
		}

	}
}
