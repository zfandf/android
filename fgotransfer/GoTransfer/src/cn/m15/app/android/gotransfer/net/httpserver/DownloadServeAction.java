package cn.m15.app.android.gotransfer.net.httpserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import cn.m15.app.android.gotransfer.net.httpserver.NanoHTTPD.Response;
import android.content.Context;

public class DownloadServeAction extends ServeAction {

	private static final String MIME_DEFAULT_BINARY = "application/octet-stream";

	@Override
	protected Response response(Context mContext, Map<String, String> params,
			Map<String, String> files) throws JSONException {
		String path = params.get("path");
		return serverFile(path);
	}

	public Response response(String apkPath) throws JSONException {
		return serverFile(apkPath);
	}

	private Response serverFile(String path) throws JSONException {
		JSONObject result = new JSONObject();
		File file = new File(path);
		if (null == path || !file.exists()) {
			result.put("code", 1).put("msg",
					ServerMsg.getMsg(ServerMsg.PARAM_MISSING));
		} else {
			FileInputStream fileStream;
			try {
				fileStream = new FileInputStream(path);
				return createDownloadResponse(Response.Status.OK,
						MIME_DEFAULT_BINARY, fileStream, file);
			} catch (FileNotFoundException e) {
			}
		}
		return createResponse(Response.Status.OK, MIME_PLAINTEXT,
				result.toString());
	}

	private Response createDownloadResponse(Response.Status status,
			String mimeType, InputStream message, File file) {
		Response res = new Response(status, mimeType, message);
		res.addHeader("Accept-Ranges", "bytes");
		res.addHeader("Content-Disposition",
				"attachment;filename=" + file.getName());
		res.addHeader("Content-Length", "" + file.length());
		return res;
	}
}
