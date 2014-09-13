package cn.m15.app.android.gotransfer;

public class Const {
	public static final boolean DEBUG = true;
	public static final boolean SHARE_APK = true;

	public static final int APP_ID = 200;
	public static final String GO_APP_ID = "64";
	public static final int TIME_OUT = 1000 * 60 * 5;

	public static final String FEEDBACK_URL = DEBUG ? "http://tfeedback.tshenbian.com/"
			: "http://feedback.tshenbian.com/";

	public static final String QRCODE_URL = DEBUG ? "http://tfeedback.tshenbian.com/"
			: "http://feedback.tshenbian.com/";

	// WX
	public static final String WX_APP_ID = "wx5b3c9c0b056d9159";

	// SINA
	public static final String SINA_APP_KEY = "3195629764";

	public static final String USEDCHARACTORSET = "utf-8";

	// loader id
	// Note: Do not change the value of loader id !!!
	public static final int LOADER_PICTURE = 0;
	public static final int LOADER_VEDIO = 1;
	public static final int LOADER_MUSIC = 2;
	public static final int LOADER_FILES = 3;
	public static final int LOADER_APPS = 4;
	public static final int LOADER_MEDIA = 5;
	public static final int LOADER_CAMERA = 10;
	public static final int LOADER_CONVERSATION = 11;

	public static final String APPREFIX = "GOTRANSFER_";

	public static final String BROADCAST_ACTION_REFRESH_AP_LIST = "cn.m15.app.android.gotransfer.refreshaplist";
	public static final String BROADCAST_ACTION_AP_CONNECTED = "cn.m15.app.android.gotransfer.apconnected";

	public static final String BROADCAST_ACTION_RECEIVE_MSG = "cn.m15.app.android.gotransfer.receivemsg";
	public static final String BROADCAST_ACTION_REFRESH_USER_LIST = "cn.m15.app.android.gotransfer.refreshuserlist";
	public static final String BROADCAST_ACTION_RECEIVE_FILE = "cn.m15.app.android.gotransfer.receivefile";

	public static final String INTENT_EXTRA_AP_LIST = "aplist";
	public static final String INTENT_EXTRA_IP_ADDRESS = "ipaddress";
	public static final String INTENT_EXTRA_PACKET_NO = "packetno";
	public static final String INTENT_EXTRA_USERNAME = "username";
	public static final String INTENT_EXTRA_FILE_PATH = "filepath";
	public static final String INTENT_EXTRA_PERCENTAGE = "percentage";
	public static final String INTENT_EXTRA_PERCENTAGE_MAP = "percentagemap";

	public static final String INTENT_EXTRA_TRANSFER_TYPE = "transfertype";

	public static final int INTENT_EXTRA_TRANSFER_TYPE_RECEIVE = 0x00000000;
	public static final int INTENT_EXTRA_TRANSFER_TYPE_SEND = 0x00000001;

	public static final int MESSAGE_TO_DISMISS = 0x00000002;

	public static final String INTENT_EXTRA_USER_LIST = "userlist";
	public static final String INTENT_EXTRA_FILE_LIST = "filelist";
	public static final String INTENT_EXTRA_PACKETNO_LIST = "packetnolist";
	
	
	// Wifi 
	public static final int CREATE_AP_WAIT_SECONDES = 30;  // seconds

}
