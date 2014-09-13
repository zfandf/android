package cn.m15.app.android.gotransfer.net.ipmsg2;

/**
 * 协议常量表
 */
public class MessageConst {
	
	/**
	 * 协议版本号
	 */
	public static final int VERSION = 0x009;
	
	/**
	 * TCP/UDP端口号，协议默认端口2425
	 */
	public static final int PORT = 0x0979;
	
	/**
	 * UDP通信协议常量表
	 */
	public static class UdpMessageConst {
		
		/**
		 * UDP分包版本
		 */
		public static final short PACKET_VERSION = 0x01;
		
		/**
		 * UDP数据包最大长度：32KB
		 */
		public static final int PACKET_MAX_LENGTH = 32 * 1024;
		
		/**
		 * UDP数据包包头长度：32bytes
		 */
		public static final int PACKET_HRED_LENGTH = 32;
	}
	
	/**
	 * TCP通信协议常量表
	 */
	public static class TcpMessageConst {
		
		/**
		 * TCP分包版本
		 */
		public static final short PACKET_VERSION = 0x01;
		
		/**
		 * TCP数据包最大长度：64KB
		 */
		public static final int PACKET_MAX_LENGTH = 64 * 1024;  
		
		/**
		 * TCP数据包包头长度：32bytes
		 */
		public static final int PACKET_HRED_LENGTH = 32;
		
		/**
		 * 传输数据类型：数据
		 */
		public static final byte DATA_TYPE_DATA = 0;		
		
		/**
		 * 传输数据类型：命令
		 */
		public static final byte DATA_TYPE_COMMAND = 1;      
		
	}

	/**
	 * 命令常量表
	 */
	public static class Command {
		
		/**
		 * 不进行任何操作
		 */
		public static final int NOOPERATION = 0x00000000;
		
		/**
		 * (UDP) 用户上线
		 */
		public static final int BR_ENTRY = 0x00000001;
		
		/**
		 * (UDP) 用户退出
		 */
		public static final int BR_EXIT = 0x00000002; 
		
		/**
		 * (UDP) 通报在线
		 */
		public static final int ANSENTRY = 0x00000003; 

		/**
		 * (UDP) 发送消息
		 */
		public static final int SENDMSG = 0x00000101;
		
		/**
		 * (UDP) 通报收到消息
		 */
		public static final int RECVMSG = 0x00000102;
		
		/**
		 * (UDP) 消息打开确认通知
		 */
		public static final int ANSREADMSG = 0x00000103; 

		/**
		 * (UDP) 发送文件列表
		 */
		public static final int SEND_FILE_LIST = 0x00000201;
		
		/**
		 * (TCP) 文件发送请求
		 */
		public static final int REQUEST_SEND_FILE = 0x00000202;
		
		/**
		 * (TCP) 文件夹发送请求
		 */
		public static final int REQUEST_SEND_DIR = 0x00000203;
		
		/**
		 * (UDP) 拒绝接收文件
		 */
		public static final int REFUSE_RECEIVE = 0x00000204;
		
		/**
		 * (TCP) 取消文件传输
		 */
		public static final int CANCEL_TRANSFER = 0x00000205;
		
		/**
		 * (TCP) 暂停文件传输
		 */
		public static final int PAUSE_RECEIVE = 0x00000206;
		
		/**
		 * (TCP) 回应文件传输
		 */
		public static final int RESPONSE_SEND_FILE = 0x00000207;
		
		/**
		 * (TCP) 回应文件夹传输
		 */
		public static final int RESPONSE_SEND_DIR = 0x00000208;
		
		/**
		 * (UDP) 回应发送文件列表
		 */
		public static final int RESPONSE_SEND_FILE_REQUEST = 0x00000209;
		
		/**
		 * 结束线程
		 */
		public static final int THREAD_MESSAGE_END = 0x90000001;
	}
	
	/**
	 * 特殊协议参数常量表
	 */
	public static class Args {
		
		/**
		 * 协议版本号
		 */
		public static final String VERSION = "version";
		
		/**
		 * 命令号
		 */
		public static final String COMMAND_NO = "commandNo";
		
		/**
		 * 数据包ID
		 */
		public static final String PACKET_NUMBER = "packetNo";
		
		/**
		 * 内容消息
		 */
		public static final String MSG_TEXT = "msgText";
		
