package cn.m15.app.android.gotransfer.net.httpserver;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.json.JSONException;

import android.annotation.SuppressLint;
import android.content.Context;

public class GowebNanoHTTPD extends NanoHTTPD {

	public static String verifyCode;
	public static final String WEB_WERSION = "2.0";
	public static final String MIME_DEFAULT_BINARY = "application/octet-stream";
	public static final int DEFAULT_HTTP_PORT = 9999;
	private String mAPKPathsString = "";
	private String apkName = "";
	private Context mContext = null;
	private static final int VERIFY_LENGHT = 4;
	private static String defaultHomePage = "index.html";
	private static String downloadPage = "/download.html";
	private static String packageName = "cn.m15.app.android.gotransfer.net.httpserver.";
	private static String parentClassName = "ServeAction";
	private static Map<String, String> knownMimeTypes = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;

		{
			put("apk", "application/vnd.android.package-archive");
			put("html", "text/html");
			put("jpg", "image/jpeg");
			put("png", "iamge/png");
			put("gif", "image/gif");
			put("css", "text/css");
			put("js", "application/x-javascript");
			put("json", "text/plain");
		}
	};
	private static Map<String, String> actionsMap = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;

		{
			put("verify", "Verify");
			put("file", "List");
			put("info", "PhoneInfo");
			put("create_dir", "CreateDir");
			put("delete", "Delete");
			put("copy", "CopyMove");
			put("move", "CopyMove");
			put("upload", "UploadFile");
			put("download", "Download");
			put("get_download", "GetDownload");
			put("get_country", "GetCountry");
			put("image", "Image");
			put("app_list", "AppList");

		}
	};

	public GowebNanoHTTPD(Context context) {
		super(DEFAULT_HTTP_PORT);
		this.mContext = context;
	}
	
	public GowebNanoHTTPD(Context context, String apkPath, String apkName) {
		super(DEFAULT_HTTP_PORT);
		this.mContext = context;
		this.mAPKPathsString = apkPath;
		this.apkName = apkName;
	}

	public void setAPKPath(String apkPath) {
		this.mAPKPathsString = apkPath;
	}

	@SuppressLint("DefaultLocale")
	@Override
	public Response serve(IHTTPSession session) {

		Method method = session.getMethod();
		// Parse POST HTTP Body
		Map<String, String> files = new HashMap<String, String>();
		if (Method.POST.equals(method)) {
			try {
				session.parseBody(files);
			} catch (IOException e) {
				return this.serverError();
			} catch (ResponseException re) {
				return new Response(re.getStatus(), MIME_PLAINTEXT,
						re.getMessage());
			}
		}
		Map<String, String> params = session.getParms();

		if (null != params.get("action")) {
			String action = params.get("action").toLowerCase();
			Set<String> knowActions = actionsMap.keySet();
			if (knowActions.contains(action)) {
				String actionClass = actionsMap.get(action);
				try {
					ServeAction serve = (ServeAction) Class.forName(
							packageName + actionClass + parentClassName)
							.newInstance();
					return serve.response(mContext, params, files);
				} catch (Exception e) {
					e.printStackTrace();
					return serverUnsupport();
				}
			}
		}
		try {
			return this.serverStaticFile(session.getUri());
		} catch (Exception e) {
			e.printStackTrace();
			return this.serverError();
		}
	}

	private Response serverStaticFile(String uri) throws IOException,
			JSONException {

		if (uri.equals("/")) {
			return serverStaticFile(uri + defaultHomePage);
		} else if (uri.equals("/d")) {
			return serverStaticFile(downloadPage);
		} else if (!apkName.equals("")
				&& (uri.toLowerCase(Locale.getDefault()).indexOf(apkName) >= 0)) {
			return new DownloadServeAction().response(mAPKPathsString);
		}

		InputStream input = this.mContext.getAssets().open("wwwroot" + uri);
		if (null != input) {
			return new Response(Response.Status.OK, getFileMimeType(uri), input);
		}
		return this.server404();
	}

	@SuppressLint("DefaultLocale")
	private String getFileMimeType(String uri) {
		int dotPost = uri.lastIndexOf(".");
		String mime = null;
		if (dotPost > 0) {
			mime = knownMimeTypes.get(uri.substring(dotPost + 1).toLowerCase());
		}
		return mime == null ? MIME_DEFAULT_BINARY : mime;
	}

	private Response serverUnsupport() {
		Response res = new Response(Response.Status.REDIRECT, MIME_PLAINTEXT,
				ServerMsg.getMsg(ServerMsg.ACTION_UNSUPPORT));
		res.addHeader("Location", defaultHomePage);
		return res;
	}

	private Response serverError() {
		return new Response(Response.Status.OK, MIME_PLAINTEXT,
				ServerMsg.getMsg(ServerMsg.SREVER_ERROR));
	}

	private Response server404() {
		return new Response(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "");
	}

	public static String generateVerifyCode() {
		StringBuilder verifiedCode = new StringBuilder();
		Random rand = new Random();
		for (int i = 0; i < VERIFY_LENGHT; i++) {
			verifiedCode.append(rand.nextInt(10));
		}
		String code = verifiedCode.toString();
		GowebNanoHTTPD.verifyCode = code;
		return code;
	}
}