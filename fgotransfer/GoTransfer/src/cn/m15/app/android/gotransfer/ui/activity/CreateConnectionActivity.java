package cn.m15.app.android.gotransfer.ui.activity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.m15.app.android.gotransfer.R;
import cn.m15.app.android.gotransfer.ui.fragment.dialog.CommonDialogFragment;
import cn.m15.app.android.gotransfer.ui.fragment.dialog.CommonDialogFragment.DialogButtonClickListener;
import cn.m15.app.android.gotransfer.ui.widget.animation.MySurfaceView;
import cn.m15.app.android.gotransfer.ui.widget.animation.Reciver;
import cn.m15.app.android.gotransfer.ui.widget.animation.WaterBackground;
import cn.m15.app.android.gotransfer.utils.DialogUtil;
import cn.m15.gotransfer.sdk.ConfigManager;
import cn.m15.gotransfer.sdk.entity.TransferFile;
import cn.m15.gotransfer.sdk.entity.TransferFileManager;
import cn.m15.gotransfer.sdk.net.ipmsg.TransferManager;
import cn.m15.gotransfer.sdk.net.ipmsg.UdpThreadManager;
import cn.m15.gotransfer.sdk.net.ipmsg.UdpThreadManager.ConnectedUserChangedListener;
import cn.m15.gotransfer.sdk.net.ipmsg.TUser;
import cn.m15.gotransfer.sdk.net.wifi.WifiApConnector;
import cn.m15.gotransfer.sdk.net.wifi.WifiApConnector.CreateWifiApResultListener;

