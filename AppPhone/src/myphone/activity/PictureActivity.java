package myphone.activity;

import java.util.ArrayList;
import java.util.List;

import myphone.utils.ImageCache;
import myphone.utils.ImageUtil;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PictureActivity extends Activity {
	
	private static final String TAG = "main";
	private ListView mListView;
	
	private static ImageCache sImageCache;
	
	private List<Picture> pictureList = new ArrayList<Picture>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picture);
		
		sImageCache = ImageCache.getInstance(this);
		ImageUtil.init(this, ImageUtil.getBitmapImage(getResources(), R.drawable.ic_wallpaper, 100, 100));
		
		initPictureList();
		PictureAdapter picAdapter = new PictureAdapter(this, R.layout.item_picture, pictureList);
		
		mListView = (ListView) findViewById(R.id.picture_list_view);
		mListView.setAdapter(picAdapter);
		
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				Picture picture = pictureList.get(position);
				Toast.makeText(PictureActivity.this, picture.getName(), Toast.LENGTH_SHORT).show();
			}
		});
		
	}
	
	class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {

		@Override
		protected Bitmap doInBackground(Integer... resIds) {
			final String imgKey = String.valueOf(resIds[0]);
			
			Bitmap bitmap = sImageCache.getBitmapFromCache(imgKey);
			if (bitmap == null) {
				bitmap = ImageUtil.getBitmapImage(getResources(), resIds[0], 100, 100);
			}
			return bitmap;
		}
	}
	
	private void initPictureList() {
//		Cursor c = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
		for (int i = 0; i < 500; i++) {
			Picture p = new Picture("hahaha", R.drawable.ic_bigimg);
			pictureList.add(p);
		}
	}
		
	public class PictureAdapter extends ArrayAdapter<Picture> {
		
		private int resourceId;
		
		public PictureAdapter(Context context, int resource, List<Picture> objects) {
			super(context, resource, objects);
			resourceId = resource;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Picture picture = getItem(position);
			View view;
			ViewHolder viewHolder;
			if (convertView == null) {
				view = LayoutInflater.from(getContext()).inflate(resourceId, null);
				viewHolder = new ViewHolder();
				viewHolder.imageView = (ImageView) view.findViewById(R.id.picture_image);
				viewHolder.nameView = (TextView) view.findViewById(R.id.picture_text); 
				view.setTag(viewHolder);
			} else {
				view = convertView;
				viewHolder = (ViewHolder)view.getTag();
			}
			
			final String imageKey = String.valueOf(picture.getImageId());
			Bitmap bitmap = sImageCache.getBitmapFromCache(imageKey);
			if (bitmap == null) {
				bitmap = ImageUtil.getBitmapImage(getResources(), picture.getImageId(), 100, 100);
				sImageCache.addBitmapToCache(imageKey, bitmap);
			} else {
				Log.i(TAG, "bitmap not null");
			}
			viewHolder.imageView.setImageBitmap(bitmap);
			viewHolder.nameView.setText(picture.getName());
			return view;
		}
	}
	
	class ViewHolder {
		ImageView imageView;
		TextView nameView;
	}
	
	public class Picture {
		
		private String name;

		private int imageId;
		
		public Picture(String name, int imageId) {
			this.name = name;
			this.imageId = imageId;
		}
		
		public String getName() {
			return name;
		}

		public int getImageId() {
			return imageId;
		}
		
	}
}
