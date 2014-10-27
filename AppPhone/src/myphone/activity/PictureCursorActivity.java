package myphone.activity;

import java.util.ArrayList;
import java.util.List;

import myphone.utils.Image;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.widget.CursorAdapter;
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

public class PictureCursorActivity extends Activity {

	private ListView mListView;
	
	private Cursor pictureList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picture);
		
		initPictureList();
		PictureCusorAdapter picAdapter = new PictureCusorAdapter(this, pictureList, 0);
		
		mListView = (ListView) findViewById(R.id.picture_list_view);
		mListView.setAdapter(picAdapter);
		
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
//				Picture picture = pictureList.get(position);
//				Toast.makeText(PictureCursorActivity.this, picture.getName(), Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	private void initPictureList() {
		Cursor c = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
//		for (int i = 0; i < 1; i++) {
//			Picture p = new Picture("hahaha", R.drawable.ic_bigimg);
//			pictureList.add(p);
//		}
	}
	
	public class PictureCusorAdapter extends CursorAdapter {

		private Context mContext;
		private Cursor mCursor;
		
		public PictureCusorAdapter(Context context, Cursor c, int flags) {
			super(context, c, flags);
			mContext = context;
			mCursor = c;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return null;
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
			
			Bitmap bitmap = Image.getBitmapImage(getResources(), picture.getImageId(), 100, 100);
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
