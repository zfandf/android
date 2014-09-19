package cn.m15.app.android.gotransfer.ui.fragment.dialog;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import cn.m15.app.android.gotransfer.GoTransferApplication;
import cn.m15.app.android.gotransfer.R;
import cn.m15.gotransfer.sdk.ConfigManager;

public class ChangeNameDialog extends CommonDialogFragment {

	private EditText mEdt;
	private String mUsername;

	@Override
	protected void onContentCreated(FrameLayout flContent) {
		View view = LayoutInflater.from(getActivity()).inflate(
				R.layout.dialog_change_name, null);
		mEdt = (EditText) view.findViewById(R.id.edt_name);
		flContent.addView(view);
		mUsername = GoTransferApplication.getInstance().getSharedPreferences("SelfName",
				Context.MODE_PRIVATE).getString("username", ConfigManager.getInstance().getSelfName());
		mEdt.setText(mUsername);
		mEdt.setSelection(mUsername.length());
		
	}

	@Override
	protected void initButtons(FrameLayout flButtons) {
		super.initButtons(flButtons);
		mEdt.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int count, int after) { 
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				String name = s.toString();
				if (TextUtils.isEmpty(name)) {
					mRightBtn.setEnabled(false);
					mRightBtn.setTextColor(getResources().getColor(R.color.c8));
				} else {
					mRightBtn.setEnabled(true);
					mRightBtn.setTextColor(getResources().getColor(R.color.c4));
					int chineseCount = getChineseCount(name);
					int englishCount = name.length() - chineseCount;
					if (chineseCount * 3 + englishCount > 15) {
						mEdt.setText("");
						mEdt.append(s.subSequence(0, name.length() - 1));
					}
				}
			}
		});
	}

	public String getEditText() {
		String name = mEdt.getText().toString().trim();
		return name;
	}
	
	public static int getChineseCount(String content) {
		int count = 0;
		String regEx = "[\u4e00-\u9fa5]";
		Pattern pattern = Pattern.compile(regEx);
		Matcher matcher = pattern.matcher(content);
		while(matcher.find()) {
			count ++;
		}
		return count;
	}
}
