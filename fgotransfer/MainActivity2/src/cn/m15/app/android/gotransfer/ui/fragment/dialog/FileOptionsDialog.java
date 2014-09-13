package cn.m15.app.android.gotransfer.ui.fragment.dialog;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cn.m15.app.android.gotransfer.R;
import cn.m15.app.android.gotransfer.utils.DialogUtil;
import cn.m15.app.android.gotransfer.utils.FileUtil;

public class FileOptionsDialog extends CommonDialogFragment
		implements AdapterView.OnItemClickListener {
	
	private String mFilePath;
	
	@Override
	protected void onContentCreated(FrameLayout flContent) {
		mFilePath = getArguments().getString("file_path");
		Activity acitivty = getActivity();
		String[] fileOptions = acitivty.getResources().getStringArray(R.array.file_options);
		FileOptionsAdapter adapter = new FileOptionsAdapter(acitivty, R.layout.item_file_option, R.id.tv_file_option);
		for(String fileOption : fileOptions) {
			adapter.add(fileOption);
		}
		ListView listView = (ListView) LayoutInflater.from(acitivty).inflate(R.layout.dialog_file_options, null);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		flContent.addView(listView);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 
				LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.setMargins(0, 0, 0, 0);
		flContent.setLayoutParams(lp);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		dismissAllowingStateLoss();
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		FragmentActivity activity = getActivity();
		if (activity != null && !activity.isFinishing()) {
			switch(position) {
			case 0:
				openFile(activity, mFilePath);
				break;
			case 1:
				deleteFile(activity, mFilePath);
				break;
			case 2:
				DialogUtil.showFileDetailDialog(activity, mFilePath);
				break;
			}
		}
		dismiss();
	}
	
	public static void openFile(Activity activity, String filePath) {
		File file = new File(filePath);
		if (file.exists()) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
			Uri uri = Uri.fromFile(file);
			String extension = FileUtil.getFileExtensionByName(file.getName());
			String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
			intent.setDataAndType(uri, mimeType);
			activity.startActivity(Intent.createChooser(intent, null));
		} else {
			Toast.makeText(activity, R.string.file_not_exist, Toast.LENGTH_SHORT).show();
		}
	}
	
	private static boolean deleteFile(Activity activity, String filePath) {
		File file = new File(filePath);
		if (file.exists()) {
			file.delete();
			// if you want to delete a file, please add the follow code:
			activity.sendBroadcast(new Intent(
					Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, 
					Uri.fromFile(file)));
			return true;
		}
		Toast.makeText(activity, activity.getString(R.string.file_not_exist), Toast.LENGTH_SHORT).show();
		return false;
	}
	
	public static class FileOptionsAdapter extends ArrayAdapter<String> {

		public FileOptionsAdapter(Context context, int resource,
				int textViewResourceId) {
			super(context, resource, textViewResourceId);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView textView = (TextView) super.getView(position, convertView, parent);
			switch(position) {
			case 0:
				textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_file_cat, 0, 0, 0);
				break;
			case 1:
				textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_file_delete, 0, 0, 0);
				break;
			case 2:
				textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_file_properties, 0, 0, 0);
				break;
			}
			return textView;
		}
	}
}