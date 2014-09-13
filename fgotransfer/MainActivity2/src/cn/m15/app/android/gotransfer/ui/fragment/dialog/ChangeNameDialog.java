package cn.m15.app.android.gotransfer.ui.fragment.dialog;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import cn.m15.app.android.gotransfer.GoTransferApplication;
import cn.m15.app.android.gotransfer.R;

public class ChangeNameDialog extends CommonDialogFragment {

	private TextView mTv;
	private EditText mEdt;
	private int mTvColor;
	private String mUsername;

	@Override
	protected void onContentCreated(FrameLayout flContent) {
		// TODO Auto-generated method stub

		mTvColor = getActivity().getResources().getColor(R.color.c4);
		View view = LayoutInflater.from(getActivity()).inflate(
				R.layout.dialog_change_name, null);
		mEdt = (EditText) view.findViewById(R.id.edt_name);
		mTv = (TextView) view.findViewById(R.id.tv_title);
		mTv.setTextColor(mTvColor);
		flContent.addView(view);
		mUsername = GoTransferApplication.getInstance().getSharedPreferences("SelfName",
				Context.MODE_PRIVATE).getString("username", GoTransferApplication.getInstance().getSelfName());
		mEdt.setText(mUsername);
		mEdt.setSelection(mUsername.length());
	}

	@Override
	protected void initButtons(FrameLayout flButtons) {
		super.initButtons(flButtons);
		mEdt.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				if (TextUtils.isEmpty(mEdt.getText().toString().trim())) {
					mLeftBtn.setEnabled(false);
					mLeftBtn.setTextColor(getResources().getColor(R.color.btn_text_disable));
				} else {
					mLeftBtn.setEnabled(true);
					mLeftBtn.setTextColor(getResources().getColor(R.color.c4));
				}
			}
		});
	}

	public String getEditText() {
		String name = mEdt.getText().toString().trim();
		return name;

	}

}