public class CreateConnectionActivity extends BaseActivity implements
		View.OnClickListener, DialogButtonClickListener,
		CreateWifiApResultListener, ConnectedUserChangedListener {

	protected static final FragmentActivity FragmentActivity = null;
	private boolean mIsFromSendBtn = false;
	private TextView mIscreatingTv;
	private TextView mNotifyfriendsTv;
	private TextView mCreateSuccessHintTv;
	private Button mBtnCloseConnection;//断开链接
	private Button mBtnFriendsInstall;//给好友安装
	private Button mBtnSendChecked;//中间发送按钮
	private Button mBtnSendUnChecked;//中间发送按钮未选中状态
	private Timer mTimer;
	private int mPointX,mPointY;//圆心坐标   
	private MySurfaceView mView;
	private List<Reciver> mList;//接收者集合
	private int mWth;//屏幕宽
	private int mHei;//屏幕高
	public static List<Reciver> mSelectRecivers;//被选中的接收者
	private Map<String,TUser> mUserMap;//所有 接收者
	private ArrayList<TransferFile> mTransferFiles; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// 创建连接之前清空以前保存的user
		UdpThreadManager.getInstance().getUsers().clear();
		super.onCreate(savedInstanceState);
		setTitle(R.string.create_connection);
		DisplayMetrics dm = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay()
		.getMetrics(dm);
		//得到屏幕宽高
		mWth = dm.widthPixels;
		mHei = dm.heightPixels;
		

		//setContentView(R.layout.activity_create_connection);
		//getSupportActionBar().hide();
		mList=new ArrayList<Reciver>();
		mView=initMysurfaceView(mList);
	   	setContentView(mView);
	   	LinearLayout.LayoutParams xmlLayout = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
	   	LayoutInflater inflater=(LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
		View view2=inflater.inflate(R.layout.activity_create_connection, null);
		addContentView(view2, xmlLayout);
		mIsFromSendBtn = getIntent().getBooleanExtra("is_from_send_btn", false);
		//创建连接
//		mCreateConnectionStatusTv = (TextView) findViewById(R.id.tv_create_connection_status);
		//正在创建
		mIscreatingTv=(TextView) findViewById(R.id.tv_create_iscreating);
		//创建成功
		mCreateSuccessHintTv = (TextView) findViewById(R.id.tv_create_success_hint);
		//通知好友
		mNotifyfriendsTv=(TextView) findViewById(R.id.tv_notify_friends);
		
		
		mApConnector.addCreateWifiApResultListener(this);
		UdpThreadManager.getInstance().addConnectedUserChangedListener(this);
		mApConnector.createWifiAp();

		

		mBtnCloseConnection = (Button) findViewById(R.id.btn_close_connection);
		mBtnFriendsInstall = (Button) findViewById(R.id.btn_friend_install);
		//中间发送按钮
		mBtnSendChecked=(Button) findViewById(R.id.btn_send_checked);
		mBtnSendUnChecked=(Button) findViewById(R.id.btn_send_unchecked);
		mBtnSendUnChecked.setVisibility(View.VISIBLE);
		mBtnSendChecked.setVisibility(View.GONE);
		mBtnSendUnChecked.setVisibility(View.GONE);
		mBtnSendChecked.setOnClickListener(this);
		mView.setmBtn(mBtnSendChecked);
		mView.setmBtnUnChecked(mBtnSendUnChecked);
		
		mBtnCloseConnection.setVisibility(View.GONE);
		mBtnFriendsInstall.setVisibility(View.VISIBLE);
		mBtnCloseConnection.setOnClickListener(this);
		mBtnFriendsInstall.setOnClickListener(this);
	}
	public MySurfaceView initMysurfaceView(List<Reciver> list){
		//获得一个头像
				
		Bitmap imagebac=BitmapFactory.decodeResource(this.getResources(),
				R.drawable.img_send_wave);
	   	WaterBackground back=new WaterBackground(imagebac, mWth, mHei);
	   	//显示自定义的SurfaceView视图
	   	
	   	MySurfaceView view=new MySurfaceView(this, back, list);
	
	   	return view;
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mApConnector.removeCreateWifiApResultListener(this);
		UdpThreadManager.getInstance().removeConnectedUserChangedListener(this);
		if (mTimer != null) {
			mTimer.cancel();
			mTimer.purge();			
		}
	}

	public void onWaitTimeout() {
		mTimer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				mApConnector.mWaitConnectTimeoutNum++;
				DialogUtil.showWaitJoinTimeTooLongDialog(CreateConnectionActivity.this);
			}
		};
		mTimer.schedule(task, WifiApConnector.WAIT_CONNECT_TIMEOUT);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_close_connection:
			disconnect();
			break;
		case R.id.btn_friend_install:
			startActivity(new Intent(this, InviteFriendsActivity.class));
			break;
		case R.id.btn_send_checked:
			
			Map<String,TUser> map=new HashMap<String,TUser>();
			
			if(mSelectRecivers!=null&&mSelectRecivers.size()>0){
				
				for(int i=0;i<mSelectRecivers.size();i++){
					Reciver reciverSelected=mSelectRecivers.get(i);
					String ip=reciverSelected.getIp();
					//根据ip得到被点击者
					map.put(ip, mUserMap.get(ip));
				}
				
				TransferManager.getInstance().startTransferFiles(this, map.values(), mTransferFiles);
				TransferFileManager.getInstance().clear();				
				Intent intent = new Intent(CreateConnectionActivity.this, ConversationActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				CreateConnectionActivity.this.startActivity(intent);
				CreateConnectionActivity.this.finish();
			}
		
			break;
		}
	}

	// 断开连接
	private void disconnect() {
		closeWifiConnection();
		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("isCloseConnection", true);
		startActivity(intent);
		finish();
	}

	@Override
	public void onDialogButtonClick(CommonDialogFragment dialog, int which,
			String tag) {
		dialog.dismiss();
		if (DialogUtil.DIALOG_WAIT_JOIN_TIME_TOO_LONG.equals(tag)
				&& which == DialogInterface.BUTTON_POSITIVE) {
			disconnect();
		}
	}

	@Override
	public void createWifiApStarted() {
		Log.d("zhpucreateconn", "createWifiApStarted");
		mCreateSuccessHintTv.setText(R.string.creating_wifi);
		
	}

	@Override
	public void createWifiApSuccess() {
		Log.d("zhpucreateconn", "createWifiSuccess");
		
		//正在创建隐藏
		mIscreatingTv.setVisibility(View.GONE);
//		//创建成功显示
		mCreateSuccessHintTv.setVisibility(View.VISIBLE);
		mCreateSuccessHintTv.setText(R.string.create_success);
		//断开连接显示
		mBtnCloseConnection.setVisibility(View.VISIBLE);
		//给好友安装显示
		mBtnFriendsInstall.setVisibility(View.VISIBLE);
		//通知好友显示
		mNotifyfriendsTv.setVisibility(View.VISIBLE);
		String sNotify = getResources().getString(R.string.create_success_hint); 
		String sFinalstr = String.format(sNotify, ConfigManager.getInstance().getSelfName());
		//设置机器名称高亮
		int start=sFinalstr.indexOf(ConfigManager.getInstance().getSelfName());
		SpannableStringBuilder style=new SpannableStringBuilder(sFinalstr); 
		style.setSpan(new ForegroundColorSpan(Color.rgb(255, 123, 38)), start, sFinalstr.length()-2,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		mNotifyfriendsTv.setText(style);
		

		UdpThreadManager.getInstance().connectSocket();
		onWaitTimeout();
	}

	@Override
	public void createWifiApFailed() {
		//正在创建隐藏
		mIscreatingTv.setVisibility(View.GONE);
//		//创建成功显示
		mCreateSuccessHintTv.setVisibility(View.VISIBLE);
		mCreateSuccessHintTv.setText(R.string.create_fail);
		Toast.makeText(this, R.string.create_wifi_failed, Toast.LENGTH_SHORT).show();
		
		if (mIsFromSendBtn) {
			disconnect();
		} else {
			closeWifiConnection();
			Intent intent = new Intent(this, ConnectFriendsActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
		}
	}
	@SuppressLint("NewApi")
	@Override
	public void connectedUserChanged(Map<String, TUser> user) {
		
		mTransferFiles= getIntent().getParcelableArrayListExtra("transfer_file_list");	
		Bitmap image = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.favicon50);
		//头像宽高
		int BACKWIDTH = image.getWidth();
		int BACKHEIGHT = image.getHeight();
		
		//圆心坐标
		mPointX=mWth / 2-BACKWIDTH/2;
		mPointY=mHei/2-BACKHEIGHT;
		
		//半径
		int r = mWth / 2 - BACKWIDTH;
		//得到头像角度
		if (user == null || user.size() == 0){
			System.out.println("mList.size()"+mList.size());
			mList.clear();
			mView=initMysurfaceView(mList);
			//获得被点击的所有连接者
		   	mSelectRecivers=mView.getUserList();
		   	mBtnSendChecked.setVisibility(View.GONE);
		   	mBtnSendUnChecked.setVisibility(View.GONE);
		   	return;
		}
		
		if (mIsFromSendBtn) {
			if(mTransferFiles.size()>0){
				mBtnSendUnChecked.setVisibility(View.VISIBLE);
				mNotifyfriendsTv.setVisibility(View.VISIBLE);
				mNotifyfriendsTv.setText(R.string.choose_reciver);
				
				
				mBtnFriendsInstall.setVisibility(View.GONE);
				mUserMap=user;
				Collection<TUser> c=user.values();
				//得到每个对象之间的角度
				int angle=getAngle(8);
				//所有连接对象的集合
				List<TUser> list=new ArrayList<TUser>();
				list.addAll(c);
				int j=0;
				mList.clear();
				for(int i = -150; i < 210;){
					// 避免第一个和最后一个重合
					if (210 - i < angle) {
						break;
					}
					int x = (int) (mPointX + (r * Math.cos(i * Math.PI
							/ 180)));
					int y = (int) (mPointY + (float) (r * Math.sin(i
							* Math.PI / 180)));
					//Reciver对象
					Bitmap btpReciver=BitmapFactory.decodeResource(getResources(), R.drawable.favicon50_black);
					Reciver recivera=new Reciver(btpReciver,list.get(j).getUserName(), x, y);
					
					recivera.setIp(list.get(j).getIp());
					
					mList.add(recivera);
					i = i + angle;
					j++;
					if (j == c.size()) {
						break;
					}
				}
				mView=initMysurfaceView(mList);
				//获得被点击的所有连接者
			   	mSelectRecivers=mView.getUserList();
			   	
			}
			
		} else {
			Intent intent = new Intent(this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
		}
	}
	
	/**
	 * 
	 * @param size
	 * @return 头像和圆心之间的角度
	 */
	private int getAngle(int size){
		int angle=(int) Math.round(360/size);
		return angle;
	}
}
