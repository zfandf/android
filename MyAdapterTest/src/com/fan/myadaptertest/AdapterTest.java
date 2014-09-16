package com.fan.myadaptertest;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AdapterTest extends BaseAdapter {

	private List<? extends Map<String, ?>> mData;
//	private Context mcontext;
	private String[] mfrom;
	private int[] mto;
	private LayoutInflater mInflater;
	private int mresource;
	
	public AdapterTest(Context context, int resource, String[] from, int[] to,
			List<? extends Map<String, ?>> objects) {
		// TODO Auto-generated constructor stub
		mData = objects;
//		mcontext = context;
		mfrom = from;
		mto = to;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mresource = resource;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mData.size();
	}
	
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mData.get(position);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.i("<<<position>>>", ""+convertView);
		View v;
		if (convertView == null) {
			v = mInflater.inflate(mresource, parent, false);
		} else {
			v = convertView;
		}
		// TODO Auto-generated method stub
		return createItemView(position, v, parent);
	}
	
	public View createItemView(int position, View convertView, ViewGroup parent) {
		Log.i("<<<position>>>", ""+position);
		
		for (int i = 0; i < mfrom.length; i++) {
			TextView v1 = (TextView)convertView.findViewById(mto[i]);
			String s = (String) mData.get(position).get(mfrom[i]);
			v1.setText(s);
		}
        return convertView;
	}
}
