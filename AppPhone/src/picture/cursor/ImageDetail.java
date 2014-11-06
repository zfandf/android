package picture.cursor;

import myphone.activity.R;
import myphone.utils.ImageUtil;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

public class ImageDetail extends Activity {
	
	public static final String PATH_NAME = "path_name";
	private static String sPath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.item_picture);
		
		Intent intent = getIntent();
		sPath = intent.getStringExtra(PATH_NAME);
		
		final int width = getResources().getDisplayMetrics().widthPixels;
		
		Bitmap bitmap = ImageUtil.getBitmapImage(sPath, width);
		ImageView imageView = (ImageView) findViewById(R.id.picture_image);
		imageView.setImageBitmap(bitmap);
	}
}
