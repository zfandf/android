package cn.m15.app.android.gotransfer.net.httpserver;

import java.io.File;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import cn.m15.app.android.gotransfer.net.httpserver.NanoHTTPD.Response;

public class DeleteServeAction extends ServeAction {

	@Override
	protected Response response(Context mContext, Map<String, String> params,
			Map<String, String> files) throws JSONException {
		JSONObject result = new JSONObject();
		String path = params.get("path");
		if (null == path) {
			result.put("code", 1).put("msg",
					ServerMsg.getMsg(ServerMsg.PARAM_MISSING));
			return new Response(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT,
					result.toString());
		}

		String[] paths = (path.indexOf("|") == -1) ? new String[] { path }
				: path.split("[|]");

		int totalCount = paths.length;
		int deletedCount = 0;
		for (String p : paths) {
			File file = new File(p);
			if (file.exists()) {
				boolean isDeleted = DeleteFile
						.deleteDir(file.getAbsolutePath());
				if (isDeleted) {
					deletedCount++;
				}
			}
		}
		if (totalCount != deletedCount) {
			result.put("code", 2).put("msg",
					ServerMsg.getMsg(ServerMsg.DELETE_FAILED));
		} else {
			result.put("code", 0).put("count", deletedCount);
		}
		return createResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT,
				result.toString());
	}

}
