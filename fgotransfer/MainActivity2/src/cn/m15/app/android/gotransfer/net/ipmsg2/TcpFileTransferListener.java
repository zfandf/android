package cn.m15.app.android.gotransfer.net.ipmsg2;

import java.util.ArrayList;
import java.util.Map;

import cn.m15.app.android.gotransfer.enity.TransferFile;


public interface TcpFileTransferListener {

	/**
	 * 文件(夹)发送进度
	 * @param packetNo 		   文件传输唯一标识
	 * @param srcPath 发送文件(夹)的源路径
	 * @param sendedSize	   已发送的字节数
	 * @param totalSize	 	   总字节数字节数
	 */
	public void onSendFileProgress(long packetNo, String srcPath, long sendedSize, long totalSize);
	
	/**
	 * 文件(夹)接收进度
	 * @param packetNo 		文件传输唯一标识
	 * @param srcPath 		接收文件(夹)的源路径
	 * @param receivedSize	已接收的字节数
	 * @param totalSize	 	 总字节数字节数
	 */
	public void onReceiveFileProgress(long packetNo, String srcPath, long receivedSize, long totalSize);

	/**
	 * 客户端关闭，主要由IOException和ClosedChannelException引起
	 * @param packetNo 
	 */
	public void onClientClosed(long packetNo);
	
	/**
	 * 服务端关闭与某个客户端的连接
	 * @param packetNo
	 */
	public void onCloseConnectionWithClient(long packetNo);


	/**
	 * 服务端关闭，主要由IOException和ClosedChannelException引起
	 * @param packetNoMap 
	 */
	public void onServerClosed(Map<String, Long> packetNoMap);

	/**
	 * 发送方，发送的文件已经不存在
	 * @param packetNo
	 * @param filePath 
	 */
	public void onSendedFileNotExist(long packetNo, String filePath);

	/**
	 * 接收方，请求发送的文件已经不存在，不包括文件夹内文件不存在的情况
	 * @param packetNo
	 * @param filePath
	 */
	public void onReceivedFileNotExsit(long packetNo, String filePath);

	/**
	 * 文件全部接收完
	 * @param packetNo
	 */
	public void onFileReceiveFinish(long packetNo);

	/**
	 * 文件全部发送完
	 * @param packetNo
	 */
	public void onFileSendFinish(long packetNo);

	/**
	 * 对方取消整体传输
	 * @param packetNo
	 */
	public void onCancelledByPeer(long packetNo);

	/**
	 * 接收方存储空间不足
	 */
	public void onStorageTooSmallAtReceiver(long packetNo);

	/**
	 * 在接收文件之前，将接收文件的本地路径存入数据库
	 * @param packetNo
	 * @param srcPath
	 * @param localPath
	 */
	public void onBeforeReceiveFile(long packetNo, String srcPath, String localPath);

	/**
	 * 接收到"发送文件列表(UDP)"消息
	 * @param message
	 * @param files
	 */
	public void onReceiveSendFileUdp(UdpMessage message, ArrayList<TransferFile> files);

	/**
	 * 发送方得知拒绝接收文件
	 * @param packetNo
	 */
	public void onRefuseReceive(long packetNo);

}
