package cn.m15.app.android.gotransfer.net.httpserver;

import java.io.File;
import java.io.FileFilter;
import java.util.Date;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.content.Context;
import android.util.Log;
import cn.m15.app.android.gotransfer.net.httpserver.NanoHTTPD.Response;
import cn.m15.app.android.gotransfer.utils.FileUtil;

public class ListServeAction extends ServeAction {

	private static final FileFilter FILE_FILTER = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			return !pathname.isHidden() && pathname.canRead();
		}

	};

	private static final FileFilter DIR_FILTER = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			return !pathname.isHidden() && pathname.canRead()
					&& pathname.isDirectory();
		}
	};

	@Override
	protected Response response(Context mContext, Map<String, String> params,
			Map<String, String> files) throws JSONException {
		JSONObject result = new JSONObject();
		String path = params.get("path");
		String filter = params.get("filter");
		if (null == path) {
			result.put("code", "1").put("msg",
					ServerMsg.getMsg(ServerMsg.PARAM_MISSING));
		} else {
			Log.i("list start >>>>", new Date().getTime() + "");
			JSONArray lists = new JSONArray();
			File dir = new File(path);
			FileFilter fileFilter = (filter == null || filter.length() == 0) ? FILE_FILTER
					: DIR_FILTER;
			File[] list = dir.listFiles(fileFilter);
			FileUtil.sortList(list);

			for (File file : list) {
				JSONObject item = new JSONObject();
				String fileAbsPath = file.getAbsolutePath();
				item.put("path", fileAbsPath);
				item.put("name", file.getName());
				item.put("time", ValueConvertUtil.formateMil(file.lastModified()));
				// item.put("size",
				// FileUtil.formatFileSize(FileUtil.getFileAndDirSize(file)));
				if (file.isDirectory()) {
					if (filter != null) {
						item.put("has_sub_dir",
								FileUtil.isNotEmptyDir(fileAbsPath, fileFilter));
					}
					item.put("type", "dir");
				} else {
					int pos = file.getName().lastIndexOf(".");
					item.put("suffix", file.getName().substring(pos + 1));
					item.put("type", "file");
				}

				lists.put(item);
			}
			result.put("files", lists);
			result.put("code", 0);
			Log.i("list end >>>>", new Date().getTime() + "");
		}

		return createResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT,
				result.toString());
	}
}
