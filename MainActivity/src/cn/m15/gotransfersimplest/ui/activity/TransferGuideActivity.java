package cn.m15.gotransfersimplest.ui.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import cn.m15.gotransfersimplest.Const;
import cn.m15.gotransfersimplest.R;
import cn.m15.gotransfersimplest.net.httpserver.GoNanoHTTPD;
import cn.m15.gotransfersimplest.net.httpserver.WebServer;
import cn.m15.gotransfersimplest.net.wifi.WifiApManager;

public class TransferGuideActivity extends BaseActivity {
	private String mTransferData;
	private String mStep1;
	private String mStep2;
	private GridView mShareGv;
	private GoNanoHTTPD mHttpServer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_transfer_guide);
		
		mTransferData = getIntent().getStringExtra("transfer_files");
		
		String[] guideSharesArray = getResources().getStringArray(R.array.guide_shares);
		List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < guideSharesArray.length; i++) {
			Map<String, Object> grid = new HashMap<String, Object>();
			grid.put("image", R.drawable.ic_launcher);
			grid.put("title", guideSharesArray[i]);
			dataList.add(grid);
		}
		mShareGv = (GridView) findViewById(R.id.gv_share);
		mShareGv.setAdapter(new SimpleAdapter(this, dataList,
				android.R.layout.simple_gallery_item, 
				new String[] {"image", "title" },
				new int[] { android.R.id.text1, android.R.id.text1 }));
		mShareGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch (position) {
				case 0:
					shareToMessage();
					break;
				case 1:
					shareToMail();
					break;
				case 2:
					break;
				case 3:
					break;
				case 4:
					break;
				case 5:
					break;
				}
			}
			
		});
		
		String wlan = WifiApManager.encodeWifiApName();
		mStep1 = getString(R.string.guide_step1) + wlan;
		TextView step1Tv = (TextView) findViewById(R.id.tv_step1);
		step1Tv.setText(mStep1);

		String step2Part1 = getString(R.string.guide_step2_part1);
		String step2Part2 = getString(R.string.guide_step2_part2);
		mStep2 = step2Part1 + Const.DOWNLOAD_URL + step2Part2;
		TextView step2Tv = (TextView) findViewById(R.id.tv_step2);
		step2Tv.setText(mStep2);
		
		if (!mApConnector.isCreatedAp()) {
			mApConnector.createWifiAp();
		}
		
		JSONObject json = null;
		try {
			json = new JSONObject(mTransferData);
		} catch (JSONException e1) {
		}
		JSONArray jsonArray = new JSONArray();
		jsonArray.put(json);
		mHttpServer = WebServer.getInstance()
				.getServer(this, Const.HTTT_SERVER_PORT, jsonArray);
		try {
			mHttpServer.start();
		} catch (IOException e) {
			Toast.makeText(this, R.string.http_server_start_failed, 
					Toast.LENGTH_LONG).show();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		new AsyncTask<Void, Void, Void>() {
			
			@Override
			protected Void doInBackground(Void... params) {
				if (mHttpServer != null) {
					mHttpServer.stop();					
				}
				return null;
			}
		}.execute();
	}
	
	private void shareToMessage() {
		String smsBody = mStep1 + "\n\n" + mStep2;
		Uri smsToUri = Uri.parse("smsto:");
		Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
		intent.putExtra("sms_body", smsBody);
		startActivity(Intent.createChooser(intent, ""));
	}

	private void shareToMail() {
		String mailSubject = getString(R.string.guide_share_email_title);
		String mailContent = mStep1 + "\n\n" + mStep2;
		Intent intent = new Intent(Intent.ACTION_SENDTO);
		intent.setData(Uri.parse("mailto:"));
		intent.putExtra(Intent.EXTRA_SUBJECT, mailSubject);
		intent.putExtra(Intent.EXTRA_TEXT, mailContent);
		startActivity(Intent.createChooser(intent, ""));
	}
}
