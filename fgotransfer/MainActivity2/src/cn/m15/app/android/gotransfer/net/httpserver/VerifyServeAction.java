package cn.m15.app.android.gotransfer.net.httpserver;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import cn.m15.app.android.gotransfer.net.httpserver.NanoHTTPD.Response;
import android.content.Context;

public class VerifyServeAction extends ServeAction {

	@Override
	protected Response response(Context mContext, Map<String, String> params,
			Map<String, String> files) throws JSONException {
		JSONObject result = new JSONObject();
		String code = params.get("code");
		if (code == null || !GowebNanoHTTPD.verifyCode.equals(code)) {
			result.put("code", 1).put("msg",
					ServerMsg.getMsg(ServerMsg.CODE_WRONG));
		} else {
			result.put("code", 0);
		}
		return createResponse(Response.Status.OK, MIME_PLAINTEXT,
				result.toString());
	}

}