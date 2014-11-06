package picture.cursor;

import myphone.activity.R;
import myphone.utils.ImageCache;
import myphone.utils.ImageUtil;
import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.GridView;

public class ImageActivity extends Activity {
	
//	private ListView mListView;
	private GridView mGridView;
	private ImageAdapter mAdapter;
	private Bitmap mPlaceHolerBitmap;// 默认图
	private Cursor mListData;
	private int mImageSize;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picture_list);
		
		mImageSize = getResources().getDisplayMetrics().widthPixels / 4;
		mPlaceHolerBitmap = ImageUtil.getBitmapImage(getResources(), R.drawable.ic_launcher, mImageSize, mImageSize);
		
		ImageCache.setInstance(this);// 初始化ImageCache
		ImageTask.initTask(getResources(), mPlaceHolerBitmap, mImageSize);// 初始化ImageTask
		initListData();
		
		mAdapter = new ImageAdapter(this, mListData, R.layout.item_picture, R.id.picture_image, R.id.picture_text);
		
		mGridView = (GridView) findViewById(R.id.listview_picture);
		mGridView.setAdapter(mAdapter);
		
	}
	
	public void initListData() {
		String[] projection = {
				MediaStore.Images.Media.DISPLAY_NAME,
				MediaStore.Images.Media.DATA,
				MediaStore.Images.Media._ID
		};
		mListData = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
	}
}
