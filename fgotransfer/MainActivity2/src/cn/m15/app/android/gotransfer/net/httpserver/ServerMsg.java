package cn.m15.app.android.gotransfer.net.httpserver;

import java.util.HashMap;
import java.util.Locale;

public class ServerMsg {
	
	public static final String PARAM_MISSING = "params_missing";
	public static final String COPY_FAILED = "copy_failed";
	public static final String FILE_NOT_EXISTS = "file_not_exist";
	public static final String DIE_EXISTS = "dir_exist";
	public static final String CREATE_FAILED = "create_failed";
	public static final String DELETE_FAILED = "delete_failed";
	public static final String MOVE_FAILED = "move_failed";
	public static final String CODE_WRONG = "code_wrong";
	public static final String CARD_NAME = "card_name";
	public static final String INNER_NAME = "inner_name";
	public static final String ACTION_UNSUPPORT = "action_unsupport";
	public static final String SREVER_ERROR = "server_error";
	public static final String DISK_FULL = "disk_full";
	public static final String UPLOAD_FAILED = "upload_failed";
	public static final String NO_APPS = "no_apps";
	public static final String NEED_CONFRM = "need confirm";
	
	static final HashMap<String, String> CN_MSGS = new HashMap<String, String>() {{
		put("params_missing", "缺少参数");
		put("copy_failed", "复制失败");
		put("file_not_exists", "文件不存在");
		put("dir_exist", "文件夹已经存在");
		put("create_failed", "创建失败");
		put("delete_failed", "删除失败");
		put("move_failed", "移动失败，请刷新重试");
		put("code_wrong", "验证码错误");
		put("card_name", "SD卡");
		put("inner_name", "手机内存");
		put("action_unsupport", "action不存在");
		put("server_error", "服务器错误");
		put("disk_full", "存储空间不足");
		put("upload_failed", "文件上传失败");
		put("no_apps", "无app");
		put("need confirm", "需要确认");
	}};
	static final HashMap<String, String> EN_MSGS = new HashMap<String, String>() {{
		put("params_missing", "缺少参数");
		put("copy_failed", "copy failed");
		put("file_not_exists", "file not exist");
		put("dir_exist", "dir has already exist");
		put("create_failed", "create failed");
		put("delete_failed", "delete failed");
		put("move_failed", "move failed, please refresh and retry");
		put("code_wrong", "verify code is wrong");
		put("card_name", "SD card");
		put("inner_name", "Inner Storage");
		put("server_error", "server error");
		put("disk_full", "not enough space");
		put("upload_failed", "upload failed");
		put("no_apps", "no app");
		put("need confirm", "need confirm");
	}};
	static final HashMap<String,HashMap<String,String>> msg = new HashMap<String, HashMap<String, String>>(){{
		put("cn", CN_MSGS);
		put("en", EN_MSGS);
	}};
	
	static String getMsg(String key) {
		String country = getCountry();
		return msg.get(country).get(key);
	}
	
	public static String getCountry() {
		String country = "en";
		Locale locale = Locale.getDefault();
		String c = locale.getCountry();
		if (c.equalsIgnoreCase("cn")) {
			country = "cn";
		}
		return country;
	}

}
