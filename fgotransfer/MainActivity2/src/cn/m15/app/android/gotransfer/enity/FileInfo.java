package cn.m15.app.android.gotransfer.enity;

import android.os.Parcel;
import android.os.Parcelable;

public class FileInfo implements Parcelable {
	public String name;
	public String path;
	public boolean isDir;
	public long size;
	public long date; // ms
	public int type = 100;
	public boolean isBack;

	public FileInfo() {

	}

	private FileInfo(Parcel in) {
		name = in.readString();
		path = in.readString();
		isDir = in.readInt() == 0 ? false : true;
		size = in.readLong();
		date = in.readLong();
		type = in.readInt();
		isBack = in.readInt() == 0 ? false : true;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(path);
		dest.writeInt(isDir ? 1 : 0);
		dest.writeLong(size);
		dest.writeLong(date);
		dest.writeInt(type);
		dest.writeInt(isBack ? 1 : 0);
	}

	public static final Parcelable.Creator<FileInfo> CREATOR = new Parcelable.Creator<FileInfo>() {

		public FileInfo createFromParcel(Parcel in) {
			return new FileInfo(in);
		}

		public FileInfo[] newArray(int size) {
			return new FileInfo[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}
