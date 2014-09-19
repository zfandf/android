package cn.m15.app.android.gotransfer.ui.widget.animation;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.text.TextPaint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import cn.m15.app.android.gotransfer.ui.activity.ConnectingActivity;
import cn.m15.gotransfer.sdk.net.wifi.WifiApConnector;

public class MyView extends View {
	Reciver receiver;
	List<Reciver> list;
	TextPaint mTextPaint=new TextPaint();
	
	public MyView(Context context) {
		super(context);
	}

	public MyView(Context context, List<Reciver> list) {
		super(context);
		this.list = list;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			for (int i = 0; i < list.size(); i++) {

				boolean f = list.get(i).isClick(x, y);
				if (f) {
					Log.d("zhpuclick", list.get(i).flag + "::" + i + ":::"
							+ list.get(i).getDecodename());
					WifiApConnector.getInstance().connectToAP(
							list.get(i).getDecodename());

					Intent intent = new Intent(getContext(), ConnectingActivity.class);
					intent.putExtra("ConnectName", list.get(i).getName());

					getContext().startActivity(intent);
				}

			}
		}
		return false;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		mTextPaint.setAntiAlias(true);
		
		mTextPaint.setTextSize(sp2px(getContext(), 12));
		for (int i = 0; i < list.size(); i++) {
			list.get(i).draw(canvas, mTextPaint);
		}
	}

	public static int sp2px(Context context, float spValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (spValue * fontScale + 0.5f);
	}
}
