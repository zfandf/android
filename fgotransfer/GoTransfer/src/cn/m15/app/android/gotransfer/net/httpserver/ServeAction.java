package cn.m15.app.android.gotransfer.net.httpserver;

import java.util.Map;

import org.json.JSONException;

import cn.m15.app.android.gotransfer.net.httpserver.NanoHTTPD.Response;
import android.content.Context;


public class ServeAction {
	
	protected static final String MIME_PLAINTEXT = "text/plain";
	public static final int SUCCESS = 0;
	public static final int FAILED = 1;
	public static final int CONFIRM = 2;
	
	protected Response response(Context mContext, 
			Map<String, String> params, Map<String, String> files) throws JSONException {
		return createResponse(Response.Status.OK, MIME_PLAINTEXT, "");
	}
	
	protected Response createResponse(Response.Status status, String mimeType, String message) {
        Response res = new Response(status, mimeType, message);
        res.addHeader("Accept-Ranges", "bytes");
        res.addHeader("Access-Control-Allow-Origin", "*");
        res.addHeader("Access-Control-Allow-Methods", "GET");
        res.addHeader("Access-Control-Allow-Credentials", "true");
        return res;
    }
}