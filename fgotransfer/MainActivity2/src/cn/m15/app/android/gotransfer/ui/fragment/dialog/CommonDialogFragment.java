package cn.m15.app.android.gotransfer.ui.fragment.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import cn.m15.app.android.gotransfer.R;

public class CommonDialogFragment extends DialogFragment implements View.OnClickListener {
	public static final String DEFAULT_TAG = "dialog_common";
	
	protected DialogButtonClickListener mListener;
	protected DialogDismissListener mDismissListener;
	
	protected LayoutInflater mInflater;
	
	protected DialogParams mParams;
	
	protected Button mLeftBtn;
	protected Button mRightBtn;
	
	public static class DialogParams implements Parcelable {
		public String title; // 标题, 不设置则不显示标题
		public String contentText; // 不能NULL
		// 设置单按钮文字任选以下两个变量的其中之一
		public String btnLeftText; // 左侧按钮文字
		public String btnRightText;// 右侧按钮文字
		public String tag;		   // Dialog Tag
		public boolean canceledOnTouchOutside = true;
		public String fragmentTag;
		public int textGravity;
		
		public DialogParams () {}
		
		private DialogParams(Parcel in) {
			title = in.readString();
			contentText = in.readString();
			btnLeftText = in.readString();
			btnRightText = in.readString();
			tag = in.readString();
			canceledOnTouchOutside = in.readInt() == 1 ? true : false;
			fragmentTag = in.readString();
			textGravity = in.readInt();
		}
		
		@Override
		public int describeContents() {
			return 0;
		}
		
		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(title);
			dest.writeString(contentText);
			dest.writeString(btnLeftText);
			dest.writeString(btnRightText);
			dest.writeString(tag);
			dest.writeInt(canceledOnTouchOutside ? 1 : 0);
			dest.writeString(fragmentTag);
			dest.writeInt(textGravity);
		}
		
