package cn.m15.app.android.gotransfer.utils;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import cn.m15.app.android.gotransfer.R;
import cn.m15.app.android.gotransfer.enity.TransferFilesManager;
import cn.m15.app.android.gotransfer.net.ipmsg2.User;
import cn.m15.app.android.gotransfer.ui.fragment.dialog.ChangeNameDialog;
import cn.m15.app.android.gotransfer.ui.fragment.dialog.CommonDialogFragment;
import cn.m15.app.android.gotransfer.ui.fragment.dialog.CommonDialogFragment.DialogParams;
import cn.m15.app.android.gotransfer.ui.fragment.dialog.FileDetailDialog;
import cn.m15.app.android.gotransfer.ui.fragment.dialog.FileOptionsDialog;
import cn.m15.app.android.gotransfer.ui.fragment.dialog.SendFilesDialog;

public class DialogUtil {
	public static final String DIALOG_CANNOT_CHOOSER_FILES = "dialog_cannot_chooser_files";
	public static final String DIALOG_CLOSE_CONNECTION = "dialog_close_connection";
	public static final String DIALOG_CLEAR_SELECTED_FILES = "dialog_clear_selected_files";
	public static final String DIALOG_SEND_FILES = "dialog_send_files";
	public static final String DIALOG_NETWORK_DISCONNECTED = "dialog_network_disconnected";
	public static final String DIALOG_CANCEL_TRANSFER = "dialog_cancel_transfer";
	public static final String DIALOG_DISCONNECT_ALL_TRANSFERS = "dialog_disconnect_all_transfers";
	public static final String DIALOG_NOT_ENOUGH_SPACE = "dialog_not_enough_space";
	public static final String DIALOG_CHANGE_NAME = "dialog_change_name";
	public static final String DIALOG_FILE_OPTIONS = "dialog_file_options";
	public static final String DIALOG_FILE_DETAIL = "dialog_file_detail";
	public static final String DIALOG_WAIT_JOIN_TIME_TOO_LONG = "dialog_wait_join_time_too_long";

	private DialogUtil() {
	}

	public static void showCannotChooserFilesDialog(FragmentActivity activity) {
		DialogParams params = new DialogParams();
		params.tag = DIALOG_CANNOT_CHOOSER_FILES;
		params.contentText = activity.getString(R.string.sdcard_disable);
		params.btnLeftText = activity.getString(R.string.i_know);
		params.textGravity = Gravity.CENTER;
		params.canceledOnTouchOutside = false;
		CommonDialogFragment dialog = new CommonDialogFragment();
		dialog.setParams(params);
		dialog.setCancelable(false);
		dialog.show(activity);
	}

	public static void showCloseConnectionDialog(FragmentActivity activity) {
		DialogParams params = new DialogParams();
		params.tag = DIALOG_CLOSE_CONNECTION;
		params.contentText = activity.getString(R.string.close_connection_content);
		params.btnLeftText = activity.getString(R.string.close_connection);
		params.btnRightText = activity.getString(R.string.cancel);
		params.textGravity = Gravity.CENTER;
		params.canceledOnTouchOutside = false;
		CommonDialogFragment dialog = new CommonDialogFragment();
		dialog.setParams(params);
		dialog.setCancelable(false);
		dialog.show(activity);
	}

	public static void showDisconnectAllTransfersDialog(FragmentActivity activity) {
		DialogParams params = new DialogParams();
		params.tag = DIALOG_DISCONNECT_ALL_TRANSFERS;
		params.contentText = activity.getString(R.string.disconnect_all_transfers_content);
		params.btnLeftText = activity.getString(R.string.close_connection);
		params.btnRightText = activity.getString(R.string.cancel);
		params.textGravity = Gravity.CENTER;
		params.canceledOnTouchOutside = false;
		CommonDialogFragment dialog = new CommonDialogFragment();
		dialog.setParams(params);
		dialog.setCancelable(false);
		dialog.show(activity);
	}

	public static void showClearSelectedFilesDialog(FragmentActivity activity) {
		DialogParams params = new DialogParams();
		params.tag = DIALOG_CLEAR_SELECTED_FILES;
		TransferFilesManager transferFilesManager = TransferFilesManager.getInstance();
		transferFilesManager.size();
		params.contentText = String.format(
				activity.getResources().getString(R.string.clear_all_selected_files),
				transferFilesManager.size());
		params.btnLeftText = activity.getString(R.string.ok);
		params.btnRightText = activity.getString(R.string.cancel);
		params.textGravity = Gravity.CENTER;
		params.canceledOnTouchOutside = false;
		CommonDialogFragment dialog = new CommonDialogFragment();
		dialog.setParams(params);
		dialog.show(activity);

	}

