package cn.m15.app.android.gotransfer.ui.activity;

import java.io.File;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.m15.app.android.gotransfer.GoTransferApplication;
import cn.m15.app.android.gotransfer.R;
import cn.m15.app.android.gotransfer.ui.fragment.dialog.FileOptionsDialog;
import cn.m15.app.android.gotransfer.utils.DialogUtil;
import cn.m15.app.android.gotransfer.utils.FileUtil;
import cn.m15.app.android.gotransfer.utils.ImageUtil;
import cn.m15.app.android.gotransfer.utils.images.ImageResizer;
import cn.m15.app.android.gotransfer.utils.images.LocalImageFetcher;
import cn.m15.app.android.gotransfer.utils.images.Utils;
import cn.m15.app.android.gotransfer.utils.images.VideoImageFetcher;
import cn.m15.gotransfer.sdk.database.Transfer.Conversation;
import cn.m15.gotransfer.sdk.database.TransferDBHelper;
import cn.m15.gotransfer.sdk.database.TransferDBHelper.TransferDatabaseListener;
import cn.m15.gotransfer.sdk.entity.TransferMsgPoller;
import cn.m15.gotransfer.sdk.net.ipmsg.MessageConst;
import cn.m15.gotransfer.sdk.net.ipmsg.MessageConst.FileType;
import cn.m15.gotransfer.sdk.net.ipmsg.MessageConst.Status;
import cn.m15.gotransfer.sdk.net.ipmsg.TUser;
import cn.m15.gotransfer.sdk.net.ipmsg.TcpTransferClient;
import cn.m15.gotransfer.sdk.net.ipmsg.TransferManager;
import cn.m15.gotransfer.sdk.net.ipmsg.UdpThreadManager;

