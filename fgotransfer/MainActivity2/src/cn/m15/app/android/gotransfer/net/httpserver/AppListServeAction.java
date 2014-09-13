package cn.m15.app.android.gotransfer.net.httpserver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.m15.app.android.gotransfer.Const;
import cn.m15.app.android.gotransfer.net.httpserver.NanoHTTPD.Response;
import cn.m15.app.android.gotransfer.utils.FileUtil;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

public class AppListServeAction extends ServeAction {

	public static final String ICONS_CACHE = Const.APPREFIX + "webcache";

	@Override
	protected Response response(Context mContext, Map<String, String> params,
			Map<String, String> files) throws JSONException {
		JSONObject result = new JSONObject();
		TreeMap<String, String> apps = getPackageInfo(mContext);

		if (apps.size() == 0) {
			result.put("code", 1).put("msg",
					ServerMsg.getMsg(ServerMsg.NO_APPS));
		} else {
			JSONArray applist = new JSONArray();
			for (Map.Entry<String, String> entry : apps.entrySet()) {
				JSONObject app = new JSONObject();
				app.put("name", entry.getValue()).put("path", entry.getKey());
				applist.put(app);
			}
			result.put("code", 0).put("apps", applist);
		}
		return createResponse(Response.Status.OK, MIME_PLAINTEXT,
				result.toString());
	}

	private TreeMap<String, String> getPackageInfo(Context mContext) {
		TreeMap<String, String> result = new TreeMap<String, String>();
		// get all packages
		PackageManager packageManager = mContext.getPackageManager();
		List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
		// list.size() may equals to zero
		if (packageInfos.size() > 0) {
			String[] storages = FileUtil.getVolumePaths(mContext);
			String savePath = "";
			for (String storage : storages) {
				if (FileUtil.isInternalStoragePath(mContext, storage)) {
					savePath = storage;
				}
			}
			File cacheDir = new File(savePath + File.separator + ICONS_CACHE);
			if (!cacheDir.exists()) {
				cacheDir.mkdirs();
			}
			FileOutputStream out = null;
			ApplicationInfo appInfo = null;
			BitmapDrawable drawable = null;
			Bitmap bitmap = null;
			String packageLable = "";
			String path = "";
			File file = null;
			for (PackageInfo p : packageInfos) {
				if ((p.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
					// get bitmap
					appInfo = p.applicationInfo;
					drawable = (BitmapDrawable) packageManager
							.getApplicationIcon(appInfo);
					bitmap = drawable.getBitmap();
					// write file
					packageLable = packageManager.getApplicationLabel(appInfo)
							.toString();
					path = cacheDir + File.separator + packageLable + ".png";
					file = new File(path);
					if (!file.exists()) {
						try {
							out = new FileOutputStream(file);
							if (bitmap.compress(Bitmap.CompressFormat.PNG, 100,
									out)) {
								result.put(path, packageLable);
							}
							out.flush();
							out.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else {
						result.put(path, packageLable);
					}
				}
			}
		}
		return result;
	}
}
