package cn.m15.app.android.gotransfer.ui.activity;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.CursorTreeAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.m15.app.android.gotransfer.Const;
import cn.m15.app.android.gotransfer.R;
import cn.m15.app.android.gotransfer.database.Transfer;
import cn.m15.app.android.gotransfer.database.Transfer.Conversation;
import cn.m15.app.android.gotransfer.database.TransferDBHelper;
import cn.m15.app.android.gotransfer.enity.FileTransferMsgPoller;
import cn.m15.app.android.gotransfer.net.ipmsg2.MessageConst.Status;
import cn.m15.app.android.gotransfer.net.ipmsg2.TcpFileTransferClient;
import cn.m15.app.android.gotransfer.net.ipmsg2.User;
import cn.m15.app.android.gotransfer.ui.fragment.dialog.FileOptionsDialog;
import cn.m15.app.android.gotransfer.utils.DialogUtil;

public class ConversationActivity2 extends BaseActivity2 
		implements LoaderCallbacks<Cursor>, AbsListView.OnScrollListener {

	private static final String[] PARTITION_PROJECT = new String[] { 
			Conversation._ID,
			Conversation.PACKET_ID, 
			Conversation.FRIEND, 
			Conversation.WHOLE_STATUS,
			Conversation.MAC_ADDRESS 
	};
	private static final String PARTITION_SELECTION = "0==0) GROUP BY (" + Conversation.PACKET_ID;
	private static final String PARTITION_ORDER = Conversation._ID + " DESC";
	private static final int INDEX_PARTITION_PACKAGE_ID = 1;
	private static final int INDEX_PARTITION_FRIEND = 2;
	private static final int INDEX_PARTITION_WHOLE_STATUS = 3;
	private static final int INDEX_PARTITION_MAC_ADDRESS = 4;

	private static final String[] CONVERSATION_PROJECTION = new String[] { 
			Conversation._ID,
			Conversation.PACKET_ID, 
			Conversation.FILENAME, 
			Conversation.SRCPATH,
			Conversation.IS_SEND, 
			Conversation.FRIEND, 
			Conversation.FILETYPE, 
			Conversation.STATUS,
			Conversation.FILESIZE, 
			Conversation.CREATED, 
			Conversation.POSITION,
			Conversation.LOCALPATH, 
			Conversation.REMAINING_PATHS 
	};
	private static final String CONVERSATION_SELECTION = Conversation.PACKET_ID + "=?";
	private static final String CONVERSATION_ORDER = Conversation._ID + " ASC";

	private static final int INDEX_CONVERSATION_PACKAGE_ID = 1;
	private static final int INDEX_CONVERSATION_FILENAME = 2;
	private static final int INDEX_CONVERSATION_SRCPATH = 3;
	private static final int INDEX_CONVERSATION_IS_SEND = 4;
	private static final int INDEX_CONVERSATION_FRIEND = 5;
	private static final int INDEX_CONVERSATION_FILETYPE = 6;
	private static final int INDEX_CONVERSATION_STATUS = 7;
	private static final int INDEX_CONVERSATION_FILESIZE = 8;
	private static final int INDEX_CONVERSATION_CREATED = 9;
	private static final int INDEX_CONVERSATION_POSITION = 10;
	private static final int INDEX_CONVERSATION_LOCALPATH = 11;
	private static final int INDEX_CONVERSATION_REMAINING_PATHS = 12;

	private ExpandableListView mListView;
	private ConversationAdapter2 mAdapter;
	private FileTransferMsgPoller mPoller;
	
	private boolean mIsFlashListView = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_conversation);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		mListView = (ExpandableListView) findViewById(R.id.conversation_list);
		mAdapter = new ConversationAdapter2(this);
		mListView.setAdapter(mAdapter);
		mListView.setOnScrollListener(this);

		mPoller = new FileTransferMsgPoller();
		mPoller.start(this);
	}
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			mIsFlashListView = true;
			Log.d("Conversation", "onScrollStateChanged idle:"+mIsFlashListView);
		} else {
			mIsFlashListView = false;
			Log.d("Conversation", "onScrollStateChanged no idle:"+mIsFlashListView);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		getSupportLoaderManager()
			.initLoader(Const.LOADER_CONVERSATION, null, this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		getSupportLoaderManager()
			.destroyLoader(Const.LOADER_CONVERSATION);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d("zhpudes", "Conver2_onDestroy");
		if (transferServer != null) {
			transferServer.stopServer();
			transferServer = null;
		}
		if (transferClientMap != null && transferClientMap.size() > 0) {
			for (TcpFileTransferClient client : transferClientMap.values()) {
				if (client != null) {
					client.stopClient(true);
				}
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return true;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
		return new CursorLoader(this, Transfer.Conversation.CONTENT_URI,
				PARTITION_PROJECT, PARTITION_SELECTION, null, PARTITION_ORDER);
	}

	// for test
	private long mTimestamp;

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (mTimestamp == 0) {
			mTimestamp = System.currentTimeMillis();
			Log.e("Conversation", "onLoadFinished: " + 0);
		} else {
			mTimestamp = System.currentTimeMillis() - mTimestamp;
			Log.e("Conversation", "onLoadFinished: " + mTimestamp);
			mTimestamp = System.currentTimeMillis();
		}

		mAdapter.changeCursor(cursor);
		int groupPos = cursor.getCount();
		while (groupPos >= 0) {
			mListView.expandGroup(groupPos);
			groupPos--;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.changeCursor(null);
	}

	final static class ViewHolder {
		ImageView ivFileThumb;
		TextView tvFileName;
		TextView tvFileSize;
		TextView tvStatus;
		ProgressBar pbProgress;
	}

	private class ConversationAdapter2 extends CursorTreeAdapter {
		private Context mContext;
		private LayoutInflater mLayoutInflater;

		public ConversationAdapter2(Context context) {
			super(null, context);
			mContext = context;
			mLayoutInflater = LayoutInflater.from(context);
		}

		@Override
		protected Cursor getChildrenCursor(Cursor groupCursor) {
			String packetId = groupCursor.getString(INDEX_PARTITION_PACKAGE_ID);
			return mContext.getContentResolver().query(
					Conversation.CONTENT_URI, CONVERSATION_PROJECTION,
					CONVERSATION_SELECTION, new String[] { packetId },
					CONVERSATION_ORDER);
		}

		@Override
		protected View newGroupView(Context context, Cursor cursor,
				boolean isExpanded, ViewGroup parent) {
			return mLayoutInflater.inflate(R.layout.header_conversation,
					parent, false);
		}

		@Override
		protected void bindGroupView(View view, Context context, Cursor cursor,
				boolean isExpanded) {
			TextView friendTv = (TextView) view.findViewById(R.id.tv_friend);
			Button acceptBtn = (Button) view.findViewById(R.id.btn_accept);
			Button refuseBtn = (Button) view.findViewById(R.id.btn_refuse);
			Button cancelBtn = (Button) view.findViewById(R.id.btn_cancel);

			final long packetNo = cursor.getLong(INDEX_PARTITION_PACKAGE_ID);
			final String macAddress = cursor.getString(INDEX_PARTITION_MAC_ADDRESS);
			
			String friend = cursor.getString(INDEX_PARTITION_FRIEND);
			friendTv.setText(friend);
			
			acceptBtn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					TransferDBHelper.changeWaitReceiveToReceiving(mContext,
							packetNo, Status.RECEIVING);
					TcpFileTransferClient client = transferClientMap.get(packetNo);
					if (client != null) {
						client.startClient(THREAD_POOL);
					}
				}
			});

			refuseBtn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					TransferDBHelper.updateWholeTransferStatus(mContext,
							packetNo, Status.REFUSED);
					netThreadHelper.refuseReceiveFiles(
							getIpByMacAddress(macAddress), packetNo);
				}
			});

			final int wholeStatus = cursor.getInt(INDEX_PARTITION_WHOLE_STATUS);
			if (wholeStatus == Status.WAIT_RECEIVE) {
				acceptBtn.setVisibility(View.VISIBLE);
				refuseBtn.setVisibility(View.VISIBLE);
				cancelBtn.setVisibility(View.GONE);
			} else if (wholeStatus == Status.WAIT_SEND
					|| wholeStatus == Status.RECEIVING
					|| wholeStatus == Status.SENDING) {
				acceptBtn.setVisibility(View.GONE);
				refuseBtn.setVisibility(View.GONE);
				cancelBtn.setVisibility(View.VISIBLE);
			} else {
				acceptBtn.setVisibility(View.GONE);
				refuseBtn.setVisibility(View.GONE);
				cancelBtn.setVisibility(View.GONE);
			}

			cancelBtn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					TransferDBHelper.updateWholeTransferStatus(mContext,
							packetNo, Status.CANCELLED);
					if (wholeStatus == Status.RECEIVING) {
						TcpFileTransferClient client = transferClientMap.get(packetNo);
						if (client != null) {
							client.cancelReceiveFile();
						}
					} else if (wholeStatus == Status.SENDING) {
						if (transferServer != null) {
							transferServer.cancelSendFile(packetNo);
						}
					} else if (wholeStatus == Status.WAIT_SEND) {
						if (transferServer != null) {
							transferServer.cancelSendFile(packetNo);
							netThreadHelper.cancelSendingWhenNotStart(
									getIpByMacAddress(macAddress), packetNo);
						}
					}
				}
			});
		}

		private String getIpByMacAddress(String macAddress) {
			String ipAddress = null;
			for (User user : netThreadHelper.getUsers().values()) {
				if (macAddress.equals(user.getMac())) {
					ipAddress = user.getIp();
					break;
				}
			}
			return ipAddress;
		}

		@Override
		protected View newChildView(Context context, Cursor cursor,
				boolean isLastChild, ViewGroup parent) {
			View view = mLayoutInflater.inflate(
					R.layout.item_transfer_file_list, null);
			ViewHolder holder = new ViewHolder();
			holder.ivFileThumb = (ImageView) view
					.findViewById(R.id.iv_file_thumb);
			holder.tvFileName = (TextView) view.findViewById(R.id.tv_file_name);
			holder.tvFileSize = (TextView) view.findViewById(R.id.tv_file_size);
			holder.tvStatus = (TextView) view
					.findViewById(R.id.tv_transfer_status);
			holder.pbProgress = (ProgressBar) view
					.findViewById(R.id.pb_progress);
			view.setTag(holder);
			return view;
		}

		@Override
		protected void bindChildView(View view, Context context, Cursor cursor,
				boolean isLastChild) {
			ViewHolder holder = (ViewHolder) view.getTag();

			final String localPath = cursor.getString(INDEX_CONVERSATION_LOCALPATH);
			String fileName = cursor.getString(INDEX_CONVERSATION_FILENAME);
			int status = cursor.getInt(INDEX_CONVERSATION_STATUS);
			long fileSize = cursor.getLong(INDEX_CONVERSATION_FILESIZE);
			long transferSize = cursor.getLong(INDEX_CONVERSATION_POSITION);
			int progress = 0;
			if (fileSize != 0) {
				progress = (int) (100.0 * transferSize / fileSize);
			}
			
			holder.tvFileName.setText(fileName);
			holder.tvFileSize.setText(Formatter.formatFileSize(mContext, fileSize));

			view.setOnClickListener(null);
			view.setOnLongClickListener(null);

			switch (status) {
			case Status.WAIT_SEND:
				holder.pbProgress.setVisibility(View.GONE);
				holder.tvFileSize.setVisibility(View.VISIBLE);
				holder.tvStatus.setText(R.string.transfer_wait_send);
				break;
			case Status.WAIT_RECEIVE:
				holder.pbProgress.setVisibility(View.GONE);
				holder.tvFileSize.setVisibility(View.VISIBLE);
				holder.tvStatus.setText(R.string.transfer_wait_receive);
				break;
			case Status.SENDING:
				holder.pbProgress.setVisibility(View.VISIBLE);
				holder.pbProgress.setProgress(progress);
				holder.tvFileSize.setVisibility(View.GONE);
				holder.tvStatus.setText(R.string.transfer_sending);
				break;
			case Status.RECEIVING:
				holder.pbProgress.setVisibility(View.VISIBLE);
				holder.pbProgress.setProgress(progress);
				holder.tvFileSize.setVisibility(View.GONE);
				holder.tvStatus.setText(R.string.transfer_receiving);
				break;
			case Status.RECEIVE_FINISH:
				holder.pbProgress.setVisibility(View.GONE);
				holder.tvFileSize.setVisibility(View.VISIBLE);
				holder.tvStatus.setText(R.string.transfer_accept_success);
				view.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (!TextUtils.isEmpty(localPath)) {
							FileOptionsDialog.openFile(
									ConversationActivity2.this, localPath);
						}
					}
				});
				view.setOnLongClickListener(new View.OnLongClickListener() {

					@Override
					public boolean onLongClick(View v) {
						if (!TextUtils.isEmpty(localPath)) {
							DialogUtil.showFileOptionsDialog(
									ConversationActivity2.this, localPath);
						}
						return true;
					}
				});
				break;
			case Status.SEND_FINISH:
				holder.pbProgress.setVisibility(View.GONE);
				holder.tvFileSize.setVisibility(View.VISIBLE);
				holder.tvStatus.setText(R.string.transfer_send_success);
				break;
			case Status.CANCELLED:
				holder.pbProgress.setVisibility(View.GONE);
				holder.tvFileSize.setVisibility(View.VISIBLE);
				holder.tvStatus.setText(R.string.transfer_cancelled);
				break;
			case Status.CANCELLED_BY_PEER:
				holder.pbProgress.setVisibility(View.GONE);
				holder.tvFileSize.setVisibility(View.VISIBLE);
				holder.tvStatus.setText(R.string.transfer_cancelled_by_peer);
				break;
			case Status.REFUSED:
				holder.pbProgress.setVisibility(View.GONE);
				holder.tvFileSize.setVisibility(View.VISIBLE);
				holder.tvStatus.setText(R.string.transfer_refused);
				break;
			case Status.RECEIVE_FAILED:
				holder.pbProgress.setVisibility(View.GONE);
				holder.tvFileSize.setVisibility(View.VISIBLE);
				holder.tvStatus.setText(R.string.receive_file_failed);
				break;
			case Status.SEND_FAILED:
				holder.pbProgress.setVisibility(View.GONE);
				holder.tvFileSize.setVisibility(View.VISIBLE);
				holder.tvStatus.setText(R.string.send_file_failed);
				break;
			default:
				holder.pbProgress.setVisibility(View.GONE);
				holder.tvFileSize.setVisibility(View.VISIBLE);
				holder.tvStatus.setText(R.string.transfer_status_unknow);
				break;
			}
		}
		
		@Override
		public void notifyDataSetChanged(boolean releaseCursors) {
			if (!mIsFlashListView) return;
			super.notifyDataSetChanged(releaseCursors);
		}

	}
}
