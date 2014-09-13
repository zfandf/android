package cn.m15.app.android.gotransfer.ui.widget;

import java.util.List;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.widget.TextView;

public class ClickableTextView extends TextView {

	public ClickableTextView(Context context) {
		super(context);
	}

	public ClickableTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ClickableTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setTextWithClickableWords(String text,
			List<ClickableWord> clickableWords) {
		setMovementMethod(LinkMovementMethod.getInstance());
		setText(addClickablePart(text, clickableWords), BufferType.SPANNABLE);
	}

	private SpannableStringBuilder addClickablePart(String str,
			List<ClickableWord> clickableWords) {
		SpannableStringBuilder ssb = new SpannableStringBuilder(str);

		for (ClickableWord clickableWord : clickableWords) {
			int idx1 = str.indexOf(clickableWord.getWord());
			int idx2 = 0;
			while (idx1 != -1) {
				idx2 = idx1 + clickableWord.getWord().length();
				ssb.setSpan(clickableWord.getClickableSpan(), idx1, idx2, 0);
//				ssb.setSpan(new ForegroundColorSpan(Color.BLACK), idx1, idx2, 0);
				idx1 = str.indexOf(clickableWord.getWord(), idx2);
			}
		}

		return ssb;
	}

	public static class ClickableWord {
		private String word;
		private ClickableSpan clickableSpan;

		public ClickableWord(String word, ClickableSpan clickableSpan) {
			this.word = word;
			this.clickableSpan = clickableSpan;
		}

		/**
		 * @return the word
		 */
		public String getWord() {
			return word;
		}

		/**
		 * @return the clickableSpan
		 */
		public ClickableSpan getClickableSpan() {
			return clickableSpan;
		}
	}
}
