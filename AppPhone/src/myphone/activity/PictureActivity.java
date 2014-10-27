package myphone.activity;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PictureActivity extends ActionBarActivity {

	private ListView mListView;
	
	private List<Picture> pictureList = new ArrayList<Picture>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picture);
		
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
	
	private void initPictureList() {
		
//		for (int i = 0; i < 1000; i++) {
//			Picture p = new Picture("hahaha", R.drawable.ic_reception_app_press);
//			pictureList.add(p);
//		}
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.picture, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public class PictureAdapter extends ArrayAdapter<Picture> {
		
		private int resourceId;
		
		public PictureAdapter(Context context, int resource, List<Picture> objects) {
			super(context, resource, objects);
			resourceId = resource;
		}
		
		@SuppressLint("ViewHolder")
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
			
			viewHolder.imageView.setImageResource(picture.getImageId());
			viewHolder.nameView.setText(picture.getName());
			return view;
		}
		
		class ViewHolder {
			ImageView imageView;
			TextView nameView;
		}
		
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
