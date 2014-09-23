package cn.m15.app.android.gotransfer.ui.activity;

import java.util.Hashtable;

import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.util.TypedValue;
import android.widget.TextView;
import cn.m15.app.android.gotransfer.Const;
import cn.m15.app.android.gotransfer.R;
import cn.m15.app.android.gotransfer.utils.AppUtil;
import cn.m15.gotransfer.sdk.ConfigManager;
import cn.m15.gotransfer.sdk.net.wifi.WifiApConnector;
import cn.m15.gotransfer.sdk.net.wifi.WifiApManager;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class FreeShareActivity extends BaseActivity {
	private TextView mTvStep1;
	private TextView mTvStep2;
	private int mQrWidth;
	private int mQrHeight;
	private Bitmap mBitmap;
	private boolean mIsPreCreate = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_free_share);
		setTitle(R.string.share_wifiap);
		
		mTvStep1 = (TextView) findViewById(R.id.tv_free_share_step1);
		mTvStep2 = (TextView) findViewById(R.id.tv_free_share_step2);
		
		PackageInfo app = AppUtil.getAppPackageInfo(getPackageName());
		String appp = AppUtil.getAppAPKPath(app);
		
		// 没有创建WIFI热点的话就重新创建
		if (mApConnector.getCreateStatus() != WifiApConnector.CREATE_WIFI_AP_STARTED
				&& mApConnector.getCreateStatus() != WifiApConnector.CREATE_WIFI_AP_SUCCESS) {
			mIsPreCreate = false;
			closeWifiConnection();
			mApConnector.createWifiAp();
		}
		ConfigManager.getInstance().shareAPKWithHttp(appp, Const.APK_NAME);

		String wlan = WifiApManager.encodeWifiApName();
		String url = "http://192.168.43.1:8888";
		
		mQrWidth = mQrHeight = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 180, getResources().getDisplayMetrics());
		createQRImage(url);
		
		String wlanText = getString(R.string.free_share_step1, wlan);
		String urlText = getString(R.string.free_share_step2, url);
		
		SpannableString wlanSb = new SpannableString(wlanText);
		SpannableString urlSb = new SpannableString(urlText);

		int wlanIndex = wlanText.indexOf(wlan);
		int urlIndex = urlText.indexOf(url);
		wlanSb.setSpan(new ForegroundColorSpan(Color.parseColor(getResources().getString(R.color.c4))), 
				wlanIndex,
				wlanIndex + wlan.length(),
				SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
		urlSb.setSpan(new ForegroundColorSpan(Color.parseColor(getResources().getString(R.color.c4))), 
				urlIndex,
				urlIndex + url.length(),
				SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
		ImageSpan span = new ImageSpan(this, mBitmap);
		int start = urlText.length();
		urlSb.setSpan(span, start-1, start, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
		mTvStep1.setText(wlanSb);
		mTvStep2.setText(urlSb);
		
		
	}
	
	@Override
	protected void onDestroy() {
		ConfigManager.getInstance().stopShareHttpServer();
		if (!mIsPreCreate) { // 如果进来之前没有创建wifi热点就断开
			mApConnector.destroyWifiAp();
		}
		super.onDestroy();
	}
	
	public void createQRImage(String url) {
		try {
			if (url == null || "".equals(url) || url.length() < 1) {
				return;
			}
			Hashtable<EncodeHintType, Integer> hints = new Hashtable<EncodeHintType, Integer>();
			hints.put(EncodeHintType.MARGIN, 1);
			BitMatrix bitMatrix = new QRCodeWriter().encode(url,
					BarcodeFormat.QR_CODE, mQrWidth, mQrHeight, hints);
			int[] pixels = new int[mQrWidth * mQrHeight];
			for (int y = 0; y < mQrHeight; y++) {
				for (int x = 0; x < mQrWidth; x++) {
					if (bitMatrix.get(x, y)) {
						pixels[y * mQrWidth + x] = 0xff000000;
					} else {
						pixels[y * mQrWidth + x] = 0xffffffff;
					}
				}
			}
			Bitmap bitmap = Bitmap.createBitmap(mQrWidth, mQrHeight, Bitmap.Config.ARGB_8888);
			bitmap.setPixels(pixels, 0, mQrWidth, 0, 0, mQrWidth, mQrHeight);
			//mIvQrCode.setImageBitmap(bitmap);
			mBitmap = bitmap;
		} catch (WriterException e) {
			e.printStackTrace();
		}
	}
	
}