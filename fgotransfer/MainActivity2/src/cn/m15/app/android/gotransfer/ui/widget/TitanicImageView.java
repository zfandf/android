package cn.m15.app.android.gotransfer.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import cn.m15.app.android.gotransfer.R;

public class TitanicImageView extends View {

	private Bitmap mBmWave = null;
	private Bitmap mBmCover = null;
	private Bitmap mBmAlpha = null;
	private Paint mPaint = new Paint();

	private RectF mRectCover;
	private RectF mRectWave;

	private float x1 = 0;
	private float y1 = 0;

	private int mCoverOriginX;
	private int mCoverOriginY;

	private int mWaveOriginX;
	private int mWaveOriginY;
	
	private boolean mFlip;
	
	private boolean mIsAlpha = true;
	
	public void setX1(float x1) {
		this.x1 = x1;
		invalidate();
	}

	public void setY1(float y1) {
		this.y1 = y1;
		invalidate();
	}

	public float getX1() {
		return x1;
	}

	public float getY1() {
		return y1;
	}

	public TitanicImageView(Context context) {
		super(context);
		init(context);
	}

	public TitanicImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Titanic);
		mFlip = a.getBoolean(R.styleable.Titanic_flip, false);
		a.recycle();
		init(context);
	}

	public TitanicImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		mBmWave = BitmapFactory.decodeResource(getResources(),
				R.drawable.img_create_wifi_alpha_wave);
		mBmCover = BitmapFactory.decodeResource(getResources(),
				R.drawable.img_create_wifi);
		mBmAlpha = BitmapFactory.decodeResource(getResources(),
				R.drawable.img_create_wifi_alpha);
		if (mFlip) {
			mBmWave = flip(mBmWave);
			mBmCover = flip(mBmCover);
			mBmAlpha = flip(mBmAlpha);
			x1 = -mBmAlpha.getWidth();
		}
		
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Style.FILL);
		mPaint.setStrokeWidth(5);
		mPaint.setColor(getContext().getResources().getColor(R.color.c4));
	}
	
	public void setIsAlpha(boolean isAlpha) {
		mIsAlpha = isAlpha;
		invalidate();
	}

	public float getWaveWidth() {
		if (mBmWave != null) {
			return mBmWave.getWidth();
		}
		return 0;
	}
	
	public float getAlphaWidth() {
		if (mBmAlpha != null) {
			return mBmAlpha.getWidth();
		}
		return 0;
	}

	private Bitmap flip(Bitmap src) {
		Matrix m = new Matrix();
		m.preScale(-1, 1);
		Bitmap dst = Bitmap.createBitmap(src, 0, 0, src.getWidth(),
				src.getHeight(), m, false);
		dst.setDensity(DisplayMetrics.DENSITY_DEFAULT);
		return dst;
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		mCoverOriginX = (getWidth() - mBmCover.getWidth()) / 2;
		mCoverOriginY = (getHeight() - mBmCover.getHeight()) / 2;
		mWaveOriginX = (getWidth() - mBmAlpha.getWidth()) / 2;
		mWaveOriginY = (getHeight() - mBmAlpha.getHeight()) / 2;

		if (mRectCover == null) {
			mRectCover = new RectF(mCoverOriginX, mCoverOriginY, mCoverOriginX
					+ mBmCover.getWidth(), mCoverOriginY + mBmCover.getHeight());
		}

		if (mRectWave == null) {
			mRectWave = new RectF(mWaveOriginX, mWaveOriginY, mWaveOriginX
					+ mBmAlpha.getWidth(), mWaveOriginY + mBmAlpha.getHeight());
		}

		if (mIsAlpha) {
			canvas.drawBitmap(mBmAlpha, null, mRectWave, mPaint);	
			canvas.drawBitmap(mBmWave, new Rect((int) x1, (int) y1, 
					(int) x1 + mBmAlpha.getWidth(), (int) y1 + mBmWave.getHeight()),
					mRectWave, mPaint);
		} else {
			canvas.drawRect(mRectWave, mPaint);
		}

		canvas.drawLine(mRectWave.left, mRectWave.bottom, mRectWave.right, mRectWave.bottom, mPaint);
		canvas.drawBitmap(mBmCover, null, mRectCover, mPaint);
	}
}
