package cn.m15.app.android.gotransfer.net.httpserver;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import cn.m15.app.android.gotransfer.net.httpserver.NanoHTTPD.Response;
import android.content.Context;

public class GetCountryServeAction extends ServeAction {

	@Override
	protected Response response(Context mContext, Map<String, String> params,
			Map<String, String> files) throws JSONException {
		JSONObject result = new JSONObject();
		result.put("code", 0);
		String country = ServerMsg.getCountry();
		result.put("country", country);
		return createResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT,
				result.toString());
	}
}
