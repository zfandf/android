package cn.m15.app.android.gotransfer.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ListView;
import cn.m15.app.android.gotransfer.R;

public class ExpandableHeightListView extends ListView {

	private boolean mExpanded = false;

	public ExpandableHeightListView(Context context) {
		super(context);
	}

	public ExpandableHeightListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.ExpandableHeightListView);
		mExpanded = t.getBoolean(R.styleable.ExpandableHeightListView_expanded, false);
		t.recycle();
	}

	public ExpandableHeightListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// HACK! TAKE THAT ANDROID!
		if (mExpanded) {
			// Calculate entire height by providing a very large height hint.
			// But do not use the highest 2 bits of this integer; those are
			// reserved for the MeasureSpec mode.
			int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
					MeasureSpec.AT_MOST);
			super.onMeasure(widthMeasureSpec, expandSpec);

			ViewGroup.LayoutParams params = getLayoutParams();
			params.height = getMeasuredHeight();
		} else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}

}