	public static void showChangeNameDialog(FragmentActivity activity) {
		DialogParams params = new DialogParams();
		params.tag = DIALOG_CHANGE_NAME;
		ChangeNameDialog dialog = new ChangeNameDialog();
		params.btnLeftText = activity.getString(R.string.confirm);
		params.btnRightText = activity.getString(R.string.cancel);
		dialog.setParams(params);
		dialog.show(activity);

	}

	public static void showSendFilesDialog(FragmentActivity activity, ArrayList<User> receiversList) {
		DialogParams params = new DialogParams();
		params.tag = DIALOG_SEND_FILES;
		params.btnRightText = activity.getString(R.string.send);
		SendFilesDialog dialog = new SendFilesDialog();
		Bundle args = new Bundle();
		args.putParcelableArrayList("receivers", receiversList);
		dialog.setArguments(args);
		dialog.setParams(params);
		dialog.show(activity);
	}

	public static void showNetworkDisconnectedDialog(FragmentActivity activity) {
		DialogParams params = new DialogParams();
		params.tag = DIALOG_NETWORK_DISCONNECTED;
		params.contentText = activity.getString(R.string.network_disconnected_content);
		params.btnLeftText = activity.getString(R.string.confirm);
		params.textGravity = Gravity.CENTER;
		params.canceledOnTouchOutside = false;
		CommonDialogFragment dialog = new CommonDialogFragment();
		dialog.setParams(params);
		dialog.show(activity);
	}

	@Deprecated
	public static void showCancelTransferDialog(FragmentActivity activity, User user,
			String packetNo, boolean isSend) {
		DialogParams params = new DialogParams();
		params.tag = DIALOG_CANCEL_TRANSFER;
		// params.fragmentTag = TransferingFragment.TAG;
		params.contentText = activity.getString(R.string.transfer_cancel_content);
		params.btnLeftText = activity.getString(R.string.confirm);
		params.btnRightText = activity.getString(R.string.transfer_continue);
		params.textGravity = Gravity.CENTER;
		params.canceledOnTouchOutside = false;
		Bundle args = new Bundle();
		args.putString("packetNo", packetNo);
		args.putBoolean("issend", isSend);
		args.putParcelable("user", user);
		CommonDialogFragment dialog = new CommonDialogFragment();
		dialog.setArguments(args);
		dialog.setParams(params);
		dialog.show(activity);
	}

	public static void showNotEnoughSpaceDialog(FragmentActivity activity, User user,
			String packetNo) {
		DialogParams params = new DialogParams();
		params.tag = DIALOG_NOT_ENOUGH_SPACE;
		params.contentText = activity.getString(R.string.not_enough_space_content);
		params.btnLeftText = activity.getString(R.string.confirm);
		params.textGravity = Gravity.CENTER;
		params.canceledOnTouchOutside = false;
		Bundle args = new Bundle();
		args.putString("packetNo", packetNo);
		args.putParcelable("user", user);
		CommonDialogFragment dialog = new CommonDialogFragment();
		dialog.setArguments(args);
		dialog.setParams(params);
		dialog.show(activity);
	}

	public static FileOptionsDialog showFileOptionsDialog(FragmentActivity activity, String filePath) {
		DialogParams params = new DialogParams();
		params.tag = DIALOG_FILE_OPTIONS;
		params.fragmentTag = "fragment_received_pictures";

		Bundle args = new Bundle();
		args.putString("file_path", filePath);

		FileOptionsDialog dialog = new FileOptionsDialog();
		dialog.setParams(params);
		dialog.setArguments(args);
		dialog.show(activity);

		return dialog;
	}

	public static void showFileDetailDialog(FragmentActivity activity, String filePath) {
		DialogParams params = new DialogParams();
		params.tag = DIALOG_FILE_DETAIL;
		params.btnLeftText = activity.getString(R.string.confirm);

		Bundle args = new Bundle();
		args.putString("file_path", filePath);

		FileDetailDialog dialog = new FileDetailDialog();
		dialog.setParams(params);
		dialog.setArguments(args);
		dialog.show(activity);
	}

	public static void showWaitJoinTimeTooLongDialog(FragmentActivity activity) {
		DialogParams params = new DialogParams();
		params.tag = DIALOG_WAIT_JOIN_TIME_TOO_LONG;
		params.btnLeftText = activity.getString(R.string.continue_wait);
		params.btnRightText = activity.getString(R.string.disconnect);
		params.contentText = activity.getString(R.string.wait_join_time_too_long);

		CommonDialogFragment dialog = new CommonDialogFragment();
		dialog.setParams(params);
		dialog.show(activity);
	}
	
}
