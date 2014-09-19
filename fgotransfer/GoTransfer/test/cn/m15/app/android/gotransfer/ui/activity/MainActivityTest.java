package cn.m15.app.android.gotransfer.ui.activity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowConnectivityManager;
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.tester.android.view.TestMenuItem;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.MenuItem;
import cn.m15.app.android.gotransfer.GoTransferApplication;
import cn.m15.app.android.gotransfer.R;

@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {
	private MainActivity mMainActivity;
	private MenuItem item;

	@Before
	public void setUp() {
		mMainActivity = Robolectric.buildActivity(MainActivity.class).create()
				.get();
	}

	@Test
	public void perssItemLaunchCommonWebViewActivity() throws Exception {
		item = new TestMenuItem(R.id.menu);
		mMainActivity.onOptionsItemSelected(item);
		ConnectivityManager connectivityManager = (ConnectivityManager) GoTransferApplication.getInstance()  
                .getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo info = connectivityManager.getActiveNetworkInfo();
	    ShadowConnectivityManager scManager=Robolectric.shadowOf(connectivityManager);
		
		ShadowActivity shadowActivity = Robolectric.shadowOf(mMainActivity);
		Intent i = shadowActivity.getNextStartedActivity();
		ShadowIntent shadowIntent = Robolectric.shadowOf(i);
		assertThat(shadowIntent.getComponent().getClassName(),
				equalTo(CommonWebViewActivity.class.getName()));
	}

	@Test
	public void perssItemNoLaunchCommonWebViewActivity() throws Exception {

		mMainActivity.onOptionsItemSelected(item);

		assertThat(ShadowToast.getTextOfLatestToast(),
				equalTo("No WIFI connected"));
	}
}
