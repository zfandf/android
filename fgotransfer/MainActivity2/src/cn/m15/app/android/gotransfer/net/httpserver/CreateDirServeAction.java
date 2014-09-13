package cn.m15.app.android.gotransfer.net.httpserver;

import java.io.File;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import cn.m15.app.android.gotransfer.net.httpserver.NanoHTTPD.Response;
import android.content.Context;

public class CreateDirServeAction extends ServeAction {

	@Override
	protected Response response(Context mContext, Map<String, String> params,
			Map<String, String> files) throws JSONException {
		JSONObject result = new JSONObject();
		String dirName = params.get("dir_name");
		String path = params.get("path");
		File dir = new File(path, dirName);
		if (dirName == null || path == null) {
			result.put("code", 1).put("msg",
					ServerMsg.getMsg(ServerMsg.PARAM_MISSING));
		} else if (dir.exists()) {
			result.put("code", 1).put("msg",
					ServerMsg.getMsg(ServerMsg.DIE_EXISTS));
		} else {
			boolean isMaked = dir.mkdir();
			if (isMaked) {
				result.put("code", 0);
			} else {
				result.put("code", 1).put("msg",
						ServerMsg.getMsg(ServerMsg.CREATE_FAILED));
			}
		}
		return createResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT,
				result.toString());
	}

}
