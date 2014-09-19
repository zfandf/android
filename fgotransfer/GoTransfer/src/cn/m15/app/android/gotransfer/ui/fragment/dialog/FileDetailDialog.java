package cn.m15.app.android.gotransfer.ui.fragment.dialog;

import java.io.File;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import cn.m15.app.android.gotransfer.R;
import cn.m15.app.android.gotransfer.utils.FileUtil;

public class FileDetailDialog extends CommonDialogFragment {
	private String mFilePath;
	
	@Override
	protected void onContentCreated(FrameLayout flContent) {
		mFilePath = getArguments().getString("file_path");
		View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_file_detail, null);
		
		TextView fileNameTv = (TextView) contentView.findViewById(R.id.tv_file_name);
		TextView fileTimeTv = (TextView) contentView.findViewById(R.id.tv_file_time);
		TextView filePathTv = (TextView) contentView.findViewById(R.id.tv_file_path);
		final TextView fileSizeTv = (TextView) contentView.findViewById(R.id.tv_file_size);
		
		File file = new File(mFilePath);
		fileNameTv.setText(file.getName());
		fileTimeTv.setText(DateFormat.format("yyyy-MM-dd kk:mm", file.lastModified()));
		filePathTv.setText(mFilePath);
		fileSizeTv.setText(R.string.computing);
		new AsyncTask<Void, Void, Long>() {
			
			@Override
			protected Long doInBackground(Void... params) {
				if (TextUtils.isEmpty(mFilePath)) return 0L;
				return FileUtil.getSize(new File(mFilePath));
			}
			
			@Override
			protected void onPostExecute(Long result) {
				fileSizeTv.setText(Formatter.formatFileSize(getActivity(), result));
			}
		}.execute();
		
		flContent.addView(contentView);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		dismissAllowingStateLoss();
	}
}
