package cn.m15.app.android.gotransfer.ui.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import cn.m15.app.android.gotransfer.R;
import cn.m15.app.android.gotransfer.enity.TransferFile;
import cn.m15.app.android.gotransfer.enity.TransferFilesManager;
import cn.m15.app.android.gotransfer.net.ipmsg2.MessageConst.FileType;
import cn.m15.app.android.gotransfer.ui.activity.MainActivity2.TransferFilesChangeListener;
import cn.m15.app.android.gotransfer.utils.FileUtil;
import cn.m15.app.android.gotransfer.utils.ImageUtil;
import cn.m15.app.android.gotransfer.utils.ValueConvertUtil;
import cn.m15.app.android.gotransfer.utils.images.ImageResizer;
import cn.m15.app.android.gotransfer.utils.images.LocalImageFetcher;
import cn.m15.app.android.gotransfer.utils.images.VedioImageFetcher;

public class FileListAdapter extends BaseAdapter implements OnCheckedChangeListener {

	private final static int ICON_FOLDER = R.drawable.img_folder_default;
	private final static int ICON_FILE = R.drawable.img_file_default;

	private final LayoutInflater mInflater;
	private LocalImageFetcher mImageFetcher;
	private VedioImageFetcher mVedioImageFetcher;
	private int mImageSize;

	private List<TransferFile> mData = new ArrayList<TransferFile>();
	private List<TransferFile> mRootDirs;

	private TransferFilesChangeListener mFilesChangeListener;

	public FileListAdapter(Context context, TransferFilesChangeListener listener, List<TransferFile> rootDirs) {
		mInflater = LayoutInflater.from(context);
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		mImageSize = dm.widthPixels
				- (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, dm);
		mImageSize /= 4;
		mImageFetcher = ImageUtil.createLocalImageFetcher((FragmentActivity) context, mImageSize);
		mVedioImageFetcher = ImageUtil.createVedioImageFetcher((FragmentActivity) context,
				mImageSize);
		mFilesChangeListener = listener;
		mRootDirs = rootDirs;
	}

	public void add(TransferFile file) {
		mData.add(file);
		notifyDataSetChanged();
	}

	public void clear() {
		mData.clear();
		notifyDataSetChanged();
	}

	@Override
	public TransferFile getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	public List<TransferFile> getListItems() {
		return mData;
	}

	/**
	 * Set the list items without notifying on the clear. This prevents loss of
	 * scroll position.
	 * 
	 * @param data
	 */
	public void setListItems(List<TransferFile> data) {
		mData = data;
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_files_chooser, null);
			holder = new ViewHolder();
			holder.mCheckBox = (CheckBox) convertView.findViewById(R.id.check_box);
			holder.mIvFileImg = (ImageView) convertView.findViewById(R.id.img_file);
			holder.mShadeView = convertView.findViewById(R.id.shade);
			holder.mTvFileName = (TextView) convertView.findViewById(R.id.tv_filename);
			holder.mTvFileInfo = (TextView) convertView.findViewById(R.id.tv_fileinfo);
			holder.mTvFolderName = (TextView) convertView.findViewById(R.id.tv_foldername);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
			holder.mCheckBox.setOnCheckedChangeListener(null);
		}

		File file = new File(getItem(position).path);
		holder.mTvFileName.setText(file.getName());
		if (isRootDir(file.getAbsolutePath())) {
			holder.mCheckBox.setVisibility(View.INVISIBLE);
			holder.mTvFileInfo.setVisibility(View.INVISIBLE);
		} else {
			holder.mTvFileInfo.setVisibility(View.VISIBLE);
			holder.mCheckBox.setVisibility(View.VISIBLE);
		}
		if (file.lastModified() == 0) {
			holder.mTvFileInfo.setText("");
		} else {
			holder.mTvFileInfo.setText(ValueConvertUtil.formateMil(file.lastModified()) + "");
		}
		loadFileThumb(file.getAbsolutePath(), holder.mIvFileImg);
		if (TransferFilesManager.getInstance().isFileSelected(file.getAbsolutePath())) {
			holder.mCheckBox.setChecked(true);
		} else {
			holder.mCheckBox.setChecked(false);
		}
		holder.mCheckBox.setTag(position);
		holder.mCheckBox.setOnCheckedChangeListener(this);

		return convertView;
	}

	private void loadFileThumb(String path, ImageView ivThumb) {
		int type = FileUtil.getFileType(path);
		ImageResizer imageFetcher = type == FileType.PICTURE ? mImageFetcher : mVedioImageFetcher;
		switch (type) {
		case FileType.PICTURE:
		case FileType.VIDEO:
			if (imageFetcher != null) {
				imageFetcher.loadImage(path, ivThumb);
			}
			break;

		case FileType.MUSIC:
			ivThumb.setImageResource(R.drawable.img_music_default);
			break;

		case FileType.OTHERS:
			ivThumb.setImageResource(R.drawable.img_file_default);
			break;

		case FileType.APP:
			ivThumb.setImageDrawable(ImageUtil.getApkImage(mInflater.getContext()
					.getPackageManager(), path));
			break;

		default:
			break;
		}
	}

	static class ViewHolder {
		CheckBox mCheckBox;
		ImageView mIvFileImg;
		View mShadeView;
		TextView mTvFileName;
		TextView mTvFileInfo;
		TextView mTvFolderName;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		int position = (Integer) ((CheckBox) buttonView).getTag();
		TransferFile file = getItem(position);
		if (isChecked) {
			TransferFilesManager.getInstance().put(file.path, file);
		} else {
			TransferFilesManager.getInstance().remove(file.path);
		}
		mFilesChangeListener.onTransferFilesChangedListener();
	}
	
	private boolean isRootDir(String path) {
		if (mRootDirs != null && mRootDirs.size() > 0) {
			for (TransferFile file : mRootDirs) {
				if (path.equals(file.path)) {
					return true;
				}
			}
		}
		return false;
	}

}