package cn.m15.gotransfersimplest.utils;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import cn.m15.gotransfersimplest.Const;
import cn.m15.gotransfersimplest.R;
import cn.m15.gotransfersimplest.entry.TransferFile;

public class ValueConvertUtil {
	
	public static String getFileTypeStr(Context ctx, int fileType) {
		switch (fileType) {
		case Const.PICTURE:
			return ctx.getString(R.string.picture);
		case Const.VIDEO:
			return ctx.getString(R.string.video);
		case Const.MUSIC:
			return ctx.getString(R.string.music);
		case Const.APP:
			return ctx.getString(R.string.app);
		case Const.FILE:
			return ctx.getString(R.string.file);
		default:
			return null;
		}
	}
	
	public static String convertToJsonString(String groupName, ArrayList<TransferFile> files) {
		JSONObject json = new JSONObject();
		try {
			json.put("group_name", groupName);
			JSONArray jsonArray = new JSONArray();
			for (TransferFile file : files) {
				jsonArray.put(file.toJson());
			}
			json.put("items", jsonArray);
		} catch (JSONException e) {
		}
		return json.toString();
	}
}
