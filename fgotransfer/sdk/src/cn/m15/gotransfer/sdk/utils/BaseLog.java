/**
 * 抽象Log类，SdkLog 派生自 BaseLog
 */
package cn.m15.gotransfer.sdk.utils;

public abstract class BaseLog {
	
	public boolean mDebug = false;
	
	/**
	 * 设置是否Debug状态, 控制log的输出
	 * @param debug
	 */
	public void setDebug(boolean debug) {
		mDebug = debug;
	}
	
	public abstract void i(String tag, String msg);

	public abstract void d(String tag, String msg);

	public abstract void w(String tag, String msg);

	public abstract void e(String tag, String msg);

	public abstract void e(String tag, String msg, Throwable e);
}
