package cn.m15.app.android.gotransfer.net.ipmsg2;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 用户类，对应局域网中每个在线用户的信息
 * 
 */
public class User implements Parcelable, Serializable {
	private static final long serialVersionUID = 3624909747427176090L;

	private String userName; // 用户名
	private String alias; // 别名（若为pc，则是登录名）
	private String groupName; // 组名
	private String ip; // ip地址
	private String hostName; // 主机名
	private String mac; // MAC地址
	private int msgCount; // 未接收消息数
	private int status; // 标识接收人

	public User() {
		msgCount = 0; // 初始化为零
	}

	public User(String userName, String alias, String groupName, String ip,
			String hostName, String mac) {
		super();
		this.userName = userName;
		this.alias = alias;
		this.groupName = groupName;
		this.ip = ip;
		this.hostName = hostName;
		this.mac = mac;
		msgCount = 0; // 初始化为零
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public int getMsgCount() {
		return msgCount;
	}

	public void setMsgCount(int msgCount) {
		this.msgCount = msgCount;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
	private User(Parcel in) {
		userName = in.readString();
		alias = in.readString();
		groupName = in.readString();
		ip = in.readString();
		hostName = in.readString();
		mac = in.readString();
		msgCount = in.readInt();
		status = in.readInt();
	}

	public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {

		public User createFromParcel(Parcel in) {
			return new User(in);
		}

		public User[] newArray(int size) {
			return new User[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(userName);
		dest.writeString(alias);
		dest.writeString(groupName);
		dest.writeString(ip);
		dest.writeString(hostName);
		dest.writeString(mac);
		dest.writeInt(msgCount);
		dest.writeInt(status);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (!(o instanceof User)) {
			return false;
		}
		if (o == this) {
			return true;
		}
		User user = (User) o;

		return this.mac.equals(user.mac);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mac == null) ? 0 : mac.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "User [userName=" + userName + ", alias=" + alias
				+ ", groupName=" + groupName + ", ip=" + ip + ", hostName="
				+ hostName + ", mac=" + mac + ", msgCount=" + msgCount
				+ ", status=" + status + "]";
	}

}
