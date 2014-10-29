package cn.m15.gotransfersimplest.net.httpserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

public class GoNanoHTTPD extends NanoHTTPD {

	private static final String TAG = "GoNanoHTTPD";
	private JSONArray mData;
	private Context mContext;
	private static final String[] mActions = { Actions.LIST.getName(),
			Actions.IMAGE.getName(), Actions.DOWNLOAD.getName() };
	private static final String mDefaultIndex = "index.html";
	private static final String mRoot = "wwwroot";
	private static final String MIME_DEFAULT_BINARY = "application/octet-stream";
	private static final Map<String, String> mMimeTypes = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("unknown", "application/octet-stream");
			put("js", "application/x-javascript");
			put("json", "text/plain");
			put("jpg", "image/jpeg");
			put("jpeg", "image/jpeg");
			put("html", "text/html");
			put("png", "iamge/png");
			put("gif", "image/gif");
			put("css", "text/css");
		}
	};

	public GoNanoHTTPD(int port) {
		super(port);
	}

	public GoNanoHTTPD(Context context, int port, JSONArray data) {
		super(port);
		mContext = context;
		mData = data;
	}

	@Override
	public Response serve(IHTTPSession session) {

		Map<String, String> params = session.getParms();
		String reqUri = session.getUri();

		String reqAction = params.get("action");
		Log.d(TAG, "action=" + reqAction);
		Log.d(TAG, "actions:" + Arrays.toString(mActions));
		Log.d(TAG, "list=" + Actions.LIST);
		if (null != reqAction && Arrays.asList(mActions).contains(reqAction)) {
			try {
				if (Actions.LIST.getName().equals(reqAction)) {
					Log.d(TAG, "action=" + reqAction);
					return responseList();
				} else if (Actions.IMAGE.getName().equals(reqAction)) {
					return responseImage(params);
				} else {
					return responseDownload(params);
				}
			} catch (Exception e) {
				return responseServerError();
			}
			
		} else {
			return responseStaticFile(reqUri);
		}
	}

	private Response responseDownload(Map<String, String> params)throws JSONException, IOException {
		JSONObject result = new JSONObject();
		String path = params.get("path");
		Map<String, String> checkResult = checkResponseStream(path);
		if (null == checkResult) {
			result.put("code", 1).put("msg", ResponseText.TEXT_MISSING_PARAMS);
			return responseServerOKText(result.toString());
		}
		FileInputStream fis = new FileInputStream(path);
		Response response = new Response(Response.Status.OK, MIME_DEFAULT_BINARY, fis);
		response.addHeader("Accept-Ranges", "bytes");
		response.addHeader("Content-Disposition", "attachment;filename=" + checkResult.get("name"));
		response.addHeader("Content-Length", "" + checkResult.get("length"));
		return response;
	}

	private Response responseImage(Map<String, String> params) throws IOException, JSONException {
		JSONObject result = new JSONObject();
		String path = params.get("path");
		Map<String, String> checkResult = checkResponseStream(path);
		if (null == checkResult) {
			result.put("code", 1).put("msg", ResponseText.TEXT_MISSING_PARAMS);
			return responseServerOKText(result.toString());
		}
		return responseServerOKStream(new FileInputStream(path));
	}

	private Map<String, String> checkResponseStream(String path) {
		File file = new File(path);
		if (TextUtils.isEmpty(path) || !file.exists()) {
			return null;
		}
		HashMap<String, String> result = new HashMap<String, String>();
		result.put("name", file.getName());
		result.put("length", String.valueOf(file.length()));
		return result;
	}

	private Response responseList() throws JSONException {
		JSONObject result = new JSONObject();
		result.put("code", 0).put("data", mData);
		return responseServerOKText(result.toString());
	}

	private Response responseStaticFile(String reqUri) {
		if (reqUri.equals("/")) {
			return responseStaticFile(reqUri + mDefaultIndex);
		}

		InputStream in = null;
		try {
			in = mContext.getAssets().open(mRoot + reqUri);
			return new Response(Response.Status.OK, getMimeType(reqUri), in);
		} catch (IOException e) {
			e.printStackTrace();
			return response404();
		}
	}

	private Response responseServerOKStream(InputStream in) {
		return new Response(Response.Status.OK, MIME_DEFAULT_BINARY, in);
	}

	private Response responseServerOKText(String result) {
		return new Response(Response.Status.OK, MIME_PLAINTEXT, result);
	}

	private Response responseServerError() {
		return new Response(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT,
				ResponseText.TEXT_501);
	}

	private Response response404() {
		return new Response(Response.Status.NOT_FOUND, MIME_PLAINTEXT,
				ResponseText.TEXT_404);
	}

	private String getMimeType(String reqUri) {
		int dotPost = reqUri.lastIndexOf(".");
		String mime = null;
		if (dotPost > 0) {
			mime = mMimeTypes.get(reqUri.substring(dotPost + 1).toLowerCase(
					Locale.ENGLISH));
		}
		return mime == null ? mMimeTypes.get("unknown") : mime;
	}

	enum Actions {
		LIST("list"), IMAGE("image"), DOWNLOAD("download");

		private final String name;

		Actions(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}
	}

	static final class ResponseText {
		static final String TEXT_404 = "file not found!";
		static final String TEXT_501 = "server error!";
		static final String TEXT_MISSING_PARAMS = "missing parameters";
	}

}