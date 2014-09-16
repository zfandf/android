package com.fan.appphonemanage;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class FileListAdapter extends BaseAdapter {

	private List<? extends HashMap<String, ?>> mData;
	
	private int mresource;
	
	private LayoutInflater mInflater;
	
	public FileListAdapter(Context context, int resource, List<? extends HashMap<String, ?>> list) {
		// TODO Auto-generated constructor stub
		mData = list;
		mresource = resource;
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
//		return super.getView(position, convertView, parent);
		View v = null;
		if (convertView == null) {
			v = mInflater.inflate(mresource, parent, false);
		} else {
			v = convertView;
		}
		return v;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mData.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

}
