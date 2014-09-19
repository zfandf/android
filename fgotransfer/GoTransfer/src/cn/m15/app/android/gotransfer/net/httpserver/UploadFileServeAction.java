package cn.m15.app.android.gotransfer.net.httpserver;

import java.io.File;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import cn.m15.app.android.gotransfer.net.httpserver.NanoHTTPD.Response;
import cn.m15.app.android.gotransfer.utils.FileUtil;

public class UploadFileServeAction extends ServeAction {

	@Override
	protected Response response(Context mContext, Map<String, String> params,
			Map<String, String> files) throws JSONException {
		JSONObject result = new JSONObject();
		String path = params.get("path");
		if (path == null || files.size() == 0) {
			result.put("code", 1).put("msg",
					ServerMsg.getMsg(ServerMsg.PARAM_MISSING));
		} else {
			int successCount = 0;
			int totalCount = files.size();
			for (Map.Entry<String, String> entry : files.entrySet()) {
				String fileName = entry.getKey();
				String fileAbsPath = entry.getValue();
				String fileOriginName = params.get(fileName);
				if (new File(fileAbsPath).length() > 0) {
					if (new File(path, fileOriginName).exists()) {
						fileOriginName = FileUtil
								.renameDumplicatedFile(fileOriginName);
					}
					int moveResult = MoveFile.moveFile(fileAbsPath, path, 1,
							fileOriginName);
					if (moveResult == 0) {
						successCount++;
					}

				}
			}
			if (successCount != totalCount) {
				result.put("code", 1).put("msg",
						ServerMsg.getMsg(ServerMsg.UPLOAD_FAILED));
			} else {
				result.put("code", 0).put("count", successCount);
			}
		}
		return createResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT,
				result.toString());
	}

}
