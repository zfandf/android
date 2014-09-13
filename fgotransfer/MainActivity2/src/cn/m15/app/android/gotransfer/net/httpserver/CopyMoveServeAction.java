package cn.m15.app.android.gotransfer.net.httpserver;

import java.io.File;
import java.util.LinkedList;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.m15.app.android.gotransfer.net.httpserver.NanoHTTPD.Response;
import cn.m15.app.android.gotransfer.utils.FileUtil;
import android.content.Context;

public class CopyMoveServeAction extends ServeAction {

	@Override
	protected Response response(Context mContext, Map<String, String> params,
			Map<String, String> files) throws JSONException {
		JSONObject result = new JSONObject();
		String from = params.get("from");
		String to = params.get("to");
		String action = params.get("action");
		int force = Integer.parseInt(params.get("force"));
		if (from == null || to == null) {
			result.put("code", FAILED).put("msg",
					ServerMsg.getMsg(ServerMsg.PARAM_MISSING));
		} else {
			String[] fromFiles = (from.indexOf("|") != -1) ? from.split("[|]")
					: new String[] { from };
			if (FileUtil.checkDiskSpace(fromFiles, to)) {
				result.put("code", FAILED).put("msg",
						ServerMsg.getMsg(ServerMsg.DISK_FULL));
			} else {
				int successFileCount = 0;
				int successDirCount = 0;
				int notExistsCount = 0;
				int ret = 1;
				LinkedList<String> dumplicateFiles = new LinkedList<String>();
				for (String f : fromFiles) {
					File file = new File(f);
					if (file.exists()) {
						if (action.equals("copy")) {
							ret = CopyFile.copyFile(f, to, force);
						} else if (action.equals("move")) {
							ret = MoveFile.moveFile(f, to, force);
						}
						if (ret == SUCCESS) {
							if (file.isFile()) {
								successFileCount++;
							} else {
								successDirCount++;
							}
						} else if (ret == CONFIRM) {
							dumplicateFiles.add(f);
						}
					} else {
						notExistsCount++;
					}
				}
				if (notExistsCount > 0) {
					result.put("code", FAILED);
					result.put(
							"msg",
							notExistsCount
									+ " "
									+ ServerMsg
											.getMsg(ServerMsg.FILE_NOT_EXISTS));
				} else if (dumplicateFiles.size() > 0) {
					result.put("code", CONFIRM);
					result.put("paths", new JSONArray(dumplicateFiles));
				} else {
					result.put("code", SUCCESS);
				}
				result.put("file_count", successFileCount).put("dir_count",
						successDirCount);
			}
		}
		return createResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT,
				result.toString());
	}

}
