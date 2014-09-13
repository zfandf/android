package cn.m15.app.android.gotransfer.ui.widget;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Scroller;
import cn.m15.app.android.gotransfer.R;

public class ShareAppLocker extends LinearLayout implements View.OnTouchListener {
	public static final String TAG = "locker";
	private Scroller mScroller;
	private int mTitleWidth;
	private int mBtnContainerWidth;
	private GestureDetector mGestureDetector;
	private boolean mIsTitleTouched = false;
	private int mDp10;
	
	private View mLockerView;
	private View mSwitchView;

	public ShareAppLocker(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDp10 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, context.getResources().getDisplayMetrics()); 
		View rootView = View.inflate(getContext(), R.layout.view_share_app_locker, this);
		mLockerView = rootView.findViewById(R.id.ll_locker_buttons);
		mSwitchView = mLockerView.findViewById(R.id.img_locker_switch);
		setOrientation(LinearLayout.HORIZONTAL);
		mScroller = new Scroller(getContext());
		mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
			
			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2,
					float distanceX, float distanceY) {
				if (!mIsTitleTouched) {
					return true;
				}
				int scrollX = getScrollX();
				distanceX = -distanceX;
				if (Math.abs(distanceX) > Math.abs(distanceY) * 3) {
					if (distanceX != 0) {
						int newScrollX = (int) (scrollX - distanceX);
						if (newScrollX < mDp10) {
							newScrollX = mDp10;
						} else if (newScrollX > mBtnContainerWidth) {
							newScrollX = mBtnContainerWidth + mDp10;
						}
						scrollTo(newScrollX, 0); // + Left, - Right
					}
				}
				return true;
			}
			
			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				float x = e.getX();
				float y = e.getY();
				int scrollX = getScrollXWhenTitleTouched(x, y);
				smoothScrollTo(scrollX, 0);
				return true;
			}
			
		});
		setOnTouchListener(this);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mTitleWidth = mSwitchView.getWidth();
		mBtnContainerWidth = mLockerView.getWidth() - mTitleWidth - mDp10;
		scrollTo(mBtnContainerWidth + mDp10, 0);
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int scrollX = getScrollX();
		int action = MotionEventCompat.getActionMasked(event);
		
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			if (isTitleTouched(event.getX(), event.getY())) {
				mIsTitleTouched = true;
			}
			break;
		case MotionEvent.ACTION_UP:
			mIsTitleTouched = false;
			int newScrollX2 = mBtnContainerWidth + mDp10;
			if (mBtnContainerWidth * 0.5 - scrollX > 0) {
				newScrollX2 = mDp10;
			}
			smoothScrollTo(newScrollX2, 0);
			break;
		}
		mGestureDetector.onTouchEvent(event);
		return true;
	}
	
	private boolean isTitleTouched(float x, float y) {
		boolean result = false;
		int scrollX = getScrollX();
		if (scrollX == mDp10 && x > mBtnContainerWidth && x < mBtnContainerWidth + mTitleWidth) {
			result = true;
		} else if (x > 0 && x < mTitleWidth + mDp10) {
			result = true;
		}
		return result; 
	}
	
	private int getScrollXWhenTitleTouched(float x, float y) {
		int scrollX = getScrollX();
		if (scrollX == mDp10 && x > mBtnContainerWidth && x < mBtnContainerWidth + mTitleWidth) {
			scrollX = mBtnContainerWidth + mDp10;				
		} else if (x > 0 && x < mTitleWidth + mDp10) {
			scrollX = mDp10;				
		}
		return scrollX; 
	}
	
	private void smoothScrollTo(int destX, int destY) {
		int scrollX = getScrollX();
		int delta = destX - scrollX;
		mScroller.startScroll(scrollX, 0, delta, 0);
		invalidate();
	}
	
	@Override
	public void computeScroll() {
		super.computeScroll();
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), 0);
			invalidate();
		}
	}

}
