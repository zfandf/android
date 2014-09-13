package cn.m15.app.android.gotransfer.ui.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import cn.m15.app.android.gotransfer.Const;
import cn.m15.app.android.gotransfer.GoTransferApplication;
import cn.m15.app.android.gotransfer.R;
import cn.m15.app.android.gotransfer.net.wifi.WifiApConnector;
import cn.m15.app.android.gotransfer.utils.AppUtil;
import cn.m15.app.android.gotransfer.utils.ImageUtil;

import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXWebpageObject;

public class InviteFriendsActivity extends BaseActivity2 implements
		OnItemClickListener, OnClickListener {
	private IWXAPI mIWXApi;
	private String mShareLink;
	private String mShareDesc;
	private String mShareText;
	private IWeiboShareAPI mWeiboShareAPI;
	private String[] mNamesArr = null;
	private int[] mImagesResArr = null;

	// private Button mBtnBlue, mBtnWifi;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share);
		// setCustomTitle(R.string.version);
		mNamesArr = getResources().getStringArray(R.array.share_btn_names);

		findViewById(R.id.btn_share_blue).setOnClickListener(this);
		findViewById(R.id.btn_share_wifiap).setOnClickListener(this);
		if (Locale.CHINESE.toString().equals(Locale.getDefault().getLanguage())) {
			mImagesResArr = new int[] { R.drawable.bg_btn_share_message,
					R.drawable.bg_btn_share_mail,
					R.drawable.bg_btn_share_friend,
					R.drawable.bg_btn_share_weixin,
					R.drawable.bg_btn_share_weibo,
					R.drawable.bg_btn_share_qr_code, R.drawable.ic_other };
		} else {
			mImagesResArr = new int[] { R.drawable.bg_btn_share_message,
					R.drawable.bg_btn_share_mail,
					R.drawable.bg_btn_share_qr_code, R.drawable.ic_other };
		}
		GridView gridview = (GridView) findViewById(R.id.gv_share);
		ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();
		int count = mImagesResArr.length;
		for (int i = 0; i < count; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("itemImage", mImagesResArr[i]);
			map.put("itemText", mNamesArr[i]);
			lstImageItem.add(map);
		}
		SimpleAdapter saImageItems = new SimpleAdapter(this, lstImageItem,
				R.layout.item_share, new String[] { "itemImage", "itemText" },
				new int[] { R.id.iv_share_item, R.id.tv_share_item });
		gridview.setAdapter(saImageItems);
		gridview.setOnItemClickListener(this);
		regToWX();
		initShareInfo();
	}

	private void regToWeibo() {
		mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this, Const.SINA_APP_KEY);
		mWeiboShareAPI.registerApp();
	}

	private void regToWX() {
		if (mIWXApi == null) {
			mIWXApi = WXAPIFactory.createWXAPI(this, Const.WX_APP_ID, true);
			mIWXApi.registerApp(Const.WX_APP_ID);
		}
	}

	private void initShareInfo() {
		mShareLink = getString(R.string.share_wx_link);
		mShareDesc = getString(R.string.share_wx_desc);
		mShareText = getString(R.string.share_wx_text);
	}

	private void shareToSession() {
		if (!mIWXApi.isWXAppInstalled()) {
			Toast.makeText(this, R.string.wx_app_not_installed,
					Toast.LENGTH_SHORT).show();
			return;
		}
		shareToWx(SendMessageToWX.Req.WXSceneSession);
	}

	private void shareToTimeline() {
		if (!mIWXApi.isWXAppInstalled()) {
			Toast.makeText(this, R.string.wx_app_not_installed,
					Toast.LENGTH_SHORT).show();
			return;
		}
		int apiVersion = mIWXApi.getWXAppSupportAPI();
		if (apiVersion >= 0x21020001) {
			shareToWx(SendMessageToWX.Req.WXSceneTimeline);
		} else {
			Toast.makeText(this, R.string.wx_version_too_low,
					Toast.LENGTH_SHORT).show();
		}
	}

	private void shareToWx(int scene) {
		Log.d("share", "shareToWx >>> " + scene);
		Bitmap thumbBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.img_share_app);
		byte[] thumb = ImageUtil.compressShareImageBytes(thumbBitmap);

		WXWebpageObject webpage = new WXWebpageObject();
		webpage.webpageUrl = mShareLink;
		WXMediaMessage message = new WXMediaMessage(webpage);
		message.title = mShareText;
		message.description = mShareDesc;
		message.thumbData = thumb;

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = String.valueOf(System.currentTimeMillis());
		req.message = message;
		req.scene = scene;
		mIWXApi.sendReq(req);
	}

	private void shareToMessage() {
		String smsBody = getString(R.string.share_sms_content);
		Uri smsToUri = Uri.parse("smsto:");
		Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
		intent.putExtra("sms_body", smsBody);
		startActivity(Intent.createChooser(intent, ""));
	}

	private void shareToMail() {
		String mailSubject = getString(R.string.share_mail_subject);
		String mailContent = getString(R.string.share_mail_content);
		Intent intent = new Intent(Intent.ACTION_SENDTO);
		intent.setData(Uri.parse("mailto:"));
		intent.putExtra(Intent.EXTRA_SUBJECT, mailSubject);
		intent.putExtra(Intent.EXTRA_TEXT, mailContent);
		startActivity(Intent.createChooser(intent, ""));
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long rowid) {

		switch (mImagesResArr[position]) {

		case R.drawable.bg_btn_share_message:
			shareToMessage();
			break;
		case R.drawable.bg_btn_share_mail:
			shareToMail();
			break;
		case R.drawable.bg_btn_share_friend:
			shareToTimeline();
			break;
		case R.drawable.bg_btn_share_weixin:
			shareToSession();
			break;
		case R.drawable.bg_btn_share_weibo:
			shareToWeibo();
			break;
		case R.drawable.bg_btn_share_qr_code:
			if (WifiApConnector.getInstance().isWifiConnected()) {
				showQrcode();
			} else {
				Toast.makeText(this, R.string.no_wifi_connected,
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.drawable.ic_other:
			shareToOther();
			break;
		}
	}

	private void showQrcode() {
		String notic_title = getString(R.string.share_qr_code_title);
		String link = Const.QRCODE_URL;
		int app_id = Const.APP_ID;
		String version = GoTransferApplication.getAppVersionName(this);
		link += "?action=share&app_id=" + app_id + "&app_version=" + version;
		Intent intent = new Intent(this, CommonWebViewActivity.class);
		intent.putExtra("notice_title", notic_title);
		intent.putExtra("link", link);
		startActivity(intent);
	}

	private void shareToWeibo() {
		regToWeibo();
		if (!mWeiboShareAPI.isWeiboAppInstalled()) {
			Toast.makeText(this, R.string.xinlang_app_not_installed,
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (mWeiboShareAPI.isWeiboAppSupportAPI()) {
			WeiboMultiMessage weiboMultiMessage = new WeiboMultiMessage();
			weiboMultiMessage.mediaObject = getTextObj();
			weiboMultiMessage.imageObject = getImageObj();
			SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
			request.transaction = String.valueOf(System.currentTimeMillis());
			request.multiMessage = weiboMultiMessage;
			mWeiboShareAPI.sendRequest(request);
		} else {
			Toast.makeText(this, R.string.xinlang_version_too_low,
					Toast.LENGTH_SHORT).show();
		}
	}

	private void shareToOther() {

		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/image");
		intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_mail_content));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(Intent.createChooser(intent, getTitle()));
	}

	private TextObject getTextObj() {
		TextObject textObject = new TextObject();
		textObject.text = getString(R.string.share_weibo_content);
		return textObject;
	}

	private ImageObject getImageObj() {
		ImageObject imageObject = new ImageObject();
		Bitmap bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
				getResources(), R.drawable.img_share_app), 140, 140, true);
		BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(),
				bitmap);
		imageObject.setImageObject(bitmapDrawable.getBitmap());
		return imageObject;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_share_blue:
			PackageInfo app = AppUtil.getAppPackageInfo(getPackageName());
			String appp = AppUtil.getAppAPKPath(app);
			AppUtil.shareAPKWithBluetooth(this, appp);
			break;
		case R.id.btn_share_wifiap:
			Intent intent = new Intent(this, FreeShareActivity.class);
			startActivity(intent);
		}

	}
}