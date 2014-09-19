
/**
 * Library 中使用的常量定义
 */
package cn.m15.gotransfer.sdk;

public class SdkConst {

	/**
	 * 创建的AP的前缀
	 */
	public static final String AP_PREFIX = "GT_";

	/**
	 * 默认使用的字符集
	 */
	public static final String USEDCHARACTORSET = "utf-8";

	/**
	 * 传输AP列表Intent使用的Key
	 */
	public static final String INTENT_EXTRA_AP_LIST = "aplist";

	/**
	 * 发现新的GoTransfer WIFI 
	 */
	public static final String BROADCAST_ACTION_REFRESH_AP_LIST = "cn.m15.app.android.gotransfer.refreshaplist";

	/**
	 * 收到新的传输文件
	 */
	public static final String BROADCAST_ACTION_RECEIVE_FILE = "cn.m15.app.android.gotransfer.receivefile";

	/**
	 * 收到新的聊天消息，目前未使用
	 */
	public static final String BROADCAST_ACTION_RECEIVE_MSG = "cn.m15.app.android.gotransfer.receivemsg";
	
	/**
	 * 创建Wifi AP 的最长等待时间, 单位为秒
	 */
	public static final int CREATE_AP_WAIT_SECONDES = 30; 

	/**
	 * 控制是否支持分享APK，目前默认为支持
	 */
	public static final boolean SHARE_APK = true;

}
