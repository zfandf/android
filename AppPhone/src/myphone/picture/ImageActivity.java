package myphone.picture;

import java.lang.ref.WeakReference;

import myphone.activity.R;
import myphone.utils.ImageCache;
import myphone.utils.ImageUtil;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ImageActivity extends Activity {
	
	private static final String TAG = "main";
	
	private ListView mListView;
	private ImageCursorAdapter mAdapter;
	private Bitmap mPlaceHolerBitmap;// 默认图
	private Cursor mListData;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picture_list);
		
		initListData();
		
		mAdapter = new ImageCursorAdapter(this, mListData, false, R.layout.item_picture, R.id.picture_image, R.id.picture_text);
		
		mPlaceHolerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
		mListView = (ListView) findViewById(R.id.listview_picture);
		mListView.setAdapter(mAdapter);
	}
	
	public void initListData() {
		String[] projection = {
				MediaStore.Images.Media.DISPLAY_NAME,
				MediaStore.Images.Media.DATA,
				MediaStore.Images.Media._ID
		};
		mListData = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
	}
	
	// to start loading the bitmap asynchronously, simply create a new task and execute it
	public void loadBitmap(String path, ImageView imageView) {
		if (cancelPotentialWork(path, imageView)) {
			BitmapWorkerTask task = new BitmapWorkerTask(imageView);
			final AsyncDrawable asyncDrawable = new AsyncDrawable(getResources(), mPlaceHolerBitmap, task);
			imageView.setImageDrawable(asyncDrawable);
			task.execute(path);
		}
	}
	
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
	
	public class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
		
		private final WeakReference<ImageView> imageViewReference;
		private String data;
		
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
			bitmap = ImageUtil.getBitmapImage(data, 300, 300);
			try {
				ImageCache.getInstance().addBitmapToCache(imageKey, bitmap);
			} catch (Exception e) {
				Log.i(TAG, e.getMessage()+"");
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
	
	public class ImageCursorAdapter extends CursorAdapter {
		
		private Context mContext;
		private Cursor mCursor;
		private final int itemViewId;
		private final int imageViewId;
		private final int textViewId;
		
		public ImageCursorAdapter(Context context, Cursor c, boolean autoRequery, int viewId, int imageId, int textId) {
			super(context, c, autoRequery);
			mContext = context;
			mCursor = c;
			itemViewId = viewId;
			imageViewId = imageId;
			textViewId = textId;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final View view;
			final ViewHolder viewHolder;
			
			if (!mCursor.moveToPosition(position)) {
				return null;
			}
			
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
			String path = mCursor.getString(1);
			if (path == null) {
				Log.i(TAG, "path is null");
			}
			loadBitmap(path, viewHolder.imageView);
			viewHolder.textView.setText(mCursor.getString(0));
			return view;
		}
		
		class ViewHolder {
			ImageView imageView;
			TextView textView;
		}

		@Override
		public void bindView(View view, Context context, Cursor c) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public View newView(Context context, Cursor c, ViewGroup viewGroup) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
