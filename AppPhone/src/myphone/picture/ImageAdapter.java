package myphone.picture;

import myphone.utils.ImageCache;
import myphone.utils.ImageUtil;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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
		final View view;
		final ViewHolder viewHolder;
		final int redId = imageResIds[position];
		
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
		final String imageKey = String.valueOf(redId);
		Bitmap bitmap = ImageCache.getInstance(mContext).getBitmapFromCache(imageKey);
		if (bitmap == null) {
			bitmap = ImageUtil.getBitmapImage(mContext.getResources(), redId, 100, 100);
			ImageCache.getInstance(mContext).addBitmapToCache(imageKey, bitmap);
		}
		viewHolder.imageView.setImageBitmap(bitmap);
		viewHolder.textView.setText("图片标题");
		return view;
	}
	
	class ViewHolder {
		ImageView imageView;
		TextView textView;
	}

}