		public static final Parcelable.Creator<DialogParams> CREATOR = new Parcelable.Creator<DialogParams>() {
			public DialogParams createFromParcel(Parcel in) {
				return new DialogParams(in);
			}

			public DialogParams[] newArray(int size) {
				return new DialogParams[size];
			}
		};
	}
	
	public void show(FragmentActivity activity) {
        if (activity != null && !activity.isFinishing()) {
        	FragmentManager fm = activity.getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            Fragment f = fm.findFragmentByTag(mParams.tag);
            if (f != null) {
            	ft.remove(f);
            }
            ft.add(this, mParams.tag);
            ft.commitAllowingStateLoss();
        }
    }
	
	@Override
	public final Dialog onCreateDialog(Bundle savedInstanceState) {
		mInflater = getActivity().getLayoutInflater();
        ViewGroup root = (ViewGroup) mInflater.inflate(R.layout.dialog_general, null);
        FrameLayout flTitle = (FrameLayout) root.findViewById(R.id.fl_dialog_title);
        FrameLayout flContent = (FrameLayout) root.findViewById(R.id.fl_dialog_content);
        FrameLayout flButtons = (FrameLayout) root.findViewById(R.id.fl_dialog_below_button);
        
        // 设置标题
        initTitleView(flTitle);
        // 设置内容
        onContentCreated(flContent);
        // 设置按钮文字与事件
        initButtons(flButtons);

        // 创建AlertDailog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        AlertDialog dialog = builder.create();
//      Control whether the shown Dialog is cancelable. 
//      Use DialogFragment.setCancelable(boolean) instead of directly calling this, 
//      because DialogFragment needs to change its behavior based on this.
//      dialog.setCancelable(mParams.cancelable);
        dialog.setCanceledOnTouchOutside(mParams.canceledOnTouchOutside);
        dialog.setView(root, 0, 0, 0, 0);
        return dialog;
	}
	
	/**
	 * 初始化title
	 * @param root
	 */
	protected void initTitleView(FrameLayout flTitle) {
		View defaultTitleView = mInflater.inflate(R.layout.dialog_general_title, null);
        TextView tvTitle = (TextView) defaultTitleView.findViewById(R.id.tv_dialog_title);
        if (!TextUtils.isEmpty(mParams.title)) {
        	tvTitle.setVisibility(View.VISIBLE);
        	tvTitle.setText(mParams.title);
        } else {
            tvTitle.setVisibility(View.GONE);
        }
        flTitle.addView(defaultTitleView);
	}
	
	/**
	 * 初始化buttons
	 * @param root
	 */
	protected void initButtons(FrameLayout flButtons) {
		View defaultButtonsView = mInflater.inflate(R.layout.dialog_general_below_button, flButtons, true);
		ViewStub singleBtnVs = (ViewStub) defaultButtonsView.findViewById(R.id.vs_dialog_below_button_single);
		ViewStub doubleBtnVs = (ViewStub) defaultButtonsView.findViewById(R.id.vs_dialog_below_button_double);
		
		View buttonsView = null;
		boolean hasLeft = !TextUtils.isEmpty(mParams.btnLeftText);
		boolean hasRight = !TextUtils.isEmpty(mParams.btnRightText);
		if (hasLeft && hasRight) { // double buttons
			buttonsView = doubleBtnVs.inflate();
			mLeftBtn = (Button) buttonsView.findViewById(R.id.btn_dialog_left);
			mLeftBtn.setText(mParams.btnLeftText);
			mLeftBtn.setOnClickListener(this);
	        mRightBtn = (Button) buttonsView.findViewById(R.id.btn_dialog_right);
	        mRightBtn.setText(mParams.btnRightText);
	        mRightBtn.setOnClickListener(this);
	        onLeftButtonCreated(mLeftBtn);
	        onRightButtonCreated(mRightBtn);
		} else if (hasLeft || hasRight) { // single button
			buttonsView = singleBtnVs.inflate();
			mLeftBtn = (Button) buttonsView.findViewById(R.id.btn_dialog_left);
			mLeftBtn.setOnClickListener(this);
	        if (hasLeft) {
	        	mLeftBtn.setText(mParams.btnLeftText);
	        } else {
	        	mLeftBtn.setText(mParams.btnRightText);
	        }
	        onLeftButtonCreated(mLeftBtn);
		}
	}
	
	protected void onLeftButtonCreated(Button leftBtn) {
		
	}
	
	protected void onRightButtonCreated(Button rightBtn) {
		
	}
	
	/**
	 * Override this method if it isn't fitted.
	 * @param flContent
	 */
	protected void onContentCreated(FrameLayout flContent) {
		if (mParams.contentText == null) {
			throw new IllegalArgumentException("The content of the common dialog is empty!");
		}
		View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_content_text, null);
		TextView tvContent = (TextView) contentView.findViewById(R.id.tv_dialog_content);
        tvContent.setText(mParams.contentText);
        if (mParams.textGravity == 0) {
        	tvContent.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
        } else {
        	tvContent.setGravity(mParams.textGravity);
        }
        flContent.addView(contentView);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mParams = savedInstanceState.getParcelable("dialog_params");
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (getActivity() instanceof FragmentActivity && !TextUtils.isEmpty(mParams.fragmentTag)) {
			FragmentManager fm = ((FragmentActivity) getActivity()).getSupportFragmentManager();
			Fragment f = fm.findFragmentByTag(mParams.fragmentTag);
			if (f instanceof DialogButtonClickListener) {
				mListener = (DialogButtonClickListener) f;
			}
			if (f instanceof DialogDismissListener) {
				mDismissListener = (DialogDismissListener) f;
			}
		}
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mParams != null) {
			outState.putParcelable("dialog_params", mParams);
		}
	}
	
	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		if (mDismissListener != null) {
			mDismissListener.onDialogDismiss(mParams.tag);
		}
	}
	
	@Override
	public void onClick(View v) {
		if (mListener == null) {
			 dismissAllowingStateLoss();
		} else {
			int which = DialogInterface.BUTTON_NEGATIVE;
			switch(v.getId()) {
			case R.id.btn_dialog_left:
				which = DialogInterface.BUTTON_POSITIVE;
				break;
			}
			mListener.onDialogButtonClick(this, which, mParams.tag);
		}
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (mListener == null && activity instanceof DialogButtonClickListener) {
			mListener = (DialogButtonClickListener) activity;
		} 
		if (mDismissListener == null && activity instanceof DialogDismissListener) {
			mDismissListener = (DialogDismissListener) activity;
		} 
	}
	
	public static interface DialogButtonClickListener {
		
		/**
		 * Dialog的按钮点击监听事件
		 * @param dialog
		 * @param which		识别哪个按钮被点击
		 * 	DialogInterface.BUTTON_NEGATIVE表示左边按钮
		 *  DialogInterface.BUTTON_NEUTRAL表示中间按钮 
		 *  DialogInterface.BUTTON_POSITIVE表示右边按钮
		 * @param tag		Dialog标签
		 */
		public void onDialogButtonClick(CommonDialogFragment dialog, int which, String tag);
	}
	
	public static interface DialogDismissListener {
		
		/**
		 * Dialog消失点击监听事件
		 * @param tag	Dialog标签
		 */
		public void onDialogDismiss(String tag);
	}
	
	public void setParams(DialogParams params) {
		if (params == null) {
			throw new IllegalArgumentException("DialogParams is NULL!");
		}
		this.mParams = params;
		if (TextUtils.isEmpty(mParams.tag)) {
			mParams.tag = DEFAULT_TAG;
		}
	}

	public CommonDialogFragment setDialogButtonClickListener(DialogButtonClickListener listener) {
		this.mListener = listener;
		return this;
	}

	public CommonDialogFragment setDismissListener(DialogDismissListener dismissListener) {
		this.mDismissListener = dismissListener;
		return this;
	}

}
