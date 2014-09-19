
/**
 * Log 管理, 可以通过setLogInstance，替换Library中默认的Log
 *
 */

package cn.m15.gotransfer.sdk.utils;

public class LogManager {
	private static BaseLog sLog = null;

	/**
	 * 获取目前挂载的Log
	 * @return BaseLog
	 */
	public static BaseLog getLogInstance() {
		if (sLog == null)
			sLog = SdkLog.getInstance();
		return sLog;
	}

	/**
	 * 设置Log实例
	 * @param log
	 */
	public static void setLogInstance(BaseLog log) {
		sLog = log;
	}

	/**
	 * 类似于Android Log.i(String tag, String msg), 仅在Log instance设置为Debug状态时才输出
	 * @param tag
	 * @param msg
	 */
	public static void i(String tag, String msg) {
		getLogInstance().i(tag, msg);
	}

	/**
	 * 类似于Android Log.d(String tag, String msg), 仅在Log instance设置为Debug状态时才输出
	 * @param tag
	 * @param msg
	 */
	public static void d(String tag, String msg) {
		getLogInstance().d(tag, msg);
	}

	/**
	 * 类似于Android Log.w(String tag, String msg), 仅在Log instance设置为Debug状态时才输出
	 * @param tag
	 * @param msg
	 */
	public static void w(String tag, String msg) {
		getLogInstance().w(tag, msg);
	}

	/**
	 * 类似于Android Log.e(String tag, String msg), 仅在Log instance设置为Debug状态时才输出
	 * @param tag
	 * @param msg
	 */
	public static void e(String tag, String msg) {
		getLogInstance().e(tag, msg);
	}

	/**
	 * 类似于Android Log.e(String tag, String msg, Throwable e), 仅在Log instance设置为Debug状态时才输出
	 * @param tag
	 * @param msg
	 */
	public static void e(String tag, String msg, Throwable e) {
		getLogInstance().e(tag, msg);
	}
	
}
