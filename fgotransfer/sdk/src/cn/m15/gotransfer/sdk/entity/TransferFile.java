package cn.m15.gotransfer.sdk.entity;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

public class TransferFile implements Parcelable, Serializable {
	private static final long serialVersionUID = 8191435339089560545L;
	
	// transfer status
	public static final int TRANSFER_UNKNOW = -1;
	public static final int TRANSFER_WAIT_RECEIVE = 0;
	public static final int TRANSFER_WAIT_SEND = 1;
	public static final int TRANSFER_RECEIVING = 2;
	public static final int TRANSFER_SENDING = 3;
	// transfer end status
	public static final int TRANSFER_RECEIVE_FINISH = 4;
	public static final int TRANSFER_SEND_FINISH = 5;
	public static final int TRANSFER_CANCEL = 6;
	public static final int TRANSFER_REFUSE = 7;
	public static final int TRANSFER_RECEIVE_FAILED = 8;
	public static final int TRANSFER_SEND_FAILED = 9;
	
	public String name;
	public long size;
	public String path; // local path
	public int fileType;
	public int progress = 0;
	public int transfer_status = TRANSFER_UNKNOW;
	public long lastModify;
	
	public TransferFile() {
	}
	
	private TransferFile(Parcel in) {
		name = in.readString();
		size = in.readLong();
		path = in.readString();
		fileType = in.readInt();
		progress = in.readInt();
		transfer_status = in.readInt();
		lastModify = in.readLong();
	}
	
	public static final Parcelable.Creator<TransferFile> CREATOR = new 
			Parcelable.Creator<TransferFile>() {
		
		public TransferFile createFromParcel(Parcel in) {
		    return new TransferFile(in);
		}
		
		public TransferFile[] newArray(int size) {
		    return new TransferFile[size];
		}
	};
	
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeLong(size);
		dest.writeString(path);
		dest.writeInt(fileType);
		dest.writeInt(progress);
		dest.writeInt(transfer_status);
		dest.writeLong(lastModify);
	}

	@Override
	public String toString() {
		return "TransferFile [name=" + name + ", size=" + size + ", path="
				+ path + ", fileType=" + fileType + ", progress=" + progress
				+ ", transfer_status=" + transfer_status + ", lastModify="
				+ lastModify + "]";
	}
	
}
