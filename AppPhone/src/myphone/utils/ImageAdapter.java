package myphone.utils;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageAdapter extends CursorAdapter {
		
		private Context mContext;
		private Cursor mCursor;
		private final int itemViewId;
		private final int imageViewId;
		private final int textViewId;
		
		public ImageAdapter(Context context, Cursor c, boolean autoRequery, int viewId, int imageId, int textId) {
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
//			loadBitmap(path, viewHolder.imageView);
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