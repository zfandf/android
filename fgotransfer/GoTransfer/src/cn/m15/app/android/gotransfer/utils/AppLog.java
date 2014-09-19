package cn.m15.app.android.gotransfer.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.m15.gotransfer.sdk.utils.BaseLog;

import android.util.Log;

public class AppLog extends BaseLog {

	private static Logger sLog = null;
	private static AppLog sInstance = null;

	private AppLog() {
	}
	
	/**
	 * AppLog Singleton 
	 * @param args 可以传入是否Debug设置
	 * @return
	 */
	public static AppLog getInstance(boolean... args) {
		
		if (sInstance == null) {
			sInstance = new AppLog();			
			sLog = LoggerFactory.getLogger(AppLog.class);
		}

		if (args.length > 0)
			sInstance.mDebug = args[0];
		
		return sInstance;
	}
	
	public void i(String tag, String msg) {
		if (mDebug) {
			Log.i(tag, msg);
		}
	}

	public void d(String tag, String msg) {
		if (mDebug) {
			Log.d(tag, msg);
		}
		sLog.debug(tag + "\t" + msg);
	}

	public void w(String tag, String msg) {
		if (mDebug) {
			Log.w(tag, msg);
		}
		sLog.warn(tag + "\t" + msg);
	}

	public void e(String tag, String msg) {
		if (mDebug) {
			Log.e(tag, msg);
		}
		sLog.error(tag + "\t" + msg);
	}

	public void e(String tag, String msg, Throwable e) {
		if (mDebug) {
			Log.e(tag, msg, e);
		}
		sLog.error(tag + "\t" + msg + "\t" + e == null ? null : e.getMessage());
	}
}
