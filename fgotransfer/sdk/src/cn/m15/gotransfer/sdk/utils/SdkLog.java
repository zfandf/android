/**
 * 默认的Log实现，通过系统Log实现Log输出, 通过Debug选项控制是否输出Log
 */
package cn.m15.gotransfer.sdk.utils;

import android.util.Log;

public class SdkLog extends BaseLog {

	private static SdkLog sInstance;

	private SdkLog() {
	}
	
	/**
	 * 获取Log instance
	 * @return
	 */
	public static SdkLog getInstance() {
		if (sInstance == null) {
			sInstance = new SdkLog();			
		}
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
	}

	public void w(String tag, String msg) {
		if (mDebug) {
			Log.w(tag, msg);
		}
	}

	public void e(String tag, String msg) {
		if (mDebug) {
			Log.e(tag, msg);
		}
	}

	public void e(String tag, String msg, Throwable e) {
		if (mDebug) {
			Log.e(tag, msg, e);
		}
	}
}
