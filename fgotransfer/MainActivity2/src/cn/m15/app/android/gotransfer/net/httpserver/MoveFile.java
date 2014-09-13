package cn.m15.app.android.gotransfer.net.httpserver;

public class MoveFile {
	
	public static int moveFile(String from, String to, int force) {
		return MoveFile.moveFile(from, to, force, "");
	}
	
	public static int moveFile(String from, String to, int force, String newName) {
		int copyResult = CopyFile.copyFile(from, to, force, newName);
		if (copyResult == ServeAction.SUCCESS) {
			boolean deleteCode = DeleteFile.deleteDir(from);
			if (deleteCode) {
				return ServeAction.SUCCESS;
			}
		}
		return copyResult;
	}
}