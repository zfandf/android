package cn.m15.app.android.gotransfer.ui.fragment.dialog;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import cn.m15.app.android.gotransfer.R;
import cn.m15.app.android.gotransfer.net.ipmsg2.User;

public class SendFilesDialog extends CommonDialogFragment {
	private ListView mListView;
	private ReceiverListAdapter mListAdapter;
	private ArrayList<User> mReceiverList;
	private int mBtnTextEnableColor;
	private int mBtnTextDisableColor;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelableArrayList("receivers_list", mReceiverList);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mReceiverList = getArguments().getParcelableArrayList("receivers");
		for (User user : mReceiverList) {
			user.setStatus(0);
		}
	}
	
	@Override
	protected void onContentCreated(FrameLayout flContent) {
		mBtnTextEnableColor = getActivity().getResources().getColor(R.color.c4);
		mBtnTextDisableColor = getActivity().getResources().getColor(R.color.btn_text_disable);
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_send_files_content, null);
		mListView = (ListView) view.findViewById(R.id.lv_receivers);
		mListAdapter = new ReceiverListAdapter(getActivity());
		mListAdapter.setData(mReceiverList);
		mListView.setAdapter(mListAdapter);
		flContent.addView(view);
	}
	
	@Override
	protected void initButtons(FrameLayout flButtons) {
		super.initButtons(flButtons);
		mLeftBtn.setEnabled(false);
		mLeftBtn.setTextColor(mBtnTextDisableColor);
	}
	
	@Override
	public void onClick(View v) {
		if (mListener != null) {
			for (User user : mReceiverList) {
				Log.d("SendFilesDialog", "onclick >>> "+user.getUserName()+","+user.getStatus());
			}
		}
		super.onClick(v);
	}
	
	public ArrayList<User> getSelectedReceivers() {
		ArrayList<User> selectedReceivers = new ArrayList<User>();
		for (User user : mReceiverList) {
			if (user.getStatus() != 0) {
				selectedReceivers.add(user);				
			}
		}
		return selectedReceivers;
	}
	
	public void notifyButtonChangedEnable(boolean enabled) {
		mLeftBtn.setEnabled(enabled);
		if (enabled) {
			mLeftBtn.setTextColor(mBtnTextEnableColor);
		} else {
			mLeftBtn.setTextColor(mBtnTextDisableColor);
		}
	}
	
	public class ReceiverListAdapter extends BaseAdapter {
		private ArrayList<User> mData;
		private LayoutInflater mInflater;
		private int mSelectedReceiversNumber;
		
		public ReceiverListAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
			mData = new ArrayList<User>();
		}
		
		public void setData(ArrayList<User> receiverList) {
			mData = receiverList;
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mData != null ? mData.size() : 0;
		}

		@Override
		public User getItem(int position) {
			return mData != null ? mData.get(position) : null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item_dialog_receiver, null);
				holder = new ViewHolder();
				holder.mCheckBox = (CheckBox) convertView.findViewById(R.id.check_box);
				holder.mReceiverNameTv = (TextView) convertView.findViewById(R.id.tv_receiver_name);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			final User item = getItem(position);
			convertView.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					boolean checked = holder.mCheckBox.isChecked();
					holder.mCheckBox.setChecked(!checked);
					item.setStatus(checked ? 0 : 1);
					if (checked) {
						mSelectedReceiversNumber--;						
					} else {
						mSelectedReceiversNumber++;
					}
					notifyButtonChangedEnable(mSelectedReceiversNumber > 0 ? true : false);
				}
			});
			
			holder.mReceiverNameTv.setText(item.getUserName());
			if (item.getStatus() == 0) {
				holder.mCheckBox.setChecked(false);
			} else {
				holder.mCheckBox.setChecked(true);
			}
			return convertView;
		}
		
	}
	
	static class ViewHolder {
		CheckBox  mCheckBox;
		TextView mReceiverNameTv;
	}
}
