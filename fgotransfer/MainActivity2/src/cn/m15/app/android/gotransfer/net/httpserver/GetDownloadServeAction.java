package cn.m15.app.android.gotransfer.net.httpserver;

import java.io.File;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import cn.m15.app.android.gotransfer.net.httpserver.NanoHTTPD.Response;
import android.content.Context;

public class GetDownloadServeAction extends ServeAction {

	@Override
	protected Response response(Context mContext, Map<String, String> params,
			Map<String, String> files) throws JSONException {
		JSONObject result = new JSONObject();
		String path = params.get("path");
		if (null == path) {
			result.put("code", 1).put("msg",
					ServerMsg.getMsg(ServerMsg.PARAM_MISSING));
		} else {
			String[] paths = path.indexOf("|") != -1 ? path.split("[|]")
					: new String[] { path };
			for (String filePath : paths) {
				File file = new File(filePath);
				if (!file.exists()) {
					throw new RuntimeException(
							ServerMsg.getMsg(ServerMsg.FILE_NOT_EXISTS));
				}
			}
			String downPath = DownloadFile.downloadFile(paths);
			if (downPath != null) {
				result.put("code", 0).put("path", downPath);
			}
		}
		return createResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT,
				result.toString());
	}

}