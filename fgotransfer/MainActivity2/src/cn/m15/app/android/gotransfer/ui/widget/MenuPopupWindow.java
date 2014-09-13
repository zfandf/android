package cn.m15.app.android.gotransfer.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import cn.m15.app.android.gotransfer.GoTransferApplication;
import cn.m15.app.android.gotransfer.R;

public class MenuPopupWindow {

	private LayoutInflater mLayoutInflater;
	private PopupWindow mPopupWindow;
	private MenuItemSelectedListener mListener;
	private ListView mLvMenuListView;
	private MenuAdapter mAdapter;

	public MenuPopupWindow(Context ctx, MenuItemSelectedListener listener) {
		mLayoutInflater = LayoutInflater.from(ctx);
		mListener = listener;
		initPopupWindow(ctx);
	}

	private void initPopupWindow(Context ctx) {
		View contentView = renderContentView(ctx);
		int width = ctx.getResources().getDisplayMetrics().widthPixels / 2;
		mPopupWindow = new PopupWindow(contentView, width,
				LayoutParams.WRAP_CONTENT, true);
		mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		mPopupWindow.setFocusable(true);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.update();

	}

	private View renderContentView(Context ctx) {
		View view = mLayoutInflater.inflate(R.layout.actionbar_menu_view, null);
		mLvMenuListView = (ListView) view.findViewById(R.id.lv_menu);
		mAdapter = new MenuAdapter(ctx);
		mLvMenuListView.setAdapter(mAdapter);
		mLvMenuListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mListener.onItemSelected(position);
			}

		});

		return view;
	}

	private class MenuAdapter extends BaseAdapter {
		private int iconsId[] = { R.drawable.ic_nav_name,
				R.drawable.ic_nav_receivefile, R.drawable.ic_nav_setting,
				R.drawable.ic_nav_feedback, R.drawable.ic_nav_about };
		private String items[];

		public MenuAdapter(Context ctx) {
			items = ctx.getResources().getStringArray(R.array.menu);
		}

		@Override
		public int getCount() {
			return items.length;
		}

		@Override
		public Object getItem(int position) {
			return items[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mLayoutInflater.inflate(R.layout.menu_item_view,
						null);
				holder.tvItem = (TextView) convertView
						.findViewById(R.id.tv_menu_item);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			if (position == 0) {
				holder.tvItem.setText(GoTransferApplication
						.getInstance()
						.getSharedPreferences("SlefName", Context.MODE_PRIVATE)
						.getString(
								"username",
								GoTransferApplication.getInstance()
										.getSelfName()));
			} else {
				holder.tvItem.setText(items[position]);
			}

			holder.tvItem.setCompoundDrawablesWithIntrinsicBounds(
					iconsId[position], 0, 0, 0);

			return convertView;
		}
	}

	static class ViewHolder {
		TextView tvItem;
	}

	public void dismiss() {
		mPopupWindow.dismiss();
	}
	

	public void show(View parent) {
		mLvMenuListView.setAdapter(mAdapter);
		mAdapter.notifyDataSetChanged();
		mPopupWindow.showAsDropDown(parent);
	}

	public interface MenuItemSelectedListener {
		public void onItemSelected(int item);
	}
}