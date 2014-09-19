package cn.m15.app.android.gotransfer.net.httpserver;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import cn.m15.app.android.gotransfer.net.httpserver.NanoHTTPD.Response;
import cn.m15.app.android.gotransfer.utils.FileUtil;
import cn.m15.gotransfer.sdk.net.wifi.WifiApConnector;
import cn.m15.gotransfer.sdk.net.wifi.WifiApManager;
import cn.m15.gotransfer.sdk.utils.SdkFileUtil;

public class PhoneInfoServeAction extends ServeAction {

	@Override
	protected Response response(Context mContext, Map<String, String> params,
			Map<String, String> files) throws JSONException {

		JSONObject result = new JSONObject();
		JSONArray phoneInfo = new JSONArray();

		String phoneModel = android.os.Build.MODEL;
		result.put("phone_name", phoneModel);

		String[] storages = SdkFileUtil.getVolumePaths(mContext);
		int count = 1;
		for (int i = 0; i < storages.length; i++) {
			JSONObject storageInfo = new JSONObject();
			if (FileUtil.isInternalStoragePath(mContext, storages[i])) {
				storageInfo.put("name", ServerMsg.getMsg(ServerMsg.INNER_NAME));
			} else {
				String cardName = ServerMsg.getMsg(ServerMsg.CARD_NAME);
				if (storages.length > 2) {
					cardName += count;
				}
				storageInfo.put("name", cardName);
				count++;
			}
			storageInfo.put("path", storages[i]);
			storageInfo.put("total_space",
					FileUtil.getReadableSize(FileUtil.disk_total(storages[i])));
			storageInfo.put("free_space",
					FileUtil.getReadableSize(FileUtil.disk_free(storages[i])));
			phoneInfo.put(storageInfo);
		}

		result.put("v", GowebNanoHTTPD.WEB_WERSION);
		boolean isWifiConnected = WifiApConnector.getInstance()
				.isWifiConnected();
		if (isWifiConnected) {
			result.put("ip", "http://"
					+ WifiApManager.getInstance().getWifiIpAddressAndName()[0]
					+ ":" + GowebNanoHTTPD.DEFAULT_HTTP_PORT + "/");
		} else {
			result.put("ip", "http://192.168.43.1:"
					+ GowebNanoHTTPD.DEFAULT_HTTP_PORT + "/");
		}

		result.put("storages", phoneInfo).put("code", 0);
		return createResponse(Response.Status.OK, MIME_PLAINTEXT,
				result.toString());
	}

}
