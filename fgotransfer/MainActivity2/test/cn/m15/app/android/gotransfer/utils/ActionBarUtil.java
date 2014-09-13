package cn.m15.app.android.gotransfer.utils;

import android.view.View;
import android.widget.TextView;
import cn.m15.app.android.gotransfer.R;
import cn.m15.app.android.gotransfer.ui.activity.BaseActivity;

public class ActionBarUtil {
	
	public static CharSequence getTitle(BaseActivity activity) {
		View customView = activity.getSupportActionBar().getCustomView();
		TextView titleView = (TextView) customView.findViewById(R.id.tv_actionbar_title);
		CharSequence title = titleView.getText();
		return title;
	}
}
