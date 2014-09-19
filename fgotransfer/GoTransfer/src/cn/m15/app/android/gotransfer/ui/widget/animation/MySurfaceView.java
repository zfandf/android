package cn.m15.app.android.gotransfer.ui.widget.animation;

import java.util.ArrayList;
import java.util.List;
import cn.m15.app.android.gotransfer.R;
import cn.m15.app.android.gotransfer.ui.activity.CreateConnectionActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.TextPaint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.SurfaceHolder.Callback;
import android.view.View;
import android.widget.Button;

/**
 * 
 * @author shaohx
 */
@SuppressLint("NewApi")
public class MySurfaceView extends SurfaceView implements Callback, Runnable {
	private SurfaceHolder mSfh;
	private TextPaint mPaint;
	private Thread mTh;
	private boolean mflag;
	private Canvas mCanvas;
	private Button mBtn;
	private Button mBtnUnChecked;
	private boolean canDraw=true;//是否显示白色发送按钮
	private List<Reciver> mlist;
	// 声明一个Resources实例便于加载图片
	private Resources mRres = this.getResources();
	// 声明一个滚动背景对象
	private WaterBackground mBackGround;
	//	被点击的 所有连接者
	private List<Reciver> mUserList=new ArrayList<Reciver>();
	public Button getmBtnUnChecked() {
		return mBtnUnChecked;
	}
	public void setmBtnUnChecked(Button mBtnUnChecked) {
		this.mBtnUnChecked = mBtnUnChecked;
	}
	public Button getmBtn() {
		return mBtn;
	}

	public void setmBtn(Button mBtn) {
		this.mBtn = mBtn;
	}
	
	public List<Reciver> getUserList() {
		return mUserList;
	}

	public void setUserList(List<Reciver> userList) {
		this.mUserList = userList;
	}

	/**
	 * SurfaceView初始化函数
	 */
	public MySurfaceView(Context context, WaterBackground back, List recivers) {
		super(context);
		this.mBackGround = back;
		this.mlist = recivers;
		mSfh = this.getHolder();
		mSfh.addCallback(this);
		setZOrderOnTop(false);
		mPaint = new TextPaint();
		mPaint.setColor(Color.BLACK);
		mPaint.setAntiAlias(true);
		mPaint.setTextSize(sp2px(getContext(), 12));
		setFocusable(true);
		
	}

	/**
	 * SurfaceView视图创建，响应此函数
	 */
	@Override
	public void surfaceCreated(SurfaceHolder holder) {;
		mflag = true;
		// 实例线程
		mTh = new Thread(this);
		// 启动线程
		mTh.start();
	}


	/**
	 * 绘图
	 */
	public void myDraw() {
		try {
			mCanvas = mSfh.lockCanvas();
			if (mCanvas != null) {
				mCanvas.drawRGB(250,229,204);//清除屏幕  
				//实例背景
				//背景
				if(mBackGround!=null){
					mBackGround.draw(mCanvas, mPaint);
				}
				if(mlist.size()>0&&mBtnUnChecked!=null){
					if(canDraw)
						mBtnUnChecked.setVisibility(View.VISIBLE);
					}
				}
				//接收者
				for(int i=0;i<mlist.size();i++){
					
					mlist.get(i).draw(mCanvas, mPaint);
				}
				//mReciver.draw(mCanvas, mPaint);
			
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (mCanvas != null)
				mSfh.unlockCanvasAndPost(mCanvas);
		}
	}

	/**
	 * 触屏事件监听
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			for (int i = 0; i < mlist.size(); i++) {
				boolean f = mlist.get(i).isClick(x, y);
				boolean flag2 = mlist.get(i).getFlag();
				if (f) {
					//实现点击一次选中在此点击取消
					mlist.get(i).flag = mlist.get(i).flag == false ? true : false;
					flag2 = mlist.get(i).getFlag();
					System.out.println("flage2点击后" + i + flag2);
					if (flag2) {
						
						mlist.get(i).setBmpEnemy(BitmapFactory.decodeResource(mRres,
								R.drawable.favicon50));
						mlist.get(i).setIsClick(true);
						mUserList.add(mlist.get(i));
						Log.i("muserList", "add");
						canDraw=false;
					}else if(!flag2){
						mlist.get(i).setBmpEnemy(
								BitmapFactory.decodeResource(mRres,
										R.drawable.favicon50_black));
						mlist.get(i).setIsClick(false);
						Log.i("muserList", "remover");
						mUserList.remove(mlist.get(i));
					}
					Log.i("muserList", mUserList.size()+"");
					CreateConnectionActivity.mSelectRecivers=mUserList;
				
				}
				//发送按钮的显示或隐藏
				if(mUserList.size()>0&&mBtn!=null&&mBtnUnChecked!=null){
					//如果被选中的连接者数量大于0则隐藏未选中状态发送按钮，同时显示选中状态发送按钮
					canDraw=false;
					mBtnUnChecked.setVisibility(View.GONE);
					mBtn.setVisibility(View.VISIBLE);
				}
				if(mUserList.size()==0&&mBtn!=null&&mBtnUnChecked!=null){
					canDraw=true;
					mBtnUnChecked.setVisibility(View.VISIBLE);
					mBtn.setVisibility(View.GONE);
				}
			}
		}
		return false;
	}

	/**
	 * 逻辑
	 */
	private void logic() {

		// 背景逻辑
		if (mBackGround != null) {
			mBackGround.logic();

		}
	}

	@Override
	public void run() {
		while (mflag) {
			long start = System.currentTimeMillis();
			myDraw();
			logic();
			long end = System.currentTimeMillis();
			try {
				if (end - start < 15) {
					Thread.sleep(15 - (end - start));
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * SurfaceView视图状态发生改变，响应此函数
	 */
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	/**
	 * SurfaceView视图消亡时，响应此函数
	 */
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mflag = false;
	}
	public static int sp2px(Context context, float spValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (spValue * fontScale + 0.5f);
	}
}