public class ConversationActivity extends BaseActivity 
		implements AbsListView.OnScrollListener, TransferDatabaseListener {

	public static final String[] PROJECTION = new String[] { 
		Conversation._ID,
		Conversation.PACKET_ID, 
		Conversation.FILENAME, 
		Conversation.IS_SEND, 
		Conversation.FRIEND, 
		Conversation.FILETYPE, 
		Conversation.STATUS,
		Conversation.FILESIZE, 
		Conversation.CREATED, 
		Conversation.POSITION,
		Conversation.LOCALPATH, 
		Conversation.WHOLE_STATUS,
		Conversation.MAC_ADDRESS,
		Conversation.SRCPATH
	};
	public static final String SELECTION = Conversation.PACKET_ID + "=?";
	public static final String ORDER_BY = Conversation.CREATED + " DESC, " + Conversation._ID;

	private static final int INDEX_PACKAGE_ID = 1;
	private static final int INDEX_FILENAME = 2;
	private static final int INDEX_IS_SEND = 3;
	private static final int INDEX_FRIEND = 4;
	private static final int INDEX_FILETYPE = 5;
	private static final int INDEX_STATUS = 6;
	private static final int INDEX_FILESIZE = 7;
	private static final int INDEX_CREATED = 8;
	private static final int INDEX_POSITION = 9;
	private static final int INDEX_LOCALPATH = 10;
	private static final int INDEX_WHOLE_STATUS = 11;
	private static final int INDEX_MAC_ADDRESS = 12;
	private static final int INDEX_SRCPATH = 13;

	private ListView mListView;
	private ConversationAdapter mAdapter;
	private TransferMsgPoller mPoller;
	private LocalImageFetcher mImageFetcher;
	private VideoImageFetcher mVedioImageFetcher;
	private int mImageSize;

	private boolean mIsScrolling = false;
	
	private MyAsyncQueryHandler mQueryHandler;
	
	private HashMap<Long, Integer> mPostionToTransferSection;
	private HashMap<String, Integer> mPostionToDateSection;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_conversation);
		setTitle(R.string.transfer_history);
		mListView = (ListView) findViewById(R.id.conversation_list);
		mAdapter = new ConversationAdapter(this);
		mListView.setAdapter(mAdapter);
		mListView.setOnScrollListener(this);
		
		DisplayMetrics dm = getResources().getDisplayMetrics();
		mImageSize = dm.widthPixels
				- (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, dm);
		mImageSize /= 4;
		mImageFetcher = ImageUtil.createLocalImageFetcher(this, mImageSize);
		mImageFetcher.setLoadingImage(R.drawable.bg_history_picture);
		mVedioImageFetcher = ImageUtil.createVedioImageFetcher(this, mImageSize);
		mVedioImageFetcher.setLoadingImage(R.drawable.bg_history_video);
		
		initAsyncQueryHandler(getContentResolver(), mAdapter);
		
		mPoller = new TransferMsgPoller();
		mPoller.setTransferDatabaseListener(this);
		mPoller.start(this); 
	}
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
			int totalItemCount) {
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			mIsScrolling = false;
		} else {
			mIsScrolling = true;
		}
		
		// Pause fetcher to ensure smoother scrolling when flinging
        if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
            // Before Honeycomb pause image loading on scroll to help with performance
            if (!Utils.hasHoneycomb()) {
                mImageFetcher.setPauseWork(true);
            }
        } else {
            mImageFetcher.setPauseWork(false);
        }
	}

	@Override
	protected void onStart() {
		super.onStart();
		requery();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mImageFetcher.setExitTasksEarly(false);
		mVedioImageFetcher.setExitTasksEarly(false);
	}

	@Override
	public void onPause() {
		super.onPause();
		mImageFetcher.setPauseWork(false);
		mImageFetcher.setExitTasksEarly(true);
		mVedioImageFetcher.setPauseWork(false);
		mVedioImageFetcher.setExitTasksEarly(true);
	}
	
	@Override
	public void onTransferDataChanged() {
		requery();
	}

	@Override
	protected void onDestroy() {
		mPoller.setTransferDatabaseListener(null);
		mPoller.stop();
		resetAsyncQueryHandler();
		stopTransferFile();
		super.onDestroy();
	}
	
	private void stopTransferFile() {
		// fix NetWorkOnMainThreadException
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				/*
				 * 如果作为发送方，那么只有一种情况：
				 * 	1) 等待发送或发送中时退出，算做发送方取消发送，接收方应显示对方已取消
				 */
				TransferManager manager = TransferManager.getInstance();
				Collection<Long> packetNos = manager.getPacketNos();
				if (packetNos != null) {
					for (Long packetNo : manager.getPacketNos()) {
						if (packetNos != null) {
							TransferDBHelper.updateWholeTransferStatus(GoTransferApplication.getInstance(),
									packetNo, MessageConst.Status.CANCELLED, null);							
						}
					}
				}
				manager.stopServer(false);
						
				/*
				 * 如果作为接收方，那么分为两种情况：
				 * 	1) 正在接收时退出，算做接收方取消接收，发送方应显示对方已取消
				 *  2) 未确认接收时退出，算作接收方拒绝接收，发送方应显示拒绝接收
				 */
				HashMap<Long, TcpTransferClient> transferClientMap = manager.getTransferClientMap();
				if (transferClientMap != null && transferClientMap.size() > 0) {
					for (TcpTransferClient client : transferClientMap.values()) {
						if (client != null) {
							if (client.isStarted()) {
								TransferDBHelper.updateWholeTransferStatus(GoTransferApplication.getInstance(),
										client.packetNo, MessageConst.Status.CANCELLED, null);
								client.cancelReceiveFile();
							} else {
								TransferDBHelper.updateWholeTransferStatus(GoTransferApplication.getInstance(),
									client.packetNo, MessageConst.Status.REFUSED, null);
								UdpThreadManager.getInstance().refuseReceiveFiles(
									client.serverAddress.getHostAddress(), client.packetNo);							
							}
						}
					}
				}
				return null;
			}
		}.execute();
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
	
	final static class ViewHolder {
		// header
		LinearLayout headerLL;
		LinearLayout headerContentLL;
		TextView dateTv;
		TextView friendTv;
		TextView acceptTv;
		TextView refuseTv;
		TextView cancelTv;

		// item 
		RelativeLayout itemTransferRl;
		ImageView thumbImg;
		TextView nameTv;
		TextView sizeTv;
		TextView statusTv;
		ProgressBar progressBar;
		ImageView infoImg;
		
	}

	private class ConversationAdapter extends CursorAdapter {
		private Context mContext;
		private LayoutInflater mLayoutInflater;
		private int mDp42;
		private int mDp12;
		private int mDp2;
		private int mC3;
		private int mC8;

		public ConversationAdapter(Context context) {
			super(context, null, 0);
			mContext = context;
			mLayoutInflater = LayoutInflater.from(context);
			
			DisplayMetrics dm = context.getResources().getDisplayMetrics();
			mDp42 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 42, dm);
			mDp12 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, dm);
			mDp2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, dm);
			
			mC3 = mContext.getResources().getColor(R.color.c3);
			mC8 = mContext.getResources().getColor(R.color.c8);
		}
		
		@Override
		public void bindView(View view, Context ctx, Cursor c) {
			long date = c.getLong(INDEX_CREATED);
			final String dateStr = DateFormat.getDateInstance().format(new Date(date));
			final String friend = c.getString(INDEX_FRIEND);
			final String fileName = c.getString(INDEX_FILENAME);
			final long fileSize = c.getLong(INDEX_FILESIZE);
			final long transferSize = c.getLong(INDEX_POSITION);
			final int fileType = c.getInt(INDEX_FILETYPE);
			final int wholeStatus = c.getInt(INDEX_WHOLE_STATUS);
			final int status = c.getInt(INDEX_STATUS);
			final String localPath = c.getString(INDEX_LOCALPATH);
			final long packetNo = c.getLong(INDEX_PACKAGE_ID);
			final String macAddress = c.getString(INDEX_MAC_ADDRESS);
			final int isSend = c.getInt(INDEX_IS_SEND);
			final int position = c.getPosition();
			final String srcPath = c.getString(INDEX_SRCPATH);
			int progress = 0;
			if (fileSize != 0) {
				progress = (int) (100.0 * transferSize / fileSize);
			} else if (!TextUtils.isEmpty(srcPath)
					&& new File(srcPath).isDirectory()) {
				new AsyncTask<Void, Void, Void>() {
					
					@Override
					protected Void doInBackground(Void... params) {
						long size = FileUtil.getSize(new File(srcPath));
						TransferDBHelper.updateDirSize(mContext, packetNo, srcPath, size, ConversationActivity.this);
						return null;
					}
				}.execute();
			}
			
			final ViewHolder holder = (ViewHolder) view.getTag();
			
			Log.e("ConversationA", ">>> " + fileName+", "+c.getPosition());
			Integer transferSectionPos = mPostionToTransferSection.get(packetNo);
			if (transferSectionPos != null && transferSectionPos == position) {
				holder.headerLL.setVisibility(View.VISIBLE);
				
				Integer dateSectionPos = mPostionToDateSection.get(dateStr);
				if (dateSectionPos != null && dateSectionPos == position) {
					mPostionToDateSection.put(dateStr, position);
					holder.dateTv.setVisibility(View.VISIBLE);
					holder.dateTv.setText(dateStr);
				} else {
					holder.dateTv.setVisibility(View.GONE);
				}
				
				String text = "";
				if (isSend == 0) {
					text = getString(R.string.transfer_receive_from, friend);
				} else {
					text = getString(R.string.transfer_send_to, friend);
				}
				holder.friendTv.setText(text);
				
				// initialize header padding
				if (isSend == 0) {
					holder.headerContentLL.setPadding(mDp12, 0, mDp42, 0);
				} else {
					holder.headerContentLL.setPadding(mDp42, 0, mDp12, 0);
				}
				
				// 接收、拒绝、取消按钮
				if (wholeStatus == Status.WAIT_RECEIVE) {
					holder.acceptTv.setVisibility(View.VISIBLE);
					holder.refuseTv.setVisibility(View.VISIBLE);
					holder.cancelTv.setVisibility(View.GONE);
					holder.acceptTv.setOnClickListener(new View.OnClickListener() {
					
						@Override
						public void onClick(View v) {
							holder.acceptTv.setVisibility(View.GONE);
							holder.refuseTv.setVisibility(View.GONE);
							holder.cancelTv.setVisibility(View.VISIBLE);
							new AsyncTask<Void, Void, Void>() {
					
								@Override
								protected Void doInBackground(Void... params) {
									TransferDBHelper.changeWaitReceiveToReceiving(mContext,
										packetNo, MessageConst.Status.RECEIVING, ConversationActivity.this);
												
									TransferManager manager = TransferManager.getInstance();
									TcpTransferClient client = manager.getTransferClient(packetNo);
									if (client != null) {
										client.startClient(manager.getExecutorService());
									}
									return null;
								}
					
							}.execute();
						}
					});
					
					holder.refuseTv.setOnClickListener(new View.OnClickListener() {
					
						@Override
						public void onClick(View v) {
							holder.acceptTv.setVisibility(View.GONE);
							holder.refuseTv.setVisibility(View.GONE);
							holder.cancelTv.setVisibility(View.GONE);
							new AsyncTask<Void, Void, Void>() {
					
								@Override
								protected Void doInBackground(Void... params) {
									TransferDBHelper.updateWholeTransferStatus(mContext,
											packetNo, MessageConst.Status.REFUSED, ConversationActivity.this);
									UdpThreadManager.getInstance().refuseReceiveFiles(
											getIpByMacAddress(macAddress), packetNo);
									return null;
								}
					
							}.execute();
						}
					});
				} else if (wholeStatus == Status.WAIT_SEND 
						|| wholeStatus == Status.RECEIVING
						|| wholeStatus == Status.SENDING) {
					holder.acceptTv.setVisibility(View.GONE);
					holder.refuseTv.setVisibility(View.GONE);
					holder.cancelTv.setVisibility(View.VISIBLE);
				} else {
					holder.acceptTv.setVisibility(View.GONE);
					holder.refuseTv.setVisibility(View.GONE);
					holder.cancelTv.setVisibility(View.GONE);
				}
			} else {
				holder.headerLL.setVisibility(View.GONE);
			}
			
			// 取消按钮点击事件
			if (wholeStatus < Status.RECEIVE_FINISH) {
				holder.cancelTv.setOnClickListener(new View.OnClickListener() {
	
					@Override
					public void onClick(View v) {
						holder.cancelTv.setVisibility(View.GONE);
						new AsyncTask<Void, Void, Void>() {
	
							@Override
							protected Void doInBackground(Void... params) {
								TransferDBHelper.updateWholeTransferStatus(mContext,
										packetNo, MessageConst.Status.CANCELLED, ConversationActivity.this);
								
								TransferManager tm = TransferManager.getInstance();
								if (wholeStatus == MessageConst.Status.RECEIVING) {
									
									TcpTransferClient client = tm.getTransferClient(packetNo);
									if (client != null) {
										client.cancelReceiveFile();
									}
								} else if (wholeStatus == MessageConst.Status.SENDING) {
									tm.cancelSendFile(packetNo);
								} else if (wholeStatus == MessageConst.Status.WAIT_SEND) {
									tm.cancelSendFile(packetNo);
									UdpThreadManager.getInstance().cancelSendingWhenNotStart(getIpByMacAddress(macAddress), packetNo);
								}
								return null;
							}
	
						}.execute();
					}
				});
			}
			
			loadFileThumb(localPath, fileType, holder.thumbImg);
			holder.nameTv.setText(fileName.trim());
			holder.sizeTv.setText(Formatter.formatFileSize(mContext, fileSize));
			ViewGroup.MarginLayoutParams itemTransferLayoutParams = (ViewGroup.MarginLayoutParams) 
					holder.itemTransferRl.getLayoutParams();
			if (isSend == 0) {
				itemTransferLayoutParams.setMargins(mDp12, mDp2, mDp42, mDp2);
				holder.itemTransferRl.setBackgroundResource(R.drawable.bg_item_transfer_list_receive_selector);
			} else {
				itemTransferLayoutParams.setMargins(mDp42, mDp2, mDp12, mDp2);
				holder.itemTransferRl.setBackgroundResource(R.drawable.bg_item_transfer_list_send_selector);
			}
			holder.statusTv.setTextColor(mC8);
			holder.itemTransferRl.setOnClickListener(null);
			holder.infoImg.setOnClickListener(null);
			
			switch (status) {
			case Status.WAIT_SEND:
				holder.progressBar.setVisibility(View.GONE);
				holder.statusTv.setVisibility(View.VISIBLE);
				holder.statusTv.setText(R.string.transfer_wait_send);
				holder.infoImg.setVisibility(View.GONE);
				break;
			case Status.WAIT_RECEIVE:
				holder.progressBar.setVisibility(View.GONE);
				holder.statusTv.setVisibility(View.VISIBLE);
				holder.statusTv.setText(R.string.transfer_wait_receive);
				holder.infoImg.setVisibility(View.GONE);
				break;
			case Status.SENDING:
				holder.progressBar.setVisibility(View.VISIBLE);
				holder.progressBar.setProgress(progress);
				holder.statusTv.setText(R.string.transfer_sending);
				holder.statusTv.setVisibility(View.VISIBLE);
				holder.infoImg.setVisibility(View.GONE);
			 	break;
			case Status.RECEIVING:
				holder.progressBar.setVisibility(View.VISIBLE);
				holder.progressBar.setProgress(progress);
				holder.statusTv.setText(R.string.transfer_receiving);
				holder.statusTv.setVisibility(View.VISIBLE);
				holder.infoImg.setVisibility(View.GONE);
				break;
			case Status.RECEIVE_FINISH:
				holder.progressBar.setVisibility(View.GONE);
				holder.statusTv.setVisibility(View.GONE);
				holder.infoImg.setVisibility(View.VISIBLE);
				holder.itemTransferRl.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Log.d("ConversationActivity", "cat file:"+fileName+","+localPath);
						if (!TextUtils.isEmpty(localPath)) {
							if (new File(localPath).isFile()) {
								FileOptionsDialog.openFile(ConversationActivity.this, localPath);								
							} else {
								Toast.makeText(ConversationActivity.this, 
										R.string.use_file_brower, Toast.LENGTH_SHORT).show();
							}
						} else {
							Toast.makeText(ConversationActivity.this, 
									R.string.file_not_exist, Toast.LENGTH_SHORT).show();
						}
					}
				});
				holder.infoImg.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Log.d("ConversationActivity", "operations:"+fileName+","+localPath);
						if (!TextUtils.isEmpty(localPath)) {
							DialogUtil.showFileOptionsDialog(ConversationActivity.this, packetNo, localPath);
						} else {
							Toast.makeText(ConversationActivity.this, 
									R.string.file_not_exist, Toast.LENGTH_SHORT).show();
						}
					}
				});
				break;
			case Status.SEND_FINISH:
				holder.progressBar.setVisibility(View.GONE);
				holder.statusTv.setText(R.string.transfer_send_success);
				holder.statusTv.setVisibility(View.VISIBLE);
				holder.infoImg.setVisibility(View.GONE);
				break;
			case Status.CANCELLED:
				holder.progressBar.setVisibility(View.GONE);
				holder.statusTv.setText(R.string.transfer_cancelled);
				holder.statusTv.setVisibility(View.VISIBLE);
				holder.infoImg.setVisibility(View.GONE);
				break;
			case Status.CANCELLED_BY_PEER:
				holder.progressBar.setVisibility(View.GONE);
				holder.statusTv.setText(R.string.transfer_cancelled_by_peer);
				holder.statusTv.setVisibility(View.VISIBLE);
				holder.infoImg.setVisibility(View.GONE);
				break;
			case Status.REFUSED:
				holder.progressBar.setVisibility(View.GONE);
				holder.statusTv.setText(R.string.transfer_refused);
				holder.statusTv.setVisibility(View.VISIBLE);
				holder.statusTv.setTextColor(mC3);
				holder.infoImg.setVisibility(View.GONE);
				break;
			case Status.RECEIVE_FAILED:
				holder.progressBar.setVisibility(View.GONE);
				holder.statusTv.setText(R.string.transfer_receive_failed);
				holder.statusTv.setVisibility(View.VISIBLE);
				holder.infoImg.setVisibility(View.GONE);
				break;
			case Status.SEND_FAILED:
				holder.progressBar.setVisibility(View.GONE);
				holder.statusTv.setText(R.string.transfer_send_failed);
				holder.statusTv.setVisibility(View.VISIBLE);
				holder.infoImg.setVisibility(View.GONE);
				break;
			default:
				holder.progressBar.setVisibility(View.GONE);
				holder.statusTv.setText(R.string.transfer_status_unknow);
				holder.statusTv.setVisibility(View.VISIBLE);
				holder.infoImg.setVisibility(View.GONE);
				break;
			}
			
		}

		@Override
		public View newView(Context ctx, Cursor c, ViewGroup vg) {
			View view = mLayoutInflater.inflate(R.layout.item_conversation, null);
			ViewHolder holder = new ViewHolder();
			// header
			holder.headerLL = (LinearLayout) view.findViewById(R.id.ll_conversation_header);
			holder.headerContentLL = (LinearLayout) view.findViewById(R.id.ll_conversation_header_content);
			holder.dateTv = (TextView) view.findViewById(R.id.tv_date);
			holder.friendTv = (TextView) view.findViewById(R.id.tv_friend);
			holder.acceptTv = (TextView) view.findViewById(R.id.tv_accept);
			holder.refuseTv = (TextView) view.findViewById(R.id.tv_refuse);
			holder.cancelTv = (TextView) view.findViewById(R.id.tv_cancel);
			// item
			holder.itemTransferRl = (RelativeLayout) view.findViewById(R.id.rl_item_transfer_file);
			holder.thumbImg = (ImageView) view.findViewById(R.id.img_file_thumb);
			holder.nameTv = (TextView) view.findViewById(R.id.tv_file_name);
			holder.sizeTv = (TextView) view.findViewById(R.id.tv_file_size);
			holder.statusTv = (TextView) view.findViewById(R.id.tv_status);
			holder.progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
			holder.infoImg = (ImageView) view.findViewById(R.id.img_file_info);
			view.setTag(holder);
			return view;
		}

		private String getIpByMacAddress(String macAddress) {
			String ipAddress = null;
			for (TUser user : UdpThreadManager.getInstance().getUsers().values()) {
				if (macAddress.equals(user.getMac())) {
					ipAddress = user.getIp();
					break;
				}
			}
			return ipAddress;
		}

		@Override
		public void notifyDataSetChanged() {
			// 滚动中，并且传输没有完成，不更新UI; 滚动中，但传输没有完成，更新UI; 
			if (mIsScrolling && hasTransfer()) return;
			super.notifyDataSetChanged();
		}

	}
	
	private void loadFileThumb(String path, int fileType, ImageView ivThumb) {
		if (ivThumb == null) return;
		ImageResizer imageFetcher = fileType == FileType.PICTURE ? mImageFetcher : mVedioImageFetcher;
		switch (fileType) {
		case FileType.PICTURE:
			ivThumb.setImageResource(R.drawable.bg_history_picture);
			if (imageFetcher != null) {
				imageFetcher.loadImage(path, ivThumb);
			}
			break;
		case FileType.VIDEO:
			ivThumb.setImageResource(R.drawable.bg_history_video);
			if (imageFetcher != null) {
				imageFetcher.loadImage(path, ivThumb);
			}
			break;
		case FileType.MUSIC:
			ivThumb.setImageResource(R.drawable.bg_history_music);
			break;
		case FileType.DIR:
			ivThumb.setImageResource(R.drawable.bg_history_folder);
			break;
		case FileType.OTHERS:
			ivThumb.setImageResource(R.drawable.bg_history_file);
			break;
		case FileType.APP:
			ivThumb.setBackgroundColor(Color.TRANSPARENT);
			ivThumb.setImageDrawable(ImageUtil.getApkImage(this, getPackageManager(), path, false));
			break;
		default:
			break;
		}
	}
	
	public void requery() {
		if (mQueryHandler != null) {
			mQueryHandler.startQuery(0, null, 
					Conversation.CONTENT_URI, 
					ConversationActivity.PROJECTION,
					null, 
					null, 
					ConversationActivity.ORDER_BY);		
		}
	}
	
	public void initAsyncQueryHandler(ContentResolver cr, CursorAdapter adapter) {
		if (mQueryHandler == null) {
			mQueryHandler = new MyAsyncQueryHandler(cr, adapter);	
			mPostionToTransferSection = new HashMap<Long, Integer>();
			mPostionToDateSection = new HashMap<String, Integer>();
		}
	}
	
	public void resetAsyncQueryHandler() {
		if (mQueryHandler != null) {
			mQueryHandler.mAdapter.changeCursor(null);
			mQueryHandler = null;			
		}
	}
	
	public boolean hasTransfer() {
		if (mQueryHandler != null) {
			return mQueryHandler.mHasTransfer;
		}
		return false;
	}
	
	@SuppressLint("HandlerLeak")
	public class MyAsyncQueryHandler extends AsyncQueryHandler {
		CursorAdapter mAdapter;
		boolean mHasTransfer;
		Object mLock = new Object();

		public MyAsyncQueryHandler(ContentResolver cr, CursorAdapter adapter) {
			super(cr);
			mAdapter = adapter;
		}
		
		@Override
		protected void onQueryComplete(int token, Object cookie, final Cursor cursor) {
			if (cursor != null) {
				mHasTransfer = false;
				mPostionToDateSection.clear();
				mPostionToTransferSection.clear();
				while (cursor.moveToNext()) {
					int position = cursor.getPosition();
					long packetNo = cursor.getLong(INDEX_PACKAGE_ID);
					long date = cursor.getLong(INDEX_CREATED);
					String dateStr = DateFormat.getDateInstance().format(new Date(date));
					
					if (!mPostionToTransferSection.containsKey(packetNo)) {
						mPostionToTransferSection.put(packetNo, position);
						
						if (!mPostionToDateSection.containsKey(dateStr)) {
							mPostionToDateSection.put(dateStr, position);
						}
					}
					
					int wholeStatus = cursor.getInt(INDEX_WHOLE_STATUS);
					if (wholeStatus < MessageConst.Status.RECEIVE_FINISH) {
						mHasTransfer = true;
					}
				}
			}
			mAdapter.changeCursor(cursor);
		}
		
	}
}
