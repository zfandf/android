package cn.m15.gotransfer.sdk.entity;

import java.util.ArrayList;

/**
 * 用于存储文件传输消息，根据其封装的数据对UI进行更新 
 * 该类主要存储三种消息：
 * 	1) 文件传输进度消息
 *  2) 接收到的"发送文件列表"的UDP消息
 *  3) 发送文件消息
 *  
 * 最后一种影响UI的是传输异常消息，该消息应即时通知UI更新，所以不放入该类中。
 */
public class TransferMsg {
	public int wholeStatus; // 文件传输整体状态
	public int status; // 文件传输状态
	
	// 以下参数用于存储文件传输进度
	public long packetNo; // 文件传输ID
	public String srcPath; // 传输文件的路径
	public String localPath; // 传输文件的路径
	public long transferSize; // 已经发送或接收的字节数
	public long totalSize; // 需要传输的总字节数
	
	// 以下参数用于存储"发送文件列表("UDP)"消息
	// packetNo, is_send=1, status=waiting_receive
	public String receiverName;
	public String macAddress;
	public ArrayList<TransferFile> files; // 等待接收或发送的文件列表
	// 以下参数用于发送文件列表
	public String senderName;
	// macAddress, is_send=0, status=waiting_send, files 
	
	public void reset() {
		wholeStatus = 0;
		status = 0;
		packetNo = 0;
		srcPath = null;
		localPath = null;
		transferSize = 0;
		totalSize = 0;
		receiverName = null;
		macAddress = null;
		files = null;
		senderName = null;
	}

	@Override
	public String toString() {
		return "FileTransferMsg [wholeStatus=" + wholeStatus + ", status="
				+ status + ", packetNo=" + packetNo + ", srcPath=" + srcPath
				+ ", localPath=" + localPath + ", transferSize=" + transferSize
				+ ", totalSize=" + totalSize + ", receiverName=" + receiverName
				+ ", macAddress=" + macAddress + ", files=" + files
				+ ", senderName=" + senderName + "]";
	}
	
}