		/**
		 * 文件传输支持(压缩、打包、断电续传) {@link Support}
		 */
		public static final String MSG_SUPPORT = "support";
		
		/**
		 * 文件列表
		 */
		public static final String FILE_LIST = "fileList"; 
		
		/**
		 * 文件/文件夹名称
		 */
		public static final String FILE_NAME = "fileName";
		
		/**
		 * 文件/文件夹路径
		 */
		public static final String FILE_PATH = "filePath";
		
		/**
		 * 文件夹路径
		 */
		public static final String DIR_PATH = "folderPath";
		
		/**
		 * 文件/文件夹大小
		 */
		public static final String FILE_SIZE = "fileSize";
		
		/**
		 * 文件类型 {@link FileType}
		 */
		public static final String FILE_TYPE = "fileType"; 
		
		/**
		 * 文件状态{@link FileStatus}
		 */
		public static final String FILE_STATUS = "fileStatus";
		
		/**
		 * 压缩格式
		 */
		public static final String COMPRESS = "compress"; 
		
		/**
		 * 断点续传位置(续传文件时传入)
		 */
		public static final String START_POS = "startPos";
		
		/**
		 * 文件最后修改时间
		 */
		public static final String LAST_MODIFY = "lastModify";
	}
	
	/**
	 * 文件类型常量表
	 */
	public static class FileType {
		
		/**
		 * 图片
		 */
		public static final int PICTURE = 0;
		
		/**
		 * 视频
		 */
		public static final int VIDEO = 1;
		
		/**
		 * 音乐
		 */
		public static final int MUSIC = 2; 
		
		/**
		 * 其他文件
		 */
		public static final int OTHERS = 3;
		
		/**
		 * 应用
		 */
		public static final int APP = 4;
		
		/**
		 * 文件夹	
		 */
		public static final int DIR = 5; 	
	}
	
	/**
	 * 文件状态
	 */
	public static class FileStatus {
		
		/**
		 * 正常
		 */
		public static final int NORMAL = 0; 
		
		/**
		 * 文件不存在
		 */
		public static final int NOT_EXSIT = 1;
		
		/**
		 * 文件已修改
		 */
		public static final int MODIFIED = 2; 
	}
	
	/**
	 * 压缩、打包、断点续传常量表
	 * 	如果你想同时支持多个操作，例如：断点续传和ZIP压缩，使用BREAKPOINT_RESUME|ZIP形式
	 */
	public static class Support {
		
		/**
		 * 支持打包
		 */
		public static final short ARCHIVE = 0x01;
		
		/**
		 * 支持断点续传
		 */
		public static final short BREAKPOINT_RESUME = 0x02;
		
		/**
		 * 支持ZIP压缩
		 */
		public static final short ZIP = 0x04; 
		
		/**
		 * 支持GZIP压缩
		 */
		public static final short GZIP = 0x08;
		
		/**
		 * 支持RAR压缩
		 */
		public static final short RAR = 0x10; 
		
		/**
		 * 支持BZIP2压缩
		 */
		public static final short BZIP2 = 0x20;
	}
	
	/**
	 * 传输状态
	 */
	public static class Status {
	
		/**
		 * 等待接收
		 */
		public static final int WAIT_RECEIVE = 100;
		
		/**
		 * 等待发送
		 */
		public static final int WAIT_SEND = 101;
		
		/**
		 * 接收中
		 */
		public static final int RECEIVING = 102;
		
		/**
		 * 发送中
		 */
		public static final int SENDING = 103;
		
		/**
		 * 接收完成, 同时也代表文件传世终止状态的最小值
		 */
		public static final int RECEIVE_FINISH = 200;
		
		/**
		 * 发送完成
		 */
		public static final int SEND_FINISH = 201;
		
		/**
		 * 已取消
		 */
		public static final int CANCELLED = 202;
		
		/**
		 * 对方已取消
		 */
		public static final int CANCELLED_BY_PEER = 203;
		
		/**
		 * 已拒绝
		 */
		public static final int REFUSED = 204;
		
		/**
		 * 接收失败
		 */
		public static final int RECEIVE_FAILED = 205;
		
		/**
		 * 发送失败
		 */
		public static final int SEND_FAILED = 206;		
	}
	
}